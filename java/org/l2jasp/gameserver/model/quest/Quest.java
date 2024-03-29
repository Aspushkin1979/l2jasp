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
package org.l2jasp.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jasp.Config;
import org.l2jasp.commons.database.DatabaseFactory;
import org.l2jasp.commons.util.Rnd;
import org.l2jasp.gameserver.cache.HtmCache;
import org.l2jasp.gameserver.data.ItemTable;
import org.l2jasp.gameserver.data.sql.NpcTable;
import org.l2jasp.gameserver.instancemanager.QuestManager;
import org.l2jasp.gameserver.model.Location;
import org.l2jasp.gameserver.model.Party;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.templates.NpcTemplate;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.model.clan.ClanMember;
import org.l2jasp.gameserver.model.holders.ItemHolder;
import org.l2jasp.gameserver.model.item.ItemTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jasp.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jasp.gameserver.network.serverpackets.ItemList;
import org.l2jasp.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jasp.gameserver.network.serverpackets.StatusUpdate;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.scripting.ManagedScript;

/**
 * @author Luis Arias
 */
public class Quest extends ManagedScript
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class.getName());
	
	private static final String HTML_NONE_AVAILABLE = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String HTML_ALREADY_COMPLETED = "<html><body>This quest has already been completed.</body></html>";
	
	/** Map containing events from String value of the event */
	private static Map<String, Quest> _allEvents = new HashMap<>();
	/** Map containing lists of timers from the name of the timer */
	private final Map<String, List<QuestTimer>> _questTimers = new HashMap<>();
	
	private final int _questId;
	private final String _prefixPath; // used only for admin_quest_reload
	private final String _descr;
	private final byte _initialState = State.CREATED;
	protected int[] questItemIds = null;
	
	// Dimensional Diamond Rewards by Class for 2nd class transfer quest (35)
	protected static final Map<Integer, Integer> DF_REWARD_35 = new HashMap<>();
	static
	{
		DF_REWARD_35.put(1, 61);
		DF_REWARD_35.put(4, 45);
		DF_REWARD_35.put(7, 128);
		DF_REWARD_35.put(11, 168);
		DF_REWARD_35.put(15, 49);
		DF_REWARD_35.put(19, 61);
		DF_REWARD_35.put(22, 128);
		DF_REWARD_35.put(26, 168);
		DF_REWARD_35.put(29, 49);
		DF_REWARD_35.put(32, 61);
		DF_REWARD_35.put(35, 128);
		DF_REWARD_35.put(39, 168);
		DF_REWARD_35.put(42, 49);
		DF_REWARD_35.put(45, 61);
		DF_REWARD_35.put(47, 61);
		DF_REWARD_35.put(50, 49);
		DF_REWARD_35.put(54, 85);
		DF_REWARD_35.put(56, 85);
	}
	
	// Dimensional Diamond Rewards by Race for 2nd class transfer quest (37)
	protected static final Map<Integer, Integer> DF_REWARD_37 = new HashMap<>();
	static
	{
		DF_REWARD_37.put(0, 96);
		DF_REWARD_37.put(1, 102);
		DF_REWARD_37.put(2, 98);
		DF_REWARD_37.put(3, 109);
		DF_REWARD_37.put(4, 50);
	}
	
	// Dimensional Diamond Rewards by Class for 2nd class transfer quest (39)
	protected static final Map<Integer, Integer> DF_REWARD_39 = new HashMap<>();
	static
	{
		DF_REWARD_39.put(1, 72);
		DF_REWARD_39.put(4, 104);
		DF_REWARD_39.put(7, 96);
		DF_REWARD_39.put(11, 122);
		DF_REWARD_39.put(15, 60);
		DF_REWARD_39.put(19, 72);
		DF_REWARD_39.put(22, 96);
		DF_REWARD_39.put(26, 122);
		DF_REWARD_39.put(29, 45);
		DF_REWARD_39.put(32, 104);
		DF_REWARD_39.put(35, 96);
		DF_REWARD_39.put(39, 122);
		DF_REWARD_39.put(42, 60);
		DF_REWARD_39.put(45, 64);
		DF_REWARD_39.put(47, 72);
		DF_REWARD_39.put(50, 92);
		DF_REWARD_39.put(54, 82);
		DF_REWARD_39.put(56, 23);
	}
	
	/**
	 * Return collection view of the values contains in the allEventS
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents()
	{
		return _allEvents.values();
	}
	
	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 * @param questId : int pointing out the ID of the quest
	 * @param descr : String for the description of the quest
	 */
	public Quest(int questId, String descr)
	{
		_questId = questId;
		_descr = descr;
		
		// Given the quest instance, create a string representing the path and questName like a simplified version of a canonical class name.
		// That is, if a script is in DATAPACK_PATH/scripts/quests/abc the result will be quests.abc
		// Similarly, for a script in DATAPACK_PATH/scripts/ai/individual/myClass.py the result will be ai.individual.myClass
		// All quests are to be indexed, processed, and reloaded by this form of pathname.
		final StringBuilder temp = new StringBuilder(getClass().getCanonicalName());
		temp.delete(0, temp.indexOf(".scripts.") + 9);
		temp.delete(temp.indexOf(getClass().getSimpleName()), temp.length());
		_prefixPath = temp.toString();
		
		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(Quest.this);
		}
		else
		{
			_allEvents.put(getName(), this);
		}
		
		initGlobalData();
	}
	
	/**
	 * The function init_LoadGlobalData is, by default, called by the constructor of all quests. Children of this class can implement this function in order to define what variables to load and what structures to save them in. By default, nothing is loaded.
	 */
	protected void initGlobalData()
	{
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager. Children of this class can implement this function in order to convert their structures into <var, value> tuples and make calls to save them to the database, if needed. By default, nothing is
	 * saved.
	 */
	public void saveGlobalData()
	{
	}
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	public int getQuestId()
	{
		return _questId;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param player
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(Player player)
	{
		final QuestState qs = new QuestState(this, player, _initialState);
		createQuestInDb(qs);
		return qs;
	}
	
	/**
	 * Return initial state of the quest
	 * @return State
	 */
	public byte getInitialState()
	{
		return _initialState;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return getClass().getSimpleName();
	}
	
	/**
	 * Return name of the prefix path for the quest, down to the last "." For example "quests." or "ai.individual."
	 * @return String
	 */
	public String getPrefixPath()
	{
		return _prefixPath;
	}
	
	/**
	 * Return description of the quest
	 * @return String
	 */
	public String getDescr()
	{
		return _descr;
	}
	
	/**
	 * @param player : The player instance to check.
	 * @return true if the given player got an online clan member sponsor in a 1500 radius range.
	 */
	public static boolean getSponsor(Player player)
	{
		// Player hasn't a sponsor.
		final int sponsorId = player.getSponsor();
		if (sponsorId == 0)
		{
			return false;
		}
		
		// Player hasn't a clan.
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return false;
		}
		
		// Retrieve sponsor clan member object.
		final ClanMember member = clan.getClanMember(sponsorId);
		if ((member != null) && member.isOnline())
		{
			// The sponsor is online, retrieve player instance and check distance.
			final Player sponsor = member.getPlayer();
			if ((sponsor != null) && player.isInsideRadius3D(sponsor, Config.ALT_PARTY_RANGE))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param player : The player instance to check.
	 * @return the apprentice of the given player. He must be online, and in a 1500 radius range.
	 */
	public static Player getApprentice(Player player)
	{
		// Player hasn't an apprentice.
		final int apprenticeId = player.getApprentice();
		if (apprenticeId == 0)
		{
			return null;
		}
		
		// Player hasn't a clan.
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return null;
		}
		
		// Retrieve apprentice clan member object.
		final ClanMember member = clan.getClanMember(apprenticeId);
		if ((member != null) && member.isOnline())
		{
			// The apprentice is online, retrieve player instance and check distance.
			final Player academic = member.getPlayer();
			if ((academic != null) && player.isInsideRadius3D(academic, Config.ALT_PARTY_RANGE))
			{
				return academic;
			}
		}
		
		return null;
	}
	
	/**
	 * Add a timer to the quest (if it doesn't exist already) and start it.
	 * @param name the name of the timer (also passed back as "event" in {@link #onAdvEvent(String, Npc, Player)})
	 * @param time time in ms for when to fire the timer
	 * @param npc the NPC associated with this timer (can be null)
	 * @param player the player associated with this timer (can be null)
	 * @see #startQuestTimer(String, long, Npc, Player, boolean)
	 */
	public void startQuestTimer(String name, long time, Npc npc, Player player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Gets the quest timers.
	 * @return the quest timers
	 */
	public Map<String, List<QuestTimer>> getQuestTimers()
	{
		return _questTimers;
	}
	
	/**
	 * Add a timer to the quest (if it doesn't exist already) and start it.
	 * @param name the name of the timer (also passed back as "event" in {@link #onAdvEvent(String, Npc, Player)})
	 * @param time time in ms for when to fire the timer
	 * @param npc the NPC associated with this timer (can be null)
	 * @param player the player associated with this timer (can be null)
	 * @param repeating indicates whether the timer is repeatable or one-time.<br>
	 *            If {@code true}, the task is repeated every {@code time} milliseconds until explicitly stopped.
	 */
	public void startQuestTimer(String name, long time, Npc npc, Player player, boolean repeating)
	{
		if (name == null)
		{
			return;
		}
		
		synchronized (_questTimers)
		{
			if (!_questTimers.containsKey(name))
			{
				_questTimers.put(name, new CopyOnWriteArrayList<>());
			}
			
			// If there exists a timer with this name, allow the timer only if the [npc, player] set is unique nulls act as wildcards.
			if (getQuestTimer(name, npc, player) == null)
			{
				_questTimers.get(name).add(new QuestTimer(this, name, time, npc, player, repeating));
			}
		}
	}
	
	/**
	 * Get a quest timer that matches the provided name and parameters.
	 * @param name the name of the quest timer to get
	 * @param npc the NPC associated with the quest timer to get
	 * @param player the player associated with the quest timer to get
	 * @return the quest timer that matches the specified parameters or {@code null} if nothing was found
	 */
	public QuestTimer getQuestTimer(String name, Npc npc, Player player)
	{
		if (name == null)
		{
			return null;
		}
		
		final List<QuestTimer> timers = _questTimers.get(name);
		if ((timers == null) || timers.isEmpty())
		{
			return null;
		}
		
		for (QuestTimer timer : timers)
		{
			if ((timer != null) && timer.equals(this, name, npc, player))
			{
				return timer;
			}
		}
		
		return null;
	}
	
	/**
	 * Cancel all quest timers with the specified name.
	 * @param name the name of the quest timers to cancel
	 */
	public void cancelQuestTimers(String name)
	{
		if (name == null)
		{
			return;
		}
		
		final List<QuestTimer> timers = _questTimers.get(name);
		if ((timers == null) || timers.isEmpty())
		{
			return;
		}
		
		for (QuestTimer timer : timers)
		{
			if (timer != null)
			{
				timer.cancel();
			}
		}
		
		timers.clear();
	}
	
	/**
	 * Cancel the quest timer that matches the specified name and parameters.
	 * @param name the name of the quest timer to cancel
	 * @param npc the NPC associated with the quest timer to cancel
	 * @param player the player associated with the quest timer to cancel
	 */
	public void cancelQuestTimer(String name, Npc npc, Player player)
	{
		if (name == null)
		{
			return;
		}
		
		final List<QuestTimer> timers = _questTimers.get(name);
		if ((timers == null) || timers.isEmpty())
		{
			return;
		}
		
		for (QuestTimer timer : timers)
		{
			if ((timer != null) && timer.equals(this, name, npc, player))
			{
				timer.cancel();
			}
		}
	}
	
	/**
	 * Remove a quest timer from the list of all timers.<br>
	 * Note: does not stop the timer itself!
	 * @param timer the {@link QuestState} object to remove
	 */
	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer == null)
		{
			return;
		}
		
		final List<QuestTimer> timers = _questTimers.get(timer.toString());
		if (timers != null)
		{
			timers.remove(timer);
		}
	}
	
	// These are methods to call within the core to call the quest events.
	
	public boolean notifyAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public boolean notifyDeath(Creature killer, Creature victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		return showResult(qs.getPlayer(), res);
	}
	
	public boolean notifyEvent(String event, Npc npc, Player player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public boolean notifyKill(Npc npc, Player killer, boolean isPet)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (Exception e)
		{
			return showError(killer, e);
		}
		return showResult(killer, res);
	}
	
	public boolean notifyTalk(Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		
		return showResult(qs.getPlayer(), res);
	}
	
	// override the default NPC dialogs when a quest defines this for the given NPC
	public boolean notifyFirstTalk(Npc npc, Player player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		
		player.setLastQuestNpcObject(npc.getObjectId());
		
		// if the quest returns text to display, display it. Otherwise, use the default npc text.
		if ((res != null) && (res.length() > 0))
		{
			return showResult(player, res);
		}
		
		npc.showChatWindow(player);
		
		return true;
	}
	
	public boolean notifySkillUse(Npc npc, Player caster, Skill skill)
	{
		String res = null;
		try
		{
			res = onSkillUse(npc, caster, skill);
		}
		catch (Exception e)
		{
			return showError(caster, e);
		}
		return showResult(caster, res);
	}
	
	public boolean notifySpellFinished(Npc npc, Player player, Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(npc, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public boolean notifyFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public boolean notifyAggroRangeEnter(Npc npc, Player player, boolean isPet)
	{
		String res = null;
		try
		{
			res = onAggroRangeEnter(npc, player, isPet);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public boolean notifySpawn(Npc npc)
	{
		String res = null;
		try
		{
			res = onSpawn(npc);
		}
		catch (Exception e)
		{
			return showError(npc, e);
		}
		return showResult(npc, res);
	}
	
	// these are methods that java calls to invoke scripts
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		return null;
	}
	
	public String onDeath(Creature killer, Creature victim, QuestState qs)
	{
		if (killer instanceof Npc)
		{
			return onAdvEvent("", (Npc) killer, qs.getPlayer());
		}
		return onAdvEvent("", null, qs.getPlayer());
	}
	
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		// if not overriden by a subclass, then default to the returned value of the simpler (and older) onEvent override
		// if the player has a state, use it as parameter in the next call, else return null
		final QuestState qs = player.getQuestState(getName());
		if (qs != null)
		{
			return onEvent(event, qs);
		}
		
		return null;
	}
	
	public void sendDlgMessage(String text, Player player)
	{
		player.dialog = this;
		final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId());
		dlg.addString(text);
		player.sendPacket(dlg);
	}
	
	public void onDlgAnswer(Player player)
	{
	}
	
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		return null;
	}
	
	public String onTalk(Npc npc, Player talker)
	{
		return null;
	}
	
	public String onFirstTalk(Npc npc, Player player)
	{
		return null;
	}
	
	public String onSkillUse(Npc npc, Player caster, Skill skill)
	{
		return null;
	}
	
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		return null;
	}
	
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		return null;
	}
	
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet)
	{
		return null;
	}
	
	public String onSpawn(Npc npc)
	{
		return null;
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param object
	 * @param t : Throwable
	 * @return boolean
	 */
	public boolean showError(Creature object, Throwable t)
	{
		LOGGER.log(Level.WARNING, getScriptFile().toAbsolutePath().toString(), t);
		if (t.getMessage() == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": " + t.getMessage());
		}
		if ((object != null) && object.isPlayer() && object.getActingPlayer().getAccessLevel().isGm())
		{
			final String res = "<html><body><title>Script error</title>" + t.getMessage() + "</body></html>";
			return showResult(object, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<br>
	 * <u><i>Concept:</i></u><br>
	 * 3 cases are managed according to the value of the parameter "res" :<br>
	 * <li><u>"res" ends with string ".html" :</u> an HTML is opened in order to be shown in a dialog box</li>
	 * <li><u>"res" starts with "<html>" :</u> the message hold in "res" is shown in a dialog box</li>
	 * <li><u>otherwise :</u> the message hold in "res" is shown in chat box</li><br>
	 * @param object
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	private boolean showResult(Creature object, String res)
	{
		if (res == null)
		{
			return true;
		}
		
		if (object instanceof Player)
		{
			final Player player = (Player) object;
			if (res.endsWith(".htm"))
			{
				showHtmlFile(player, res);
			}
			else if (res.startsWith("<html>"))
			{
				final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(res);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			else
			{
				player.sendMessage(res);
			}
		}
		
		return false;
	}
	
	/**
	 * Add the quest to the NPC's startQuest
	 * @param npcId
	 * @return NpcTemplate : Start NPC
	 */
	public NpcTemplate addStartNpc(int npcId)
	{
		return addEventId(npcId, EventType.QUEST_START);
	}
	
	public void addStartNpc(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, EventType.QUEST_START);
		}
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcId
	 * @return NpcTemplate : Start NPC
	 */
	public NpcTemplate addFirstTalkId(int npcId)
	{
		return addEventId(npcId, EventType.NPC_FIRST_TALK);
	}
	
	public void addFirstTalkId(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, EventType.NPC_FIRST_TALK);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.
	 * @param attackId
	 * @return int : attackId
	 */
	public NpcTemplate addAttackId(int attackId)
	{
		return addEventId(attackId, EventType.ON_ATTACK);
	}
	
	public void addAttackId(int... attackIds)
	{
		for (int attackId : attackIds)
		{
			addEventId(attackId, EventType.ON_ATTACK);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.
	 * @param killId
	 * @return int : killId
	 */
	public NpcTemplate addKillId(int killId)
	{
		return addEventId(killId, EventType.ON_KILL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.
	 * @param killIds A serie of ids.
	 */
	public void addKillId(int... killIds)
	{
		for (int killId : killIds)
		{
			addEventId(killId, EventType.ON_KILL);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.
	 * @param killIds A serie of ids.
	 */
	public void addKillId(Collection<Integer> killIds)
	{
		for (int killId : killIds)
		{
			addEventId(killId, EventType.ON_KILL);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.
	 * @param talkId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addTalkId(int talkId)
	{
		return addEventId(talkId, EventType.QUEST_TALK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.
	 * @param talkIds : A serie of ids.
	 */
	public void addTalkId(int... talkIds)
	{
		for (int talkId : talkIds)
		{
			addEventId(talkId, EventType.QUEST_TALK);
		}
	}
	
	public NpcTemplate addFactionCallId(int npcId)
	{
		return addEventId(npcId, EventType.ON_FACTION_CALL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-Use Events.
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addSkillUseId(int npcId)
	{
		return addEventId(npcId, EventType.ON_SKILL_USE);
	}
	
	public void addSkillUseId(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, EventType.ON_SKILL_USE);
		}
	}
	
	public NpcTemplate addSpellFinishedId(int npcId)
	{
		return addEventId(npcId, EventType.ON_SPELL_FINISHED);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.
	 * @param npcId ID of the NPC
	 * @return int ID of the NPC
	 */
	public NpcTemplate addAggroRangeEnterId(int npcId)
	{
		return addEventId(npcId, EventType.ON_AGGRO_RANGE_ENTER);
	}
	
	public void addAggroRangeEnterId(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, EventType.ON_AGGRO_RANGE_ENTER);
		}
	}
	
	public NpcTemplate addSpawnId(int npcId)
	{
		return addEventId(npcId, EventType.ON_SPAWN);
	}
	
	public void addSpawnId(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, EventType.ON_SPAWN);
		}
	}
	
	/**
	 * @return default html page "You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements."
	 */
	public static String getNoQuestMsg()
	{
		return HTML_NONE_AVAILABLE;
	}
	
	/**
	 * @return default html page "This quest has already been completed."
	 */
	public static String getAlreadyCompletedMsg()
	{
		return HTML_ALREADY_COMPLETED;
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
	 * @param npcId : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public NpcTemplate addEventId(int npcId, EventType eventType)
	{
		try
		{
			final NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			return t;
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
			return null;
		}
	}
	
	/**
	 * Add quests to the Player of the player.<br>
	 * <u><i>Action : </u></i><br>
	 * Add state of quests, drops and variables for quests in the HashMap _quest of Player
	 * @param player : Player who is entering the world
	 */
	public static void playerEnter(Player player)
	{
		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			
			final PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
			final PreparedStatement invalidQuestDataVar = con.prepareStatement("delete FROM character_quests WHERE char_id=? and name=? and var=?");
			statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				// Get ID of the quest and ID of its state
				final String questId = rs.getString("name");
				final String statename = rs.getString("value");
				
				// Search quest associated with the ID
				final Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null)
				{
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, State.getStateId(statename));
			}
			
			rs.close();
			invalidQuestData.close();
			statement.close();
			statement = null;
			rs = null;
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=? AND var<>?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				final String questId = rs.getString("name");
				final String var = rs.getString("var");
				final String value = rs.getString("value");
				
				// Get the QuestState saved in the loop before
				final QuestState qs = player.getQuestState(questId);
				if (qs == null)
				{
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questId);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
			}
			
			rs.close();
			invalidQuestDataVar.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not insert char quest: " + e);
		}
		
		// events
		for (String name : _allEvents.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs : QuestState pointing out the state of the quest
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			updateQuestVarInDb(qs, var, value);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<br>
	 * <u><i>Actions :</i></u><br>
	 * The selection of the right record is made with :
	 * <li>char_id = qs.getPlayer().getObjectID()</li>
	 * <li>name = qs.getQuest().getName()</li>
	 * <li>var = var</li><br>
	 * The modification made is :
	 * <li>value = parameter value</li><br>
	 * @param qs : Quest State
	 * @param var : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?");
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not update char quest: " + e);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not delete char quest: " + e);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not delete char quest: " + e);
		}
	}
	
	/**
	 * Create a record in database for quest.<br>
	 * <u><i>Actions :</i></u><br>
	 * Use fucntion createQuestVarInDb() with following parameters :<br>
	 * <li>QuestState : parameter sq that puts in fields of database :
	 * <UL type="square">
	 * <li>char_id : ID of the player</li>
	 * <li>name : name of the quest</li>
	 * </ul>
	 * </li>
	 * <li>var : string "&lt;state&gt;" as the name of the variable for the quest</li>
	 * <li>val : string corresponding at the ID of the state (in fact, initial state)</li><br>
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	/**
	 * Update informations regarding quest in database.<br>
	 * <u><i>Actions :</i></u><br>
	 * <li>Get ID state of the quest recorded in object qs</li>
	 * <li>Test if quest is completed. If true, add a star (*) before the ID state</li>
	 * <li>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</li><br>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		final String val = State.getStateName(qs.getState());
		updateQuestVarInDb(qs, "<state>", val);
	}
	
	/**
	 * Auxiliary function for party quests. Checks the player's condition. Player member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a Npc to compare distance
	 * @param cond : an integer specifying a quest condition that must be satisfied for a party member to be considered.
	 * @return QuestState : The QuestState of that player.
	 */
	public QuestState checkPlayerCondition(Player player, Npc npc, int cond)
	{
		// No valid player or npc instance is passed, there is nothing to check.
		if ((player == null) || (npc == null))
		{
			return null;
		}
		
		// Check player's quest conditions.
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return null;
		}
		
		// Condition has correct value?
		if (!qs.isCond(cond))
		{
			return null;
		}
		
		// Player is in range?
		if (!player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			return null;
		}
		
		return qs;
	}
	
	/**
	 * Auxiliary function for party quests. Checks the player's condition. Player member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a Npc to compare distance
	 * @param state : the state in which the party member's QuestState must be in order to be considered.
	 * @return QuestState : The QuestState of that player.
	 */
	public QuestState checkPlayerState(Player player, Npc npc, byte state)
	{
		// No valid player or npc instance is passed, there is nothing to check.
		if ((player == null) || (npc == null))
		{
			return null;
		}
		
		// Check player's quest conditions.
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return null;
		}
		
		// State correct?
		if (qs.getState() != state)
		{
			return null;
		}
		
		// Player is in range?
		if (!player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			return null;
		}
		
		return qs;
	}
	
	// returns a random party member's Player for the passed player's party
	// returns the passed player if he has no party.
	public Player getRandomPartyMember(Player player)
	{
		// NPE prevention. If the player is null, there is nothing to return
		if (player == null)
		{
			return null;
		}
		
		if ((player.getParty() == null) || player.getParty().getPartyMembers().isEmpty())
		{
			return player;
		}
		
		final Party party = player.getParty();
		return party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player the instance of a player whose party is to be searched
	 * @param value the value of the "cond" variable that must be matched
	 * @return Player: Player for a random party member that matches the specified condition, or null if no match.
	 */
	public Player getRandomPartyMember(Player player, String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a Npc to compare distance
	 * @param cond : an integer specifying a quest condition that must be satisfied for a party member to be considered.
	 * @return List<Player> : List of party members that matches the specified condition, empty list if none matches. If the var is null, empty list is returned (i.e. no condition is applied). The party member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance
	 *         condition is ignored.
	 */
	public List<Player> getPartyMembers(Player player, Npc npc, int cond)
	{
		if (player == null)
		{
			return Collections.emptyList();
		}
		
		final Party party = player.getParty();
		if (party == null)
		{
			return (checkPlayerCondition(player, npc, cond) != null) ? Arrays.asList(player) : Collections.emptyList();
		}
		
		final List<Player> result = new ArrayList<>();
		for (Player member : party.getPartyMembers())
		{
			if (checkPlayerCondition(member, npc, cond) != null)
			{
				result.add(member);
			}
		}
		
		return result;
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a Npc to compare distance
	 * @param cond : an integer specifying a quest condition that must be satisfied for a party member to be considered.
	 * @return Player : Player for a random party member that matches the specified condition, or null if no match. If the cond is null, null is returned (i.e. no condition is applied). The party member must be within 1500 distance from the npc. If npc is null, distance condition is ignored.
	 */
	public Player getRandomPartyMember(Player player, Npc npc, int cond)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
		{
			return null;
		}
		
		// Return random candidate.
		final List<Player> members = getPartyMembers(player, npc, cond);
		if (members.isEmpty())
		{
			final QuestState qs = player.getQuestState(getName());
			if ((qs != null) && qs.isCond(cond))
			{
				return player; // match
			}
			return null; // no match
		}
		return members.get(Rnd.get(members.size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player the instance of a player whose party is to be searched
	 * @param variable a tuple specifying a quest condition that must be satisfied for a party member to be considered.
	 * @param value
	 * @return Player: Player for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied). The party member must be within 1500 distance from the target of the reference player, or if no
	 *         target exists, 1500 distance from the player itself.
	 */
	public Player getRandomPartyMember(Player player, String variable, String value)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// for null var condition, return any random party member.
		if (variable == null)
		{
			return getRandomPartyMember(player);
		}
		
		// Normal cases, if the player is not in a party, check the player's state.
		final Party party = player.getParty();
		
		// If this player is not in a party, just check if this player instance matches the conditions itself.
		if ((party == null) || party.getPartyMembers().isEmpty())
		{
			final QuestState qs = player.getQuestState(getName());
			if (qs != null)
			{
				final Object sVar = qs.get(variable);
				if ((sVar != null) && ((String) sVar).equalsIgnoreCase(value))
				{
					return player; // match
				}
			}
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly including this player)
		final List<Player> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		WorldObject target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (Player partyMember : party.getPartyMembers())
		{
			final QuestState qs = partyMember.getQuestState(getName());
			if (qs != null)
			{
				final Object sVar = qs.get(variable);
				if ((sVar != null) && ((String) sVar).equalsIgnoreCase(value) && partyMember.isInsideRadius3D(target, Config.ALT_PARTY_RANGE))
				{
					candidates.add(partyMember);
				}
			}
		}
		
		// if there was no match, return null...
		if (candidates.isEmpty())
		{
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own.
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a Npc to compare distance
	 * @param state : the state in which the party member's QuestState must be in order to be considered.
	 * @return List<Player> : List of party members that matches the specified quest state, empty list if none matches. The party member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
	 */
	public List<Player> getPartyMembersState(Player player, Npc npc, byte state)
	{
		if (player == null)
		{
			return Collections.emptyList();
		}
		
		final Party party = player.getParty();
		if (party == null)
		{
			return (checkPlayerState(player, npc, state) != null) ? Arrays.asList(player) : Collections.emptyList();
		}
		
		final List<Player> result = new ArrayList<>();
		for (Player member : party.getPartyMembers())
		{
			if (checkPlayerState(member, npc, state) != null)
			{
				result.add(member);
			}
		}
		
		return result;
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own.
	 * @param player : the instance of a player whose party is to be searched
	 * @param npc : the instance of a monster to compare distance
	 * @param state : the state in which the party member's QuestState must be in order to be considered.
	 * @return Player: Player for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
	 */
	public Player getRandomPartyMemberState(Player player, Npc npc, byte state)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
		{
			return null;
		}
		
		// Return random candidate.
		final List<Player> members = getPartyMembersState(player, npc, state);
		if (members.isEmpty())
		{
			return null;
		}
		return members.get(Rnd.get(members.size()));
	}
	
	/**
	 * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player the instance of a player whose party is to be searched
	 * @param state the state in which the party member's queststate must be in order to be considered.
	 * @return Player: Player for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
	 */
	public Player getRandomPartyMemberState(Player player, byte state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		final Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || party.getPartyMembers().isEmpty())
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state))
			{
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly including this player)
		final List<Player> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		WorldObject target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (Player partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state) && partyMember.isInsideRadius3D(target, Config.ALT_PARTY_RANGE))
			{
				candidates.add(partyMember);
			}
		}
		
		// if there was no match, return null...
		if (candidates.isEmpty())
		{
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Show HTML file to client
	 * @param player
	 * @param fileName
	 * @return String : message sent to client
	 */
	public String showHtmlFile(Player player, String fileName)
	{
		// Create handler to file linked to the quest
		final String directory = _descr.toLowerCase();
		String content = HtmCache.getInstance().getHtm("data/scripts/" + directory + "/" + getName() + "/" + fileName);
		if (content == null)
		{
			content = HtmCache.getInstance().getHtmForce("data/scripts/quests/" + getName() + "/" + fileName);
		}
		
		if (player != null)
		{
			if (player.getTarget() != null)
			{
				content = content.replace("%objectId%", String.valueOf(player.getTarget().getObjectId()));
			}
			
			// Send message to client if message not empty
			if (content != null)
			{
				final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
		}
		return content;
	}
	
	/**
	 * Returns String representation of given quest html.
	 * @param fileName : the filename to send.
	 * @return String : message sent to client.
	 */
	public String getHtmlText(String fileName)
	{
		if (_questId > 0)
		{
			return HtmCache.getInstance().getHtmForce("data/scripts/quests/" + getName() + "/" + fileName);
		}
		return HtmCache.getInstance().getHtmForce("data/scripts/" + getDescr() + "/" + getName() + "/" + fileName);
	}
	
	// =========================================================
	// QUEST SPAWNS
	// =========================================================
	public Npc addSpawn(int npcId, Creature creature)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading(), false, 0);
	}
	
	public Npc addSpawn(int npcId, Location loc, boolean randomOffset, int despawnDelay)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay);
	}
	
	public Npc addSpawn(int npcId, Creature creature, boolean randomOffset, int despawnDelay)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading(), randomOffset, despawnDelay);
	}
	
	public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return QuestSpawn.getInstance().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}
	
	/**
	 * @return the registered quest items IDs.
	 */
	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	/**
	 * Registers all items that have to be destroyed in case player abort the quest or finish it.
	 * @param items
	 */
	public void registerQuestItems(int... items)
	{
		questItemIds = items;
	}
	
	@Override
	public boolean unload()
	{
		saveGlobalData();
		
		// Cancel all pending timers before reloading.
		// If timers ought to be restarted, the quest can take care of it with its code (example: save global data indicating what timer must be restarted).
		for (List<QuestTimer> timers : _questTimers.values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
			timers.clear();
		}
		_questTimers.clear();
		
		return QuestManager.getInstance().removeQuest(this);
	}
	
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method register ID for all EventTypes<br>
	 * Do not use for group_template AIs</b></font>
	 * @param mobs
	 * @see #registerMobs(int[], EventType...)
	 */
	public void registerMobs(int... mobs)
	{
		for (int id : mobs)
		{
			addEventId(id, EventType.ON_ATTACK);
			addEventId(id, EventType.ON_KILL);
			addEventId(id, EventType.ON_SPAWN);
			addEventId(id, EventType.ON_SPELL_FINISHED);
			addEventId(id, EventType.ON_FACTION_CALL);
			addEventId(id, EventType.ON_AGGRO_RANGE_ENTER);
		}
	}
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script event types defined in types.
	 * @param mobs
	 * @param types
	 */
	public void registerMobs(int[] mobs, EventType... types)
	{
		for (int id : mobs)
		{
			for (EventType type : types)
			{
				addEventId(id, type);
			}
		}
	}
	
	/**
	 * @param npc
	 * @param player
	 * @param isPet
	 * @return
	 */
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		return null;
	}
	
	public QuestState getClanLeaderQuestState(Player player, Npc npc)
	{
		// If player is the leader, retrieves directly the qS and bypass others checks
		if (player.isClanLeader() && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			return player.getQuestState(getName());
		}
		
		// Verify if the player got a clan
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return null;
		}
		
		// Verify if the leader is online
		final Player leader = clan.getLeader().getPlayer();
		if (leader == null)
		{
			return null;
		}
		
		if (leader.isDead())
		{
			return null;
		}
		
		// Verify if the player is on the radius of the leader. If true, send leader's quest state.
		if (leader.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			return leader.getQuestState(getName());
		}
		
		return null;
	}
	
	private void setQuestToOfflineMembers(List<Integer> objectsId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stm = con.prepareStatement("INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			for (Integer charId : objectsId)
			{
				stm.setInt(1, charId.intValue());
				stm.setString(2, getName());
				stm.setString(3, "<state>");
				stm.setString(4, "1");
				stm.executeUpdate();
			}
			
			stm.close();
			con.close();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error in updating character_quest table from Quest.java on method setQuestToOfflineMembers");
			LOGGER.info(e.toString());
		}
	}
	
	private void deleteQuestToOfflineMembers(int clanId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stm = con.prepareStatement("DELETE FROM character_quests WHERE name = ? and char_id IN (SELECT charId FROM characters WHERE clanid = ? AND online = 0)");
			stm.setString(1, getName());
			stm.setInt(2, clanId);
			stm.executeUpdate();
			
			stm.close();
			con.close();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error in deleting infos from character_quest table from Quest.java on method deleteQuestToOfflineMembers");
			LOGGER.info(e.toString());
		}
	}
	
	/**
	 * Sets the current quest to clan offline's members
	 * @param player the current player (should be clan leader)
	 */
	public void setQuestToClanMembers(Player player)
	{
		if (player.isClanLeader())
		{
			// Setting it for online members...
			for (Player onlineMember : player.getClan().getOnlineMembers())
			{
				if (!onlineMember.isClanLeader())
				{
					onlineMember.setQuestState(player.getQuestState(getName()));
				}
			}
			
			// Setting it for offline members...
			setQuestToOfflineMembers(player.getClan().getOfflineMembersIds());
		}
	}
	
	/**
	 * Finish the current quest to a clan's members
	 * @param player clan's leader
	 */
	public void finishQuestToClan(Player player)
	{
		if (player.isClanLeader())
		{
			// Deleting it for online members...
			for (Player onlineMember : player.getClan().getOnlineMembers())
			{
				if (!onlineMember.isClanLeader())
				{
					onlineMember.delQuestState(getName());
				}
			}
			
			// Deleting it for offline members...
			deleteQuestToOfflineMembers(player.getClanId());
		}
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemId the ID of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(Player player, int itemId)
	{
		return player.getInventory().getItemByItemId(itemId) != null;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(Player player, int... itemIds)
	{
		if ((itemIds == null) || (itemIds.length == 0))
		{
			return false;
		}
		final PlayerInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the amount of an item in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemId the ID of the item whose amount to get
	 * @return the amount of the specified item in player's inventory
	 */
	public static long getQuestItemsCount(Player player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}
	
	/**
	 * Get the total amount of all specified items in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemIds a list of IDs of items whose amount to get
	 * @return the summary amount of all listed items in player's inventory
	 */
	public long getQuestItemsCount(Player player, int... itemIds)
	{
		long count = 0;
		for (Item item : player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			for (int itemId : itemIds)
			{
				if (item.getItemId() == itemId)
				{
					if ((count + item.getCount()) > Long.MAX_VALUE)
					{
						return Long.MAX_VALUE;
					}
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	/**
	 * Check if the player has the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @return {@code true} if the player has the required count of the item
	 */
	protected static boolean hasItem(Player player, ItemHolder item)
	{
		return hasItem(player, item, true);
	}
	
	/**
	 * Check if the player has the required count of the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @return {@code true} if the player has the item
	 */
	protected static boolean hasItem(Player player, ItemHolder item, boolean checkCount)
	{
		if (item == null)
		{
			return false;
		}
		if (checkCount)
		{
			return getQuestItemsCount(player, item.getId()) >= item.getCount();
		}
		return hasQuestItems(player, item.getId());
	}
	
	/**
	 * Take an amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param itemId the ID of the item to take
	 * @param amount the amount to take
	 * @return {@code true} if any items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(Player player, int itemId, int amount)
	{
		final Collection<Item> items = player.getInventory().getAllItemsByItemId(itemId);
		if (amount < 0)
		{
			items.forEach(i -> takeItem(player, i, i.getCount()));
		}
		else
		{
			int currentCount = 0;
			for (Item i : items)
			{
				int toDelete = i.getCount();
				if ((currentCount + toDelete) > amount)
				{
					toDelete = amount - currentCount;
				}
				if (toDelete > 0)
				{
					takeItem(player, i, toDelete);
				}
				currentCount += toDelete;
			}
		}
		return true;
	}
	
	private static boolean takeItem(Player player, Item item, int toDelete)
	{
		if (item.isEquipped())
		{
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item itm : player.getInventory().unEquipItemInBodySlotAndRecord(item.getTemplate().getBodyPart()))
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		return player.destroyItemByItemId("Quest", item.getItemId(), toDelete, player, true);
	}
	
	/**
	 * Take a set amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param holder the {@link ItemHolder} object containing the ID and count of the item to take
	 * @return {@code true} if the item was taken, {@code false} otherwise
	 */
	protected static boolean takeItem(Player player, ItemHolder holder)
	{
		return (holder != null) && takeItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Take an amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param amount the amount to take of each item
	 * @param itemIds a list or an array of IDs of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(Player player, int amount, int... itemIds)
	{
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}
		return check;
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param holder
	 */
	public static void rewardItems(Player player, ItemHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param itemId the ID of the item to give
	 * @param countValue the amount of items to give
	 */
	public static void rewardItems(Player player, int itemId, int countValue)
	{
		if (countValue <= 0)
		{
			return;
		}
		
		final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			return;
		}
		
		int count = countValue;
		if (itemId == 57)
		{
			// TODO: Config.RATE_QUEST_REWARD_ADENA
		}
		else
		{
			count *= Config.RATE_QUESTS_REWARD;
		}
		
		// Add items to player's inventory
		final Item itemInstance = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (itemInstance == null)
		{
			return;
		}
		
		sendItemGetMessage(player, itemInstance, count);
	}
	
	/**
	 * Send the system message and the status update packets to the player.
	 * @param player the player that has got the item
	 * @param item the item obtain by the player
	 * @param count the item count
	 */
	private static void sendItemGetMessage(Player player, Item item, int count)
	{
		// If item for reward is gold, send message of gold reward to client
		if (item.getItemId() == 57)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_ADENA);
			smsg.addNumber(count);
			player.sendPacket(smsg);
		}
		else // Otherwise, send message of object reward to client
		{
			if (count > 1)
			{
				final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				smsg.addItemName(item.getItemId());
				smsg.addNumber(count);
				player.sendPacket(smsg);
			}
			else
			{
				final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
				smsg.addItemName(item.getItemId());
				player.sendPacket(smsg);
			}
		}
		
		// send packets
		player.sendPacket(new ItemList(player, false));
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param itemId
	 * @param count
	 */
	public static void giveItems(Player player, int itemId, int count)
	{
		giveItems(player, itemId, count, 0);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param holder
	 */
	protected static void giveItems(Player player, ItemHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param enchantlevel
	 */
	public static void giveItems(Player player, int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final Item item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((enchantlevel > 0) && (itemId != 57))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param attributeId
	 * @param attributeLevel
	 */
	public static void giveItems(Player player, int itemId, int count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final Item item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Get a random integer from 0 (inclusive) to {@code max} (exclusive).<br>
	 * Use this method instead of importing {@link org.l2jasp.commons.util.Rnd} utility.
	 * @param max the maximum value for randomization
	 * @return a random integer number from 0 to {@code max - 1}
	 */
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Get a random integer from {@code min} (inclusive) to {@code max} (inclusive).<br>
	 * Use this method instead of importing {@link org.l2jasp.commons.util.Rnd} utility.
	 * @param min the minimum value for randomization
	 * @param max the maximum value for randomization
	 * @return a random integer number from {@code min} to {@code max}
	 */
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	/**
	 * Get a random boolean.<br>
	 * Use this method instead of importing {@link org.l2jasp.commons.util.Rnd} utility.
	 * @return {@code true} or {@code false} randomly
	 */
	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}
	
	/**
	 * Get a random entry.
	 * @param <T>
	 * @param array of values.
	 * @return one value from array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRandomEntry(T... array)
	{
		if (array.length == 0)
		{
			return null;
		}
		return array[Rnd.get(array.length)];
	}
	
	/**
	 * Get a random entry.
	 * @param <T>
	 * @param list of values.
	 * @return one value from list.
	 */
	public static <T> T getRandomEntry(List<T> list)
	{
		if (list.isEmpty())
		{
			return null;
		}
		return list.get(Rnd.get(list.size()));
	}
	
	/**
	 * Get a random entry.
	 * @param array of Integers.
	 * @return one Integer from array.
	 */
	public static int getRandomEntry(int... array)
	{
		return array[Rnd.get(array.length)];
	}
}
