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
public class VehicleInfo extends ServerPacket
{
	private final Boat _boat;
	
	/**
	 * @param boat
	 */
	public VehicleInfo(Boat boat)
	{
		_boat = boat;
	}
	
	@Override
	public void write()
	{
		ServerPackets.VEHICLE_INFO.writeId(this);
		writeInt(_boat.getObjectId());
		writeInt(_boat.getX());
		writeInt(_boat.getY());
		writeInt(_boat.getZ());
		writeInt(_boat.getHeading());
	}
}
