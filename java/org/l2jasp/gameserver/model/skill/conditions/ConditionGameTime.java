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

import org.l2jasp.gameserver.model.skill.Env;
import org.l2jasp.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author mkizub
 */
public class ConditionGameTime extends Condition
{
	private final boolean _required;
	
	public ConditionGameTime(boolean required)
	{
		_required = required;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return GameTimeTaskManager.getInstance().isNight() == _required;
	}
}
