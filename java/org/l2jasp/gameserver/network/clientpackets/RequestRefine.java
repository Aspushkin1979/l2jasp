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
import org.l2jasp.gameserver.data.xml.AugmentationData;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.ItemTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ExVariationResult;
import org.l2jasp.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jasp.gameserver.network.serverpackets.StatusUpdate;
import org.l2jasp.gameserver.util.Util;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public class RequestRefine implements ClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_targetItemObjId = packet.readInt();
		_refinerItemObjId = packet.readInt();
		_gemstoneItemObjId = packet.readInt();
		_gemstoneCount = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Flood protect to augment script
		if (!client.getFloodProtectors().canAugmentItem())
		{
			return;
		}
		
		final Item targetItem = (Item) World.getInstance().findObject(_targetItemObjId);
		final Item refinerItem = (Item) World.getInstance().findObject(_refinerItemObjId);
		final Item gemstoneItem = (Item) World.getInstance().findObject(_gemstoneItemObjId);
		if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null) || (targetItem.getOwnerId() != player.getObjectId()) || (refinerItem.getOwnerId() != player.getObjectId()) || (gemstoneItem.getOwnerId() != player.getObjectId()) || (player.getLevel() < 46)) // must
																																																																					// be
																																																																					// level
																																																																					// 46
		{
			player.sendPacket(new ExVariationResult(0, 0, 0));
			player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		// unequip item
		if (targetItem.isEquipped())
		{
			player.disarmWeapons();
		}
		
		if (TryAugmentItem(player, targetItem, refinerItem, gemstoneItem))
		{
			final int stat12 = 0x0000FFFF & targetItem.getAugmentation().getAugmentationId();
			final int stat34 = targetItem.getAugmentation().getAugmentationId() >> 16;
			player.sendPacket(new ExVariationResult(stat12, stat34, 1));
			player.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
		}
		else
		{
			player.sendPacket(new ExVariationResult(0, 0, 0));
			player.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
		}
	}
	
	boolean TryAugmentItem(Player player, Item targetItem, Item refinerItem, Item gemstoneItem)
	{
		if (targetItem.isAugmented() || targetItem.isWear())
		{
			player.sendMessage("You can't augment items while you wear it");
			return false;
		}
		
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendMessage("You cannot augment while trading");
			return false;
		}
		
		if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		
		// check for the items to be in the inventory of the owner
		if (player.getInventory().getItemByObjectId(refinerItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong LifeStone-id.", Config.DEFAULT_PUNISH);
			return false;
		}
		
		if (player.getInventory().getItemByObjectId(targetItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Weapon-id.", Config.DEFAULT_PUNISH);
			return false;
		}
		
		if (player.getInventory().getItemByObjectId(gemstoneItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Gemstone-id.", Config.DEFAULT_PUNISH);
			return false;
		}
		
		final int itemGrade = targetItem.getTemplate().getItemGrade();
		final int itemType = targetItem.getTemplate().getType2();
		final int lifeStoneId = refinerItem.getItemId();
		final int gemstoneItemId = gemstoneItem.getItemId();
		
		// is the refiner Item a life stone?
		if ((lifeStoneId < 8723) || (lifeStoneId > 8762))
		{
			return false;
		}
		
		// must be a weapon, must be > d grade
		// TODO: can do better? : currently: using isdestroyable() as a check for hero / cursed weapons
		if ((itemGrade < ItemTemplate.CRYSTAL_C) || (itemType != ItemTemplate.TYPE2_WEAPON) || !targetItem.isDestroyable())
		{
			return false;
		}
		
		// player must be able to use augmentation
		if ((player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) || player.isDead() || player.isParalyzed() || player.isFishing() || player.isSitting())
		{
			return false;
		}
		
		int modifyGemstoneCount = _gemstoneCount;
		final int lifeStoneLevel = getLifeStoneLevel(lifeStoneId);
		final int lifeStoneGrade = getLifeStoneGrade(lifeStoneId);
		switch (itemGrade)
		{
			case ItemTemplate.CRYSTAL_C:
			{
				if ((player.getLevel() < 46) || (gemstoneItemId != 2130))
				{
					return false;
				}
				modifyGemstoneCount = 20;
				break;
			}
			case ItemTemplate.CRYSTAL_B:
			{
				if ((player.getLevel() < 52) || (gemstoneItemId != 2130))
				{
					return false;
				}
				modifyGemstoneCount = 30;
				break;
			}
			case ItemTemplate.CRYSTAL_A:
			{
				if ((player.getLevel() < 61) || (gemstoneItemId != 2131))
				{
					return false;
				}
				modifyGemstoneCount = 20;
				break;
			}
			case ItemTemplate.CRYSTAL_S:
			{
				if ((player.getLevel() < 76) || (gemstoneItemId != 2131))
				{
					return false;
				}
				modifyGemstoneCount = 25;
				break;
			}
		}
		
		// check if the lifestone is appropriate for this player
		switch (lifeStoneLevel)
		{
			case 1:
			{
				if (player.getLevel() < 46)
				{
					return false;
				}
				break;
			}
			case 2:
			{
				if (player.getLevel() < 49)
				{
					return false;
				}
				break;
			}
			case 3:
			{
				if (player.getLevel() < 52)
				{
					return false;
				}
				break;
			}
			case 4:
			{
				if (player.getLevel() < 55)
				{
					return false;
				}
				break;
			}
			case 5:
			{
				if (player.getLevel() < 58)
				{
					return false;
				}
				break;
			}
			case 6:
			{
				if (player.getLevel() < 61)
				{
					return false;
				}
				break;
			}
			case 7:
			{
				if (player.getLevel() < 64)
				{
					return false;
				}
				break;
			}
			case 8:
			{
				if (player.getLevel() < 67)
				{
					return false;
				}
				break;
			}
			case 9:
			{
				if (player.getLevel() < 70)
				{
					return false;
				}
				break;
			}
			case 10:
			{
				if (player.getLevel() < 76)
				{
					return false;
				}
				break;
			}
		}
		
		// Check if player has all gemstorne on inventory
		if ((gemstoneItem.getCount() - modifyGemstoneCount) < 0)
		{
			return false;
		}
		
		// consume the life stone
		if (!player.destroyItem("RequestRefine", refinerItem, null, false))
		{
			return false;
		}
		
		// consume the gemstones
		player.destroyItem("RequestRefine", _gemstoneItemObjId, modifyGemstoneCount, null, false);
		
		// generate augmentation
		targetItem.setAugmentation(AugmentationData.getInstance().generateRandomAugmentation(targetItem, lifeStoneLevel, lifeStoneGrade));
		
		// finish and send the inventory update packet
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		player.sendPacket(iu);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		return true;
	}
	
	private int getLifeStoneGrade(int itemIdValue)
	{
		int itemId = itemIdValue - 8723;
		if (itemId < 10)
		{
			return 0; // normal grade
		}
		
		if (itemId < 20)
		{
			return 1; // mid grade
		}
		
		if (itemId < 30)
		{
			return 2; // high grade
		}
		
		return 3; // top grade
	}
	
	private int getLifeStoneLevel(int itemIdValue)
	{
		int itemId = itemIdValue - (10 * getLifeStoneGrade(itemIdValue));
		itemId -= 8722;
		return itemId;
	}
}