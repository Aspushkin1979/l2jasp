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
package org.l2jasp.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jasp.gameserver.handler.ISkillHandler;
import org.l2jasp.gameserver.model.Fishing;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.Weapon;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.item.type.WeaponType;
import org.l2jasp.gameserver.model.skill.SkillType;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

public class FishingSkill implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.PUMPING,
		SkillType.REELING
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (!(creature instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) creature;
		final Fishing fish = player.getFishCombat();
		if (fish == null)
		{
			if (skill.getSkillType() == SkillType.PUMPING)
			{
				// Pumping skill is available only while fishing.
				// player.sendPacket(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
			}
			else if (skill.getSkillType() == SkillType.REELING)
			{
				// Reeling skill is available only while fishing.
				// player.sendPacket(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Weapon weaponItem = player.getActiveWeaponItem();
		final Item weaponInst = creature.getActiveWeaponInstance();
		if ((weaponInst == null) || (weaponItem == null) || (weaponItem.getItemType() != WeaponType.ROD))
		{
			creature.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS));
			return;
		}
		
		int ss = 1;
		int pen = 0;
		if (weaponInst.getChargedFishshot())
		{
			ss = 2;
		}
		
		final double gradebonus = 1 + (weaponItem.getCrystalType() * 0.1);
		int dmg = (int) (skill.getPower() * gradebonus * ss);
		if (player.getSkillLevel(1315) <= (skill.getLevel() - 2)) // 1315 - Fish Expertise Penalty
		{
			player.sendPacket(SystemMessageId.DUE_TO_YOUR_REELING_AND_OR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_50_DAMAGE_PENALTY_WILL_BE_APPLIED);
			pen = 50;
			final int penatlydmg = dmg - pen;
			if (player.isGM())
			{
				player.sendMessage("Dmg w/o penalty = " + dmg);
			}
			dmg = penatlydmg;
		}
		
		if (ss > 1)
		{
			weaponInst.setChargedFishshot(false);
		}
		
		if (skill.getSkillType() == SkillType.REELING) // Realing.
		{
			fish.useRealing(dmg, pen);
		}
		else // Pumping.
		{
			fish.usePomping(dmg, pen);
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
