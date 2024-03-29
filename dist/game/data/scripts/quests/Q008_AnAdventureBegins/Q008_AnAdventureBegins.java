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
package quests.Q008_AnAdventureBegins;

import org.l2jasp.gameserver.enums.Race;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;

public class Q008_AnAdventureBegins extends Quest
{
	// NPCs
	private static final int JASMINE = 30134;
	private static final int ROSELYN = 30355;
	private static final int HARNE = 30144;
	// Items
	private static final int ROSELYN_NOTE = 7573;
	// Rewards
	private static final int SOE_GIRAN = 7559;
	private static final int MARK_TRAVELER = 7570;
	
	public Q008_AnAdventureBegins()
	{
		super(8, "An Adventure Begins");
		registerQuestItems(ROSELYN_NOTE);
		addStartNpc(JASMINE);
		addTalkId(JASMINE, ROSELYN, HARNE);
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
		
		switch (event)
		{
			case "30134-03.htm":
			{
				st.startQuest();
				break;
			}
			case "30355-02.htm":
			{
				st.setCond(2);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(ROSELYN_NOTE, 1);
				break;
			}
			case "30144-02.htm":
			{
				st.setCond(3);
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(ROSELYN_NOTE, 1);
				break;
			}
			case "30134-06.htm":
			{
				st.giveItems(MARK_TRAVELER, 1);
				st.rewardItems(SOE_GIRAN, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
				break;
			}
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
				if ((player.getLevel() >= 3) && (player.getRace() == Race.DARK_ELF))
				{
					htmltext = "30134-02.htm";
				}
				else
				{
					htmltext = "30134-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case JASMINE:
					{
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30134-04.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30134-05.htm";
						}
						break;
					}
					case ROSELYN:
					{
						if (cond == 1)
						{
							htmltext = "30355-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30355-03.htm";
						}
						break;
					}
					case HARNE:
					{
						if (cond == 2)
						{
							htmltext = "30144-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30144-03.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
}