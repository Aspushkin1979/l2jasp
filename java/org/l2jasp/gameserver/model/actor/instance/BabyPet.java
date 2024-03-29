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
package org.l2jasp.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import org.l2jasp.commons.threads.ThreadPool;
import org.l2jasp.commons.util.Rnd;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.templates.NpcTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.skill.SkillTargetType;
import org.l2jasp.gameserver.model.skill.SkillType;

/**
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public class BabyPet extends Pet
{
	protected Skill _weakHeal;
	protected Skill _strongHeal;
	private Future<?> _healingTask;
	
	public BabyPet(int objectId, NpcTemplate template, Player owner, Item control)
	{
		super(objectId, template, owner, control);
		
		// look through the skills that this template has and find the weak and strong heal.
		Skill skill1 = null;
		Skill skill2 = null;
		for (Skill skill : getTemplate().getSkills().values())
		{
			// just in case, also allow cp heal and mp recharges to be considered here...you never know ;)
			if (skill.isActive() && (skill.getTargetType() == SkillTargetType.OWNER_PET) && ((skill.getSkillType() == SkillType.HEAL) || (skill.getSkillType() == SkillType.HOT) || (skill.getSkillType() == SkillType.BALANCE_LIFE) || (skill.getSkillType() == SkillType.HEAL_PERCENT) || (skill.getSkillType() == SkillType.HEAL_STATIC) || (skill.getSkillType() == SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == SkillType.COMBATPOINTPERCENTHEAL) || (skill.getSkillType() == SkillType.CPHOT) || (skill.getSkillType() == SkillType.MANAHEAL) || (skill.getSkillType() == SkillType.MANA_BY_LEVEL) || (skill.getSkillType() == SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == SkillType.MANARECHARGE) || (skill.getSkillType() == SkillType.MPHOT)))
			{
				// only consider two skills. If the pet has more, too bad...they won't be used by its AI.
				// for now assign the first two skills in the order they come. Once we have both skills, re-arrange them
				if (skill1 == null)
				{
					skill1 = skill;
				}
				else
				{
					skill2 = skill;
					break;
				}
			}
		}
		
		// process the results. Only store the ID of the skills. The levels are generated on the fly, based on the pet's level!
		if (skill1 != null)
		{
			if (skill2 == null)
			{
				// duplicate so that the same skill will be used in both normal and emergency situations
				_weakHeal = skill1;
				_strongHeal = skill1;
			}
			else if (skill1.getPower() > skill2.getPower()) // arrange the weak and strong skills appropriately
			{
				_weakHeal = skill2;
				_strongHeal = skill1;
			}
			else
			{
				_weakHeal = skill1;
				_strongHeal = skill2;
			}
			
			// start the healing task
			_healingTask = ThreadPool.scheduleAtFixedRate(new Heal(this), 0, 1000);
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_healingTask != null)
		{
			_healingTask.cancel(false);
			_healingTask = null;
		}
		return true;
	}
	
	@Override
	public synchronized void unSummon(Player owner)
	{
		super.unSummon(owner);
		
		if (_healingTask != null)
		{
			_healingTask.cancel(false);
			_healingTask = null;
		}
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		if (_healingTask == null)
		{
			_healingTask = ThreadPool.scheduleAtFixedRate(new Heal(this), 0, 1000);
		}
	}
	
	private class Heal implements Runnable
	{
		private final BabyPet _baby;
		
		public Heal(BabyPet baby)
		{
			_baby = baby;
		}
		
		@Override
		public void run()
		{
			final Player owner = _baby.getOwner();
			
			// if the owner is dead, merely wait for the owner to be resurrected
			// if the pet is still casting from the previous iteration, allow the cast to complete...
			if (!owner.isDead() && !_baby.isDead() && !_baby.isCastingNow() && !_baby.isBetrayed())
			{
				// casting automatically stops any other action (such as autofollow or a move-to).
				// We need to gather the necessary info to restore the previous state.
				final boolean previousFollowStatus = _baby.getFollowStatus();
				
				// if the owner's HP is more than 80%, do nothing.
				// if the owner's HP is very low (less than 20%) have a high chance for strong heal
				// otherwise, have a low chance for weak heal
				if (((owner.getCurrentHp() / owner.getMaxHp()) < 0.2) && (Rnd.get(4) < 3))
				{
					_baby.useMagic(_strongHeal, false, false);
				}
				else if (((owner.getCurrentHp() / owner.getMaxHp()) < 0.8) && (Rnd.get(4) < 1))
				{
					_baby.useMagic(_weakHeal, false, false);
				}
				
				// calling useMagic changes the follow status, if the babypet actually casts
				// (as opposed to failing due some factors, such as too low MP, etc).
				// if the status has actually been changed, revert it. Else, allow the pet to
				// continue whatever it was trying to do.
				// NOTE: This is important since the pet may have been told to attack a target.
				// reverting the follow status will abort this attack! While aborting the attack
				// in order to heal is natural, it is not acceptable to abort the attack on its own,
				// merely because the timer stroke and without taking any other action...
				if (previousFollowStatus != _baby.getFollowStatus())
				{
					setFollowStatus(previousFollowStatus);
				}
			}
		}
	}
}
