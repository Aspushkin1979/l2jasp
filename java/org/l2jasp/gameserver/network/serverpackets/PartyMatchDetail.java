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

import org.l2jasp.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author Gnacik
 */
public class PartyMatchDetail extends ServerPacket
{
	private final PartyMatchRoom _room;
	
	public PartyMatchDetail(PartyMatchRoom room)
	{
		_room = room;
	}
	
	@Override
	public void write()
	{
		ServerPackets.PARTY_MATCH_DETAIL.writeId(this);
		writeInt(_room.getId()); // Room ID
		writeInt(_room.getMaxMembers()); // Max Members
		writeInt(_room.getMinLevel()); // Level Min
		writeInt(_room.getMaxLevel()); // Level Max
		writeInt(_room.getLootType()); // Loot Type
		writeInt(_room.getLocation()); // Room Location
		writeString(_room.getTitle()); // Room title
	}
}