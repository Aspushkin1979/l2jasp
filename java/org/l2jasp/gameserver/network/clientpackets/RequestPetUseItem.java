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

import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.data.sql.PetDataTable;
import org.l2jasp.gameserver.handler.IItemHandler;
import org.l2jasp.gameserver.handler.ItemHandler;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.instance.Pet;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.PetInfo;
import org.l2jasp.gameserver.network.serverpackets.PetItemList;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

public class RequestPetUseItem implements ClientPacket
{
	private int _objectId;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_objectId = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().canUseItem())
		{
			return;
		}
		
		final Pet pet = (Pet) player.getPet();
		if (pet == null)
		{
			return;
		}
		
		final Item item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (item.isWear())
		{
			return;
		}
		
		final int itemId = item.getItemId();
		if (player.isAlikeDead() || pet.isDead())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(item.getItemId());
			player.sendPacket(sm);
			return;
		}
		
		// check if the item matches the pet
		if (item.isEquipable())
		{
			if (PetDataTable.isWolf(pet.getNpcId()) && // wolf
				item.getTemplate().isForWolf())
			{
				useItem(pet, item, player);
				return;
			}
			else if (PetDataTable.isHatchling(pet.getNpcId()) && // hatchlings
				item.getTemplate().isForHatchling())
			{
				useItem(pet, item, player);
				return;
			}
			else if (PetDataTable.isStrider(pet.getNpcId()) && // striders
				item.getTemplate().isForStrider())
			{
				useItem(pet, item, player);
				return;
			}
			else if (PetDataTable.isBaby(pet.getNpcId()) && // baby pets (buffalo, cougar, kookaboora)
				item.getTemplate().isForBabyPet())
			{
				useItem(pet, item, player);
				return;
			}
			else
			{
				player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
				return;
			}
		}
		else if (PetDataTable.isPetFood(itemId))
		{
			if (PetDataTable.isWolf(pet.getNpcId()) && PetDataTable.isWolfFood(itemId))
			{
				feed(pet, item);
				return;
			}
			
			if (PetDataTable.isSinEater(pet.getNpcId()) && PetDataTable.isSinEaterFood(itemId))
			{
				feed(pet, item);
				return;
			}
			else if (PetDataTable.isHatchling(pet.getNpcId()) && PetDataTable.isHatchlingFood(itemId))
			{
				feed(pet, item);
				return;
			}
			else if (PetDataTable.isStrider(pet.getNpcId()) && PetDataTable.isStriderFood(itemId))
			{
				feed(pet, item);
				return;
			}
			else if (PetDataTable.isWyvern(pet.getNpcId()) && PetDataTable.isWyvernFood(itemId))
			{
				feed(pet, item);
				return;
			}
			else if (PetDataTable.isBaby(pet.getNpcId()) && PetDataTable.isBabyFood(itemId))
			{
				feed(pet, item);
				return;
			}
		}
		
		final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
		if (handler != null)
		{
			useItem(pet, item, player);
		}
		else
		{
			player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
		}
	}
	
	private synchronized void useItem(Pet pet, Item item, Player player)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}
			
			player.sendPacket(new PetItemList(pet));
			player.sendPacket(new PetInfo(pet));
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			pet.updateEffectIcons(true);
		}
		else
		{
			// LOGGER.finest("item not equipable id:"+ item.getItemId());
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if (handler == null)
			{
				PacketLogger.warning("No itemhandler registered for itemId: " + item.getItemId());
			}
			else
			{
				handler.useItem(pet, item);
			}
		}
	}
	
	/**
	 * When fed by owner double click on food from pet inventory.<br>
	 * <font color=#FF0000><b><u>Caution</u>: 1 food = 100 points of currentFed</b></font>
	 * @param pet
	 * @param item
	 */
	private void feed(Pet pet, Item item)
	{
		// if pet has food in inventory
		if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false))
		{
			pet.setCurrentFed(pet.getCurrentFed() + 100);
		}
		
		pet.broadcastStatusUpdate();
	}
}
