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
package org.l2jasp.gameserver.model.actor.status;

import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Npc;

public class NpcStatus extends CreatureStatus
{
	public NpcStatus(Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker, boolean awake)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		// Add attackers to npc's attacker list
		if (attacker != null)
		{
			getActiveChar().addAttackerToAttackByList(attacker);
		}
		
		super.reduceHp(value, attacker, awake);
	}
	
	@Override
	public Npc getActiveChar()
	{
		return (Npc) super.getActiveChar();
	}
}
