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
package quests.Q319_ScentOfDeath;

import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;

public class Q319_ScentOfDeath extends Quest
{
	// Item
	private static final int ZOMBIE_SKIN = 1045;
	
	public Q319_ScentOfDeath()
	{
		super(319, "Scent of Death");
		registerQuestItems(ZOMBIE_SKIN);
		addStartNpc(30138); // Minaless
		addTalkId(30138);
		addKillId(20015, 20020);
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
		
		if (event.equals("30138-04.htm"))
		{
			st.startQuest();
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 11) ? "30138-02.htm" : "30138-03.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(1))
				{
					htmltext = "30138-05.htm";
				}
				else
				{
					htmltext = "30138-06.htm";
					st.takeItems(ZOMBIE_SKIN, -1);
					st.rewardItems(57, 3350);
					st.rewardItems(1060, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 1);
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(ZOMBIE_SKIN, 1, 5, 200000))
		{
			st.setCond(2);
		}
		
		return null;
	}
}