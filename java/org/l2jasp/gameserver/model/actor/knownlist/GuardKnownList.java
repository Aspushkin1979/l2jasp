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
package org.l2jasp.gameserver.model.actor.knownlist;

import org.l2jasp.Config;
import org.l2jasp.gameserver.ai.CreatureAI;
import org.l2jasp.gameserver.ai.CtrlIntention;
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Playable;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.instance.Guard;
import org.l2jasp.gameserver.model.actor.instance.Monster;

public class GuardKnownList extends AttackableKnownList
{
	public GuardKnownList(Guard activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(WorldObject object)
	{
		return addKnownObject(object, null);
	}
	
	@Override
	public boolean addKnownObject(WorldObject object, Creature dropper)
	{
		if (!Config.ALLOW_GUARDS && (object instanceof Monster))
		{
			return false;
		}
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		// Set home location of the Guard (if not already done)
		if (getActiveChar().getHomeX() == 0)
		{
			getActiveChar().getHomeLocation();
		}
		
		if (object instanceof Playable)
		{
			// Check if the object added is a Player that owns Karma.
			final Player player = object.getActingPlayer();
			
			// Set the Guard Intention to AI_INTENTION_ACTIVE
			if (((player.getKarma() > 0) //
				|| (Config.FACTION_SYSTEM_ENABLED && Config.FACTION_GUARDS_ENABLED && ((player.isGood() && Config.FACTION_EVIL_TEAM_NAME.equals(((Npc) getActiveChar()).getTemplate().getFactionId())) || (player.isEvil() && Config.FACTION_GOOD_TEAM_NAME.equals(((Npc) getActiveChar()).getTemplate().getFactionId()))))) //
				&& (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (Config.ALLOW_GUARDS && (object instanceof Monster))
		{
			// Check if the object added is an aggressive Monster
			final Monster mob = (Monster) object;
			
			// Set the Guard Intention to AI_INTENTION_ACTIVE
			if (mob.isAggressive() && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(WorldObject object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Check if the _aggroList of the Guard is Empty
		if (getActiveChar().noTarget())
		{
			// Set the Guard to AI_INTENTION_IDLE
			final CreatureAI ai = getActiveChar().getAI();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}
		
		return true;
	}
	
	@Override
	public Guard getActiveChar()
	{
		return (Guard) super.getActiveChar();
	}
}
