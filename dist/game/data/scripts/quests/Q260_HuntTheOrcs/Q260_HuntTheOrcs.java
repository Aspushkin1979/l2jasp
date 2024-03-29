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
package quests.Q260_HuntTheOrcs;

import org.l2jasp.Config;
import org.l2jasp.gameserver.enums.Race;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;
import org.l2jasp.gameserver.model.variables.PlayerVariables;

public class Q260_HuntTheOrcs extends Quest
{
	// NPC
	private static final int RAYEN = 30221;
	// Monsters
	private static final int KABOO_ORC = 20468;
	private static final int KABOO_ORC_ARCHER = 20469;
	private static final int KABOO_ORC_GRUNT = 20470;
	private static final int KABOO_ORC_FIGHTER = 20471;
	private static final int KABOO_ORC_FIGHTER_LEADER = 20472;
	private static final int KABOO_ORC_FIGHTER_LIEUTENANT = 20473;
	// Items
	private static final int ORC_AMULET = 1114;
	private static final int ORC_NECKLACE = 1115;
	// Newbie Items
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q260_HuntTheOrcs()
	{
		super(260, "Hunt the Orcs");
		registerQuestItems(ORC_AMULET, ORC_NECKLACE);
		addStartNpc(RAYEN);
		addTalkId(RAYEN);
		addKillId(KABOO_ORC, KABOO_ORC_ARCHER, KABOO_ORC_GRUNT, KABOO_ORC_FIGHTER, KABOO_ORC_FIGHTER_LEADER, KABOO_ORC_FIGHTER_LIEUTENANT);
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
		
		if (event.equals("30221-03.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("30221-06.htm"))
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
				if (player.getRace() != Race.ELF)
				{
					htmltext = "30221-00.htm";
				}
				else if (player.getLevel() < 6)
				{
					htmltext = "30221-01.htm";
				}
				else
				{
					htmltext = "30221-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int amulet = st.getQuestItemsCount(ORC_AMULET);
				final int necklace = st.getQuestItemsCount(ORC_NECKLACE);
				
				if ((amulet == 0) && (necklace == 0))
				{
					htmltext = "30221-04.htm";
				}
				else
				{
					htmltext = "30221-05.htm";
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
					int reward = (amulet * 5) + (necklace * 15);
					if (!Config.ALT_VILLAGES_REPEATABLE_QUEST_REWARD && ((amulet + necklace) >= 10))
					{
						reward += 1000;
					}
					
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
		
		switch (npc.getNpcId())
		{
			case KABOO_ORC:
			case KABOO_ORC_GRUNT:
			case KABOO_ORC_ARCHER:
			{
				st.dropItems(ORC_AMULET, 1, 0, 500000);
				break;
			}
			case KABOO_ORC_FIGHTER:
			case KABOO_ORC_FIGHTER_LEADER:
			case KABOO_ORC_FIGHTER_LIEUTENANT:
			{
				st.dropItems(ORC_NECKLACE, 1, 0, 500000);
				break;
			}
		}
		
		return null;
	}
}