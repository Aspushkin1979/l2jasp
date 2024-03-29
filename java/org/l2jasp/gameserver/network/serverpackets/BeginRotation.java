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

public class BeginRotation extends ServerPacket
{
	private final int _objectId;
	private final int _degree;
	private final int _side;
	private final int _speed;
	
	public BeginRotation(Creature creature, int degree, int side, int speed)
	{
		_objectId = creature.getObjectId();
		_degree = degree;
		_side = side;
		_speed = speed;
	}
	
	@Override
	public void write()
	{
		ServerPackets.BEGIN_ROTATION.writeId(this);
		writeInt(_objectId);
		writeInt(_degree);
		writeInt(_side);
		if (_speed != 0)
		{
			writeInt(_speed);
		}
	}
}
