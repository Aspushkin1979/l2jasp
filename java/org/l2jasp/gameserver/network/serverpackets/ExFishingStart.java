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
 * Format (ch)ddddd
 * @author -Wooden-
 */
public class ExFishingStart extends ServerPacket
{
	private final Creature _creature;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _fishType;
	@SuppressWarnings("unused")
	private final boolean _isNightLure;
	
	public ExFishingStart(Creature creature, int fishType, int x, int y, int z, boolean isNightLure)
	{
		_creature = creature;
		_fishType = fishType;
		_x = x;
		_y = y;
		_z = z;
		_isNightLure = isNightLure;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_FISHING_START.writeId(this);
		writeInt(_creature.getObjectId());
		writeInt(_fishType); // fish type
		writeInt(_x); // x poisson
		writeInt(_y); // y poisson
		writeInt(_z); // z poisson
		writeByte(0); // night lure
		writeByte(0); // ??
		writeByte((_fishType >= 7) && (_fishType <= 9)); // 0 = day lure 1 = night lure
		writeByte(0);
	}
}
