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
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: (ch) dc d: character object id c: 1 if won 0 if failed
 * @author -Wooden-
 */
public class ExFishingEnd extends ServerPacket
{
	private final boolean _win;
	Creature _creature;
	
	public ExFishingEnd(boolean win, Player character)
	{
		_win = win;
		_creature = character;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_FISHING_END.writeId(this);
		writeInt(_creature.getObjectId());
		writeByte(_win);
	}
}
