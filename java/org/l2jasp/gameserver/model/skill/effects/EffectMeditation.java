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
package org.l2jasp.gameserver.model.skill.effects;

import org.l2jasp.gameserver.model.effects.Effect;
import org.l2jasp.gameserver.model.effects.EffectType;
import org.l2jasp.gameserver.model.skill.Env;

public class EffectMeditation extends Effect
{
	public EffectMeditation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MEDITATION;
	}
	
	@Override
	public void onStart()
	{
		_effected.block();
		_effected.setMeditated(true);
	}
	
	@Override
	public void onExit()
	{
		_effected.unblock();
		_effected.setMeditated(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		// stop effect
		return false;
	}
}
