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
package org.l2jasp.gameserver.model.holders;

import org.l2jasp.gameserver.model.Location;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.actor.Player;

/**
 * @author Asp
 */
public class SummonRequestHolder
{
	private Player _summoner;
	private Location _location;
	private Skill _skill;
	
	public void setTarget(Player summoner, Skill skill)
	{
		_summoner = summoner;
		_location = summoner == null ? null : new Location(summoner.getX(), summoner.getY(), summoner.getZ(), summoner.getHeading());
		_skill = skill;
	}
	
	public Player getSummoner()
	{
		return _summoner;
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
}