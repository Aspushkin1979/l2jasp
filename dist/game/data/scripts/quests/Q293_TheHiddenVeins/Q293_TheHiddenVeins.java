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
package quests.Q293_TheHiddenVeins;

import org.l2jasp.Config;
import org.l2jasp.gameserver.enums.Race;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.quest.QuestState;
import org.l2jasp.gameserver.model.quest.State;
import org.l2jasp.gameserver.model.variables.PlayerVariables;

public class Q293_TheHiddenVeins extends Quest
{
	// NPCs
	private static final int FILAUR = 30535;
	private static final int CHINCHIRIN = 30539;
	// Monsters
	private static final int UTUKU_ORC = 20446;
	private static final int UTUKU_ARCHER = 20447;
	private static final int UTUKU_GRUNT = 20448;
	// Items
	private static final int CHRYSOLITE_ORE = 1488;
	private static final int TORN_MAP_FRAGMENT = 1489;
	private static final int HIDDEN_VEIN_MAP = 1490;
	// Reward
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q293_TheHiddenVeins()
	{
		super(293, "The Hidden Veins");
		registerQuestItems(CHRYSOLITE_ORE, TORN_MAP_FRAGMENT, HIDDEN_VEIN_MAP);
		addStartNpc(FILAUR);
		addTalkId(FILAUR, CHINCHIRIN);
		addKillId(UTUKU_ORC, UTUKU_ARCHER, UTUKU_GRUNT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30535-03.htm":
			{
				st.startQuest();
				break;
			}
			case "30535-06.htm":
			{
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
				break;
			}
			case "30539-02.htm":
			{
				if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 4)
				{
					htmltext = "30539-03.htm";
					st.playSound(QuestState.SOUND_ITEMGET);
					st.takeItems(TORN_MAP_FRAGMENT, 4);
					st.giveItems(HIDDEN_VEIN_MAP, 1);
				}
				break;
			}
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
				if (player.getRace() != Race.DWARF)
				{
					htmltext = "30535-00.htm";
				}
				else if (player.getLevel() < 6)
				{
					htmltext = "30535-01.htm";
				}
				else
				{
					htmltext = "30535-02.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getNpcId())
				{
					case FILAUR:
					{
						final int chrysoliteOres = st.getQuestItemsCount(CHRYSOLITE_ORE);
						final int hiddenVeinMaps = st.getQuestItemsCount(HIDDEN_VEIN_MAP);
						if ((chrysoliteOres + hiddenVeinMaps) == 0)
						{
							htmltext = "30535-04.htm";
						}
						else
						{
							if (hiddenVeinMaps > 0)
							{
								if (chrysoliteOres > 0)
								{
									htmltext = "30535-09.htm";
								}
								else
								{
									htmltext = "30535-08.htm";
								}
							}
							else
							{
								htmltext = "30535-05.htm";
							}
							
							st.takeItems(CHRYSOLITE_ORE, -1);
							st.takeItems(HIDDEN_VEIN_MAP, -1);
							int reward = (chrysoliteOres * 10) + (hiddenVeinMaps * 1000);
							if (!Config.ALT_VILLAGES_REPEATABLE_QUEST_REWARD && (chrysoliteOres >= 10))
							{
								reward += 2000;
							}
							
							st.rewardItems(57, reward);
							// Give newbie reward if player is eligible
							int newPlayerRewardsReceived = player.getVariables().getInt(PlayerVariables.NEW_PLAYERS_REWARDS_RECEIVED, 0);
							if (player.isNewbie() && (st.getInt("Reward") == 0) && (newPlayerRewardsReceived < 1))
							{
								st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
								st.playTutorialVoice("tutorial_voice_026");
								st.set("Reward", "1");
								player.getVariables().set(PlayerVariables.NEW_PLAYERS_REWARDS_RECEIVED, ++newPlayerRewardsReceived);
							}
						}
						break;
					}
					case CHINCHIRIN:
					{
						htmltext = "30539-01.htm";
						break;
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
		
		final int chance = getRandom(100);
		if (chance > 50)
		{
			st.dropItemsAlways(CHRYSOLITE_ORE, 1, 0);
		}
		else if (chance < 5)
		{
			st.dropItemsAlways(TORN_MAP_FRAGMENT, 1, 0);
		}
		
		return null;
	}
}