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

import org.l2jasp.commons.util.Rnd;
import org.l2jasp.gameserver.handler.ISkillHandler;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.skill.SkillType;
import org.l2jasp.gameserver.network.serverpackets.ValidateLocation;

public class GetPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.GET_PLAYER
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (creature.isAlikeDead())
		{
			return;
		}
		
		for (WorldObject target : targets)
		{
			if (target instanceof Player)
			{
				final Player trg = (Player) target;
				if (trg.isAlikeDead())
				{
					continue;
				}
				
				trg.setXYZ(creature.getX() + Rnd.get(-10, 10), creature.getY() + Rnd.get(-10, 10), creature.getZ());
				trg.sendPacket(new ValidateLocation(trg));
			}
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}