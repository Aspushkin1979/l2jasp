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

import org.l2jasp.gameserver.model.actor.instance.Boat;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author Maktakien
 */
public class VehicleDeparture extends ServerPacket
{
	private final Boat _boat;
	private final int _speed1;
	private final int _speed2; // rotation
	private final int _x;
	private final int _y;
	private final int _z;
	
	/**
	 * @param boat
	 * @param speed1
	 * @param speed2
	 * @param x
	 * @param y
	 * @param z
	 */
	public VehicleDeparture(Boat boat, int speed1, int speed2, int x, int y, int z)
	{
		_boat = boat;
		_speed1 = speed1;
		_speed2 = speed2;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public void write()
	{
		ServerPackets.VEHICLE_DEPARTURE.writeId(this);
		writeInt(_boat.getObjectId());
		writeInt(_speed1);
		writeInt(_speed2);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
	}
}
