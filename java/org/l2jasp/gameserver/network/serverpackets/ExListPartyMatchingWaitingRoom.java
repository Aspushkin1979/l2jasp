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

import java.util.ArrayList;
import java.util.List;

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jasp.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jasp.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends ServerPacket
{
	private final Player _player;
	@SuppressWarnings("unused")
	private final int _page;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _mode;
	private final List<Player> _members;
	
	public ExListPartyMatchingWaitingRoom(Player player, int page, int minLevel, int maxLevel, int mode)
	{
		_player = player;
		_page = page;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_mode = mode;
		_members = new ArrayList<>();
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_LIST_PARTY_MATCHING_WAITING_ROOM.writeId(this);
		// If the mode is 0 and the activeChar isn't the PartyRoom leader, return an empty list.
		if (_mode == 0)
		{
			// Retrieve the activeChar PartyMatchRoom
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_player.getPartyRoom());
			if ((room != null) && (room.getOwner() != null) && !room.getOwner().equals(_player))
			{
				writeInt(0);
				writeInt(0);
				return;
			}
		}
		
		for (Player cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			// Don't add yourself in the list
			if ((cha == null) || (cha == _player))
			{
				continue;
			}
			if (!cha.isPartyWaiting())
			{
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}
			if ((cha.getLevel() < _minLevel) || (cha.getLevel() > _maxLevel))
			{
				continue;
			}
			_members.add(cha);
		}
		int count = 0;
		final int size = _members.size();
		writeInt(1);
		writeInt(size);
		while (size > count)
		{
			writeString(_members.get(count).getName());
			writeInt(_members.get(count).getActiveClass());
			writeInt(_members.get(count).getLevel());
			count++;
		}
	}
}