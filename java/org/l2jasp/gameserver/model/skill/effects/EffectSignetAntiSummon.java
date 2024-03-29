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

import org.l2jasp.gameserver.ai.CtrlEvent;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Playable;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.instance.EffectPoint;
import org.l2jasp.gameserver.model.effects.Effect;
import org.l2jasp.gameserver.model.effects.EffectType;
import org.l2jasp.gameserver.model.skill.Env;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

public class EffectSignetAntiSummon extends Effect
{
	private EffectPoint _actor;
	
	public EffectSignetAntiSummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public void onStart()
	{
		_actor = (EffectPoint) getEffected();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == (getTotalCount() - 1))
		{
			return true; // do nothing first time
		}
		final int mpConsume = getSkill().getMpConsume();
		for (Creature creature : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (creature == null)
			{
				continue;
			}
			
			if (creature instanceof Playable)
			{
				final Player owner = (Player) creature;
				if (owner.getPet() != null)
				{
					if (mpConsume > getEffector().getStatus().getCurrentMp())
					{
						getEffector().sendPacket(new SystemMessage(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP));
						return false;
					}
					
					getEffector().reduceCurrentMp(mpConsume);
					
					owner.getPet().unSummon(owner);
					owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
