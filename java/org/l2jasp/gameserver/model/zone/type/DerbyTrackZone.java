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
package org.l2jasp.gameserver.model.zone.type;

import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.zone.ZoneId;

/**
 * The Monster Derby Track Zone
 * @author durgus
 */
public class DerbyTrackZone extends PeaceZone
{
	public DerbyTrackZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature instanceof Player)
		{
			creature.setInsideZone(ZoneId.MONSTER_TRACK, true);
		}
		super.onEnter(creature);
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof Player)
		{
			creature.setInsideZone(ZoneId.MONSTER_TRACK, false);
		}
		super.onExit(creature);
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
	{
	}
}
