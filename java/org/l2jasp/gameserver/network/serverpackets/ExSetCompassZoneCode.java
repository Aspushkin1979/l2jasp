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

/**
 * Format: ch d.
 * @author KenM
 */
public class ExSetCompassZoneCode extends ServerPacket
{
	/** The Constant SIEGEWARZONE1. */
	public static final int SIEGEWARZONE1 = 0x0A;
	/** The Constant SIEGEWARZONE2. */
	public static final int SIEGEWARZONE2 = 0x0B;
	/** The Constant PEACEZONE. */
	public static final int PEACEZONE = 0x0C;
	/** The Constant SEVENSIGNSZONE. */
	public static final int SEVENSIGNSZONE = 0x0D;
	/** The Constant PVPZONE. */
	public static final int PVPZONE = 0x0E;
	/** The Constant GENERALZONE. */
	public static final int GENERALZONE = 0x0F;
	
	/** The _zone type. */
	private final int _zoneType;
	
	/**
	 * Instantiates a new ex set compass zone code.
	 * @param value the value
	 */
	public ExSetCompassZoneCode(int value)
	{
		_zoneType = value;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SET_COMPASS_ZONE_CODE.writeId(this);
		writeInt(_zoneType);
	}
}
