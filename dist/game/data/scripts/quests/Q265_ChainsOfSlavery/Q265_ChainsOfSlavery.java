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
package quests.Q265_ChainsOfSlavery;

import org.l2jasp.Config;
import org.l2jasp.gameserver.enums.Race;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;
import org.l2jasp.gameserver.model.variables.PlayerVariables;

public class Q265_ChainsOfSlavery extends Quest
{
	// Item
	private static final int SHACKLE = 1368;
	// Newbie Items
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q265_ChainsOfSlavery()
	{
		super(265, "Chains of Slavery");
		registerQuestItems(SHACKLE);
		addStartNpc(30357); // Kristin
		addTalkId(30357);
		addKillId(20004, 20005);
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
		
		if (event.equals("30357-03.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("30357-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getRace() != Race.DARK_ELF)
				{
					htmltext = "30357-00.htm";
				}
				else if (player.getLevel() < 6)
				{
					htmltext = "30357-01.htm";
				}
				else
				{
					htmltext = "30357-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int shackles = st.getQuestItemsCount(SHACKLE);
				if (shackles == 0)
				{
					htmltext = "30357-04.htm";
				}
				else
				{
					int reward = 12 * shackles;
					if (!Config.ALT_VILLAGES_REPEATABLE_QUEST_REWARD && (shackles >= 10))
					{
						reward += 500;
					}
					
					htmltext = "30357-05.htm";
					st.takeItems(SHACKLE, -1);
					st.rewardItems(57, reward);
					// Give newbie reward if player is eligible
					int newPlayerRewardsReceived = player.getVariables().getInt(PlayerVariables.NEW_PLAYERS_REWARDS_RECEIVED, 0);
					if (player.isNewbie() && (st.getInt("Reward") == 0) && (newPlayerRewardsReceived < 1))
					{
						st.showQuestionMark(26);
						st.set("Reward", "1");
						
						if (player.isMageClass())
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
							player.getVariables().set(PlayerVariables.NEW_PLAYERS_REWARDS_RECEIVED, ++newPlayerRewardsReceived);
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
							player.getVariables().set(PlayerVariables.NEW_PLAYERS_REWARDS_RECEIVED, ++newPlayerRewardsReceived);
						}
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.dropItems(SHACKLE, 1, 0, (npc.getNpcId() == 20004) ? 500000 : 600000);
		
		return null;
	}
}