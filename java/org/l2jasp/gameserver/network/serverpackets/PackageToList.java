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

import java.util.Map;
import java.util.Map.Entry;

import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: (c) d[dS] d: list size [ d: char ID S: char Name ]
 * @author -Wooden-
 */
public class PackageToList extends ServerPacket
{
	private final Map<Integer, String> _players;
	
	// Lecter : i put a char list here, but i'm unsure these really are Pc. I duno how freight work tho...
	public PackageToList(Map<Integer, String> players)
	{
		_players = players;
	}
	
	@Override
	public void write()
	{
		ServerPackets.PACKAGE_TO_LIST.writeId(this);
		writeInt(_players.size());
		for (Entry<Integer, String> entry : _players.entrySet())
		{
			writeInt(entry.getKey()); // you told me char id, i guess this was object id?
			writeString(entry.getValue());
		}
	}
}
