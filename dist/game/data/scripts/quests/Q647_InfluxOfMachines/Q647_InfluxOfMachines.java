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
package quests.Q647_InfluxOfMachines;

import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;

public class Q647_InfluxOfMachines extends Quest
{
	// NPC
	private static final int GUTENHAGEN = 32069;
	// Item
	private static final int DESTROYED_GOLEM_SHARD = 8100;
	
	public Q647_InfluxOfMachines()
	{
		super(647, "Influx of Machines");
		registerQuestItems(DESTROYED_GOLEM_SHARD);
		addStartNpc(GUTENHAGEN);
		addTalkId(GUTENHAGEN);
		for (int i = 22052; i < 22079; i++)
		{
			addKillId(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("32069-02.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("32069-06.htm"))
		{
			st.takeItems(DESTROYED_GOLEM_SHARD, -1);
			st.giveItems(getRandom(4963, 4972), 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 46) ? "32069-03.htm" : "32069-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "32069-04.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32069-05.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Player partyMember = getRandomPartyMember(player, npc, 1);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(DESTROYED_GOLEM_SHARD, 1, 500, 300000))
		{
			st.setCond(2);
		}
		
		return null;
	}
}