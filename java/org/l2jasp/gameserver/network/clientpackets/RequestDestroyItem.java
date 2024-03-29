/*
 * This file is part of the L2J Asp project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jasp.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.l2jasp.Config;
import org.l2jasp.commons.database.DatabaseFactory;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.data.sql.PetDataTable;
import org.l2jasp.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jasp.gameserver.network.serverpackets.StatusUpdate;
import org.l2jasp.gameserver.util.Util;

public class RequestDestroyItem implements ClientPacket
{
	private int _objectId;
	private int _count;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_objectId = packet.readInt();
		_count = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_count <= 0)
		{
			if (_count < 0)
			{
				Util.handleIllegalPlayerAction(player, "[RequestDestroyItem] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			}
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You destroying items too fast.");
			return;
		}
		
		int count = _count;
		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		final Item itemToRemove = player.getInventory().getItemByObjectId(_objectId);
		
		// if we cant find requested item, its actualy a cheat!
		if (itemToRemove == null)
		{
			return;
		}
		
		// Cannot discard item that the skill is consumming
		if (player.isCastingNow() && (player.getCurrentSkill() != null) && (player.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId()))
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		final int itemId = itemToRemove.getItemId();
		if (itemToRemove.isWear() || !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if (!itemToRemove.isStackable() && (count > 1))
		{
			Util.handleIllegalPlayerAction(player, "[RequestDestroyItem] count > 1 but item is not stackable! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		
		if (_count > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}
		
		if (itemToRemove.isEquipped())
		{
			if (itemToRemove.isAugmented())
			{
				itemToRemove.getAugmentation().removeBonus(player);
			}
			
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item element : player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot()))
			{
				player.checkSSMatch(null, element);
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		
		if (PetDataTable.isPetItem(itemId))
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				if ((player.getPet() != null) && (player.getPet().getControlItemId() == _objectId))
				{
					player.getPet().unSummon(player);
				}
				
				// if it's a pet control item, delete the pet
				final PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				PacketLogger.warning("Could not delete pet objectid: " + e);
			}
		}
		
		final Item removedItem = player.getInventory().destroyItem("Destroy", _objectId, count, player, null);
		if (removedItem == null)
		{
			return;
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		if (removedItem.getCount() == 0)
		{
			iu.addRemovedItem(removedItem);
		}
		else
		{
			iu.addModifiedItem(removedItem);
		}
		player.sendPacket(iu);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}