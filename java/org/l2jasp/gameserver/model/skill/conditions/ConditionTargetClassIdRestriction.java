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
package org.l2jasp.gameserver.model.skill.conditions;

import java.util.List;

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.skill.Env;

public class ConditionTargetClassIdRestriction extends Condition
{
	private final List<Integer> _classIds;
	
	public ConditionTargetClassIdRestriction(List<Integer> classId)
	{
		_classIds = classId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.target instanceof Player))
		{
			return true;
		}
		return !_classIds.contains(((Player) env.target).getClassId().getId());
	}
}
