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

import org.l2jasp.gameserver.network.ServerPackets;

public class Ride extends ServerPacket
{
	public static final int ACTION_MOUNT = 1;
	public static final int ACTION_DISMOUNT = 0;
	
	private final int _id;
	private final int _bRide;
	private int _rideType;
	private final int _rideClassID;
	
	public Ride(int id, int action, int rideClassId)
	{
		_id = id; // charobjectID
		_bRide = action; // 1 for mount ; 2 for dismount
		_rideClassID = rideClassId + 1000000; // npcID
		if ((rideClassId == 12526) || // wind strider
			(rideClassId == 12527) || // star strider
			(rideClassId == 12528)) // twilight strider
		{
			_rideType = 1; // 1 for Strider ; 2 for wyvern
		}
		else if (rideClassId == 12621) // wyvern
		{
			_rideType = 2; // 1 for Strider ; 2 for wyvern
		}
	}
	
	public int getMountType()
	{
		return _rideType;
	}
	
	@Override
	public void write()
	{
		ServerPackets.RIDE.writeId(this);
		writeInt(_id);
		writeInt(_bRide);
		writeInt(_rideType);
		writeInt(_rideClassID);
	}
}
