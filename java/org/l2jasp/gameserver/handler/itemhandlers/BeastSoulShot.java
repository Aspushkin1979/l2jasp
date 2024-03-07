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
package org.l2jasp.gameserver.handler.itemhandlers;

import org.l2jasp.Config;
import org.l2jasp.gameserver.handler.IItemHandler;
import org.l2jasp.gameserver.model.actor.Playable;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.Summon;
import org.l2jasp.gameserver.model.actor.instance.BabyPet;
import org.l2jasp.gameserver.model.actor.instance.Pet;
import org.l2jasp.gameserver.model.item.Weapon;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jasp.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.util.Broadcast;

/**
 * Beast SoulShot Handler
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6645
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (playable == null)
		{
			return;
		}
		
		Player activeOwner = null;
		if (playable instanceof Summon)
		{
			activeOwner = ((Summon) playable).getOwner();
			activeOwner.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
			
			return;
		}
		else if (playable instanceof Player)
		{
			activeOwner = (Player) playable;
		}
		
		if (activeOwner == null)
		{
			return;
		}
		
		final Summon activePet = activeOwner.getPet();
		if (activePet == null)
		{
			activeOwner.sendPacket(SystemMessageId.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return;
		}
		
		if (activePet.isDead())
		{
			activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR_SAD_ISN_T_IT);
			return;
		}
		
		final int itemId = 6645;
		int shotConsumption = 1;
		Item weaponInst = null;
		Weapon weaponItem = null;
		if ((activePet instanceof Pet) && !(activePet instanceof BabyPet))
		{
			weaponInst = ((Pet) activePet).getActiveWeaponInstance();
			weaponItem = ((Pet) activePet).getActiveWeaponItem();
			if (weaponInst == null)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
				return;
			}
			
			if (weaponInst.getChargedSoulshot() != Item.CHARGED_NONE)
			{
				// SoulShots are already active.
				return;
			}
			
			final int shotCount = item.getCount();
			shotConsumption = weaponItem.getSoulShotCount();
			if (shotConsumption == 0)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
				return;
			}
			
			if ((shotCount <= shotConsumption))
			{
				// Not enough Soulshots to use.
				activeOwner.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
				return;
			}
			
			weaponInst.setChargedSoulshot(Item.CHARGED_SOULSHOT);
		}
		else
		{
			if (activePet.getChargedSoulShot() != Item.CHARGED_NONE)
			{
				return;
			}
			
			activePet.setChargedSoulShot(Item.CHARGED_SOULSHOT);
		}
		
		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!Config.DONT_DESTROY_SS && !activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (activeOwner.getAutoSoulShot().containsKey(itemId))
			{
				activeOwner.removeAutoSoulShot(itemId);
				activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addString(item.getTemplate().getName());
				activeOwner.sendPacket(sm);
				
				return;
			}
			activeOwner.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT);
			return;
		}
		
		// Pet uses the power of spirit.
		activeOwner.sendPacket(SystemMessageId.PET_USES_THE_POWER_OF_SPIRIT);
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, 2033, 1, 0, 0), 360000);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}