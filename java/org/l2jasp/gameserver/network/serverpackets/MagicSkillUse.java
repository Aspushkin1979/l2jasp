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

import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.network.ServerPackets;

public class MagicSkillUse extends ServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _targetId;
	private final int _targetx;
	private final int _targety;
	private final int _targetz;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	
	public MagicSkillUse(Creature creature, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		super(64);
		
		_objectId = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		if (target != null)
		{
			_targetId = target.getObjectId();
			_targetx = target.getX();
			_targety = target.getY();
			_targetz = target.getZ();
		}
		else
		{
			_targetId = creature.getTargetId();
			_targetx = creature.getX();
			_targety = creature.getY();
			_targetz = creature.getZ();
		}
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
	}
	
	@Override
	public void write()
	{
		ServerPackets.MAGIC_SKILL_USE.writeId(this);
		writeInt(_objectId);
		writeInt(_targetId);
		writeInt(_skillId);
		writeInt(_skillLevel);
		writeInt(_hitTime);
		writeInt(_reuseDelay);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		// if (_critical) // ?
		// {
		// writeD(1);
		// writeH(0);
		// }
		// else
		// {
		writeInt(0);
		// }
		writeInt(_targetx);
		writeInt(_targety);
		writeInt(_targetz);
	}
}