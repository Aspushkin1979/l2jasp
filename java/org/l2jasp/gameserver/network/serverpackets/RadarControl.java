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

public class RadarControl extends ServerPacket
{
	private final int _showRadar;
	private final int _type;
	private final int _x;
	private final int _y;
	private final int _z;
	
	/**
	 * 0xEB RadarControl ddddd
	 * @param showRadar
	 * @param type
	 * @param x
	 * @param y
	 * @param z
	 */
	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		_type = type; // radar type??
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public void write()
	{
		ServerPackets.RADAR_CONTROL.writeId(this);
		writeInt(_showRadar);
		writeInt(_type); // maybe type
		writeInt(_x); // x
		writeInt(_y); // y
		writeInt(_z); // z
	}
}
