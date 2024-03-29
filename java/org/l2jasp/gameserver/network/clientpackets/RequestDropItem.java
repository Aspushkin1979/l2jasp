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
import org.l2jasp.gameserver.data.xml.AdminData;
import org.l2jasp.gameserver.enums.IllegalActionPunishmentType;
import org.l2jasp.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.holders.SkillUseHolder;
import org.l2jasp.gameserver.model.item.ItemTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.item.type.EtcItemType;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;
import org.l2jasp.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jasp.gameserver.network.serverpackets.ItemList;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.util.Util;

public class RequestDropItem implements ClientPacket
{
	private int _objectId;
	private int _count;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_objectId = packet.readInt();
		_count = packet.readInt();
		_x = packet.readInt();
		_y = packet.readInt();
		_z = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		// Flood protect drop to avoid packet lag
		if (!client.getFloodProtectors().canDropItem())
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if ((player == null) || player.isDead())
		{
			return;
		}
		
		if (player.isGM() && (player.getAccessLevel().getLevel() < 80))
		{ // just head GM and admin can drop items on the ground
			player.sendPacket(SystemMessage.sendString("You have not right to discard anything from inventory."));
			return;
		}
		
		// Fix against safe enchant exploit
		if (player.getActiveEnchantItem() != null)
		{
			player.sendPacket(SystemMessage.sendString("You can't discard items during enchant."));
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_objectId);
		if ((item == null) || (_count == 0) || !player.validateItemManipulation(_objectId, "drop"))
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if ((!Config.ALLOW_DISCARDITEM && !player.isGM()) || (!item.isDropable()))
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if (item.isAugmented())
		{
			player.sendPacket(SystemMessageId.THE_AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if ((item.getItemType() == EtcItemType.QUEST) && !(player.isGM()))
		{
			return;
		}
		
		// Drop item disabled by config
		if (player.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS)
		{
			player.sendMessage("Drop item disabled for GM by config!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't allow if it's flying
		if (player.isFlying())
		{
			return;
		}
		
		// Cursed Weapons cannot be dropped
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			return;
		}
		
		if (_count > item.getCount())
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if ((Config.PLAYER_SPAWN_PROTECTION > 0) && player.isInvul() && !player.isGM())
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if (_count <= 0)
		{
			player.setAccessLevel(-1); // ban
			Util.handleIllegalPlayerAction(player, "[RequestDropItem] count <= 0! ban! oid: " + _objectId + " owner: " + player.getName(), IllegalActionPunishmentType.KICK);
			return;
		}
		
		if (!item.isStackable() && (_count > 1))
		{
			Util.handleIllegalPlayerAction(player, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + player.getName(), IllegalActionPunishmentType.KICK);
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isProcessingTransaction() || (player.getPrivateStoreType() != 0))
		{
			player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		if (player.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			player.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}
		
		if (player.isCastingNow())
		{
			final SkillUseHolder skill = player.getCurrentSkill();
			if (skill != null)
			{
				// Cannot discard item that the skill is consuming.
				if ((skill.getSkill().getItemConsumeId() == item.getItemId()) && ((player.getInventory().getInventoryItemCount(item.getItemId(), -1) - skill.getSkill().getItemConsume()) < _count))
				{
					player.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DISCARDED);
					return;
				}
				
				// Do not drop items when casting known skills to avoid exploits.
				if (player.getKnownSkill(skill.getSkillId()) != null)
				{
					player.sendMessage("You cannot drop an item while casting " + skill.getSkill().getName() + ".");
					return;
				}
			}
		}
		
		if ((ItemTemplate.TYPE2_QUEST == item.getTemplate().getType2()) && !player.isGM())
		{
			player.sendPacket(SystemMessageId.THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED);
			return;
		}
		
		if (!player.isInsideRadius2D(_x, _y, _z, 150) || (Math.abs(_z - player.getZ()) > 50))
		{
			player.sendPacket(SystemMessageId.THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD);
			return;
		}
		
		if (item.isEquipped())
		{
			// Remove augementation boni on unequip
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(player);
			}
			
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item element : player.getInventory().unEquipItemInBodySlotAndRecord(item.getTemplate().getBodyPart()))
			{
				player.checkSSMatch(null, element);
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
			
			player.sendPacket(new ItemList(player, true));
		}
		
		final Item dropedItem = player.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, false);
		if ((dropedItem != null) && (dropedItem.getItemId() == 57) && (dropedItem.getCount() >= 1000000) && (Config.RATE_DROP_ADENA <= 200))
		{
			final String msg = "Character (" + player.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			PacketLogger.warning(msg);
			AdminData.broadcastMessageToGMs(msg);
		}
	}
}
