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
package org.l2jasp.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * sample 0000: 8e d8 a8 10 48 10 04 00 00 01 00 00 00 01 00 00 ....H........... 0010: 00 d8 a8 10 48 ....H format ddddd d
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MagicSkillLaunched extends ServerPacket
{
	private final int _objectId;
	private final int _skillId;
	private final int _skillLevel;
	private int _numberOfTargets;
	private List<Creature> _targets;
	private final int _singleTargetId;
	
	public MagicSkillLaunched(Creature creature, int skillId, int skillLevel, List<Creature> targets)
	{
		_objectId = creature.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		if (targets != null)
		{
			_numberOfTargets = targets.size();
			_targets = targets;
		}
		else
		{
			_numberOfTargets = 1;
			_targets = new ArrayList<>();
			_targets.add(creature);
		}
		_singleTargetId = 0;
	}
	
	public MagicSkillLaunched(Creature creature, int skillId, int skillLevel)
	{
		_objectId = creature.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = 1;
		_singleTargetId = creature.getTargetId();
	}
	
	@Override
	public void write()
	{
		ServerPackets.MAGIC_SKILL_LAUNCHED.writeId(this);
		writeInt(_objectId);
		writeInt(_skillId);
		writeInt(_skillLevel);
		writeInt(_numberOfTargets); // also failed or not?
		if ((_singleTargetId != 0) || (_numberOfTargets == 0))
		{
			writeInt(_singleTargetId);
		}
		else
		{
			for (WorldObject target : _targets)
			{
				try
				{
					writeInt(target.getObjectId());
				}
				catch (NullPointerException e)
				{
					writeInt(0); // untested
				}
			}
		}
	}
}
