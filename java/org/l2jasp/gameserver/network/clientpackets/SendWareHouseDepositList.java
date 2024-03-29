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

import org.l2jasp.Config;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.instance.Folk;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.item.type.EtcItemType;
import org.l2jasp.gameserver.model.itemcontainer.ClanWarehouse;
import org.l2jasp.gameserver.model.itemcontainer.ItemContainer;
import org.l2jasp.gameserver.model.itemcontainer.PlayerWarehouse;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;
import org.l2jasp.gameserver.network.serverpackets.EnchantResult;
import org.l2jasp.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jasp.gameserver.network.serverpackets.StatusUpdate;

public class SendWareHouseDepositList implements ClientPacket
{
	private int _count;
	private int[] _items;
	
	@Override
	public void read(ReadablePacket packet)
	{
		if (!Config.ALLOW_WAREHOUSE)
		{
			return;
		}
		
		_count = packet.readInt();
		
		// check packet list size
		if ((_count < 0) || ((_count * 8) > packet.getRemainingLength()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			final int objectId = packet.readInt();
			_items[(i * 2) + 0] = objectId;
			final long cnt = packet.readInt();
			if ((cnt > Integer.MAX_VALUE) || (cnt < 0))
			{
				_count = 0;
				return;
			}
			
			_items[(i * 2) + 1] = (int) cnt;
		}
	}
	
	@Override
	public void run(GameClient client)
	{
		if (_items == null)
		{
			return;
		}
		
		if (_count < 1)
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		final Folk manager = player.getLastFolkNPC();
		if ((manager == null) || !player.isInsideRadius2D(manager, Npc.INTERACTION_DISTANCE))
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			return;
		}
		
		// Like L2OFF you can't confirm a deposit when you are in trade.
		if (player.getActiveTradeList() != null)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((warehouse instanceof ClanWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isDead())
		{
			player.sendMessage("You can't deposit items while you are dead.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		// Like L2OFF enchant window must close
		if (player.getActiveEnchantItem() != null)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_CANCELLED_THE_ENCHANTING_PROCESS);
			player.sendPacket(new EnchantResult(0));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		final int fee = _count * 30;
		int currentAdena = player.getAdena();
		int slots = 0;
		for (int i = 0; i < _count; i++)
		{
			final int objectId = _items[(i * 2) + 0];
			final int count = _items[(i * 2) + 1];
			
			// Check validity of requested item
			final Item item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				_items[(i * 2) + 0] = 0;
				_items[(i * 2) + 1] = 0;
				continue;
			}
			
			if (((warehouse instanceof ClanWarehouse) && !item.isTradeable()) || (item.getItemType() == EtcItemType.QUEST))
			{
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getItemId() == 57)
			{
				currentAdena -= count;
			}
			
			if (!item.isStackable())
			{
				slots += count;
			}
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			final int objectId = _items[(i * 2) + 0];
			final int count = _items[(i * 2) + 1];
			
			// check for an invalid item
			if ((objectId == 0) && (count == 0))
			{
				continue;
			}
			
			final Item oldItem = player.getInventory().getItemByObjectId(objectId);
			if (oldItem == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			if (!oldItem.isAvailable(player, true, warehouse instanceof PlayerWarehouse))
			{
				continue;
			}
			
			final int itemId = oldItem.getItemId();
			if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
			{
				continue;
			}
			
			if (CursedWeaponsManager.getInstance().isCursed(itemId))
			{
				PacketLogger.warning(player.getName() + " try to deposit Cursed Weapon on wherehouse.");
				continue;
			}
			
			final Item newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				PacketLogger.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
		}
		
		// Send updated item list to the player
		player.sendPacket(playerIU);
		
		// Update current load status on player
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
