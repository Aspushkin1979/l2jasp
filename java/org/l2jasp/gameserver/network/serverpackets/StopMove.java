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

/**
 * format ddddd sample 0000: 59 1a 95 20 48 44 17 02 00 03 f0 fc ff 98 f1 ff Y.. HD.......... 0010: ff c1 1a 00 00 .....
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class StopMove extends ServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	
	public StopMove(Creature creature)
	{
		this(creature.getObjectId(), creature.getX(), creature.getY(), creature.getZ(), creature.getHeading());
	}
	
	/**
	 * @param objectId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 */
	public StopMove(int objectId, int x, int y, int z, int heading)
	{
		_objectId = objectId;
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
	@Override
	public void write()
	{
		ServerPackets.STOP_MOVE.writeId(this);
		writeInt(_objectId);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		writeInt(_heading);
	}
}