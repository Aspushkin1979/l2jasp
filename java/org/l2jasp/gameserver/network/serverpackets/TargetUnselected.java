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
 * format dddd sample 0000: 3a 69 08 10 48 02 c1 00 00 f7 56 00 00 89 ea ff :i..H.....V..... 0010: ff 0c b2 d8 61 ....a
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class TargetUnselected extends ServerPacket
{
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	/**
	 * @param creature
	 */
	public TargetUnselected(Creature creature)
	{
		_targetObjId = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
	}
	
	@Override
	public void write()
	{
		ServerPackets.TARGET_UNSELECTED.writeId(this);
		writeInt(_targetObjId);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		// writeInt(_target.getTargetId()); //?? probably not used in client
	}
}
