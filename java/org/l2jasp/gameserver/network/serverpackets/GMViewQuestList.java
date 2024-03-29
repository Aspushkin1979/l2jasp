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

import java.util.List;

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Sh (dd) h (dddd)
 * @author Tempy
 */
public class GMViewQuestList extends ServerPacket
{
	private final Player _player;
	
	public GMViewQuestList(Player player)
	{
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.GM_VIEW_QUEST_LIST.writeId(this);
		writeString(_player.getName());
		final List<Quest> questList = _player.getAllActiveQuests();
		writeShort(questList.size()); // quest count
		for (Quest q : questList)
		{
			writeInt(q.getQuestId());
			final QuestState qs = _player.getQuestState(q.getName());
			if (qs == null)
			{
				writeInt(0);
				continue;
			}
			writeInt(qs.getCond()); // stage of quest progress
		}
	}
}
