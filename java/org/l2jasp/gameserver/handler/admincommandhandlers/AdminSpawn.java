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
package org.l2jasp.gameserver.handler.admincommandhandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jasp.gameserver.data.sql.NpcTable;
import org.l2jasp.gameserver.data.sql.SpawnTable;
import org.l2jasp.gameserver.data.sql.TeleportLocationTable;
import org.l2jasp.gameserver.data.xml.AdminData;
import org.l2jasp.gameserver.handler.IAdminCommandHandler;
import org.l2jasp.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jasp.gameserver.instancemanager.GrandBossManager;
import org.l2jasp.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.templates.NpcTemplate;
import org.l2jasp.gameserver.model.sevensigns.SevenSigns;
import org.l2jasp.gameserver.model.spawn.Spawn;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jasp.gameserver.util.BuilderUtil;
import org.l2jasp.gameserver.util.Util;

/**
 * This class handles following admin commands: - show_spawns = shows menu - spawn_index lvl = shows menu for monsters with respective level - spawn_monster id = spawns monster id on target
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler
{
	public static final Logger LOGGER = Logger.getLogger(AdminSpawn.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_spawns",
		"admin_spawn",
		"admin_spawn_monster",
		"admin_spawn_index",
		"admin_unspawnall",
		"admin_respawnall",
		"admin_spawn_reload",
		"admin_npc_index",
		"admin_spawn_once",
		"admin_show_npcs",
		"admin_teleport_reload",
		"admin_spawnnight",
		"admin_spawnday",
		"admin_topspawncount",
		"admin_top_spawn_count"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_show_spawns"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		}
		else if (command.startsWith("admin_spawn_index"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			
			try
			{
				st.nextToken();
				
				final int level = Integer.parseInt(st.nextToken());
				int from = 0;
				
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				
				showMonsters(activeChar, level, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.equals("admin_show_npcs"))
		{
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		}
		else if (command.startsWith("admin_npc_index"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			
			try
			{
				st.nextToken();
				final String letter = st.nextToken();
				int from = 0;
				
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				
				showNpcs(activeChar, letter, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		// Command spawn '//spawn name numberSpawn respawnTime'.
		// With command '//spawn name' the respawnTime will be 60 seconds.
		else if (command.startsWith("admin_spawn") || command.startsWith("admin_spawn_monster"))
		{
			try
			{
				// Create a StringTokenizer to split the command by spaces.
				final StringTokenizer st = new StringTokenizer(command, " ");
				
				// Get the first token (the command itself).
				final String cmd = st.nextToken();
				
				// Get the second token (the NPC ID or name).
				String npcId = st.nextToken();
				
				// If the second token is not a digit, search for the NPC template by name.
				if (!Util.isDigit(npcId))
				{
					// Initialize the variables.
					final StringBuilder searchParam = new StringBuilder();
					final String[] params = command.split(" ");
					NpcTemplate searchTemplate = null;
					NpcTemplate template = null;
					int pos = 1;
					
					// Iterate through the command parameters, starting from the second one.
					for (int i = 1; i < params.length; i++)
					{
						// Add the current parameter to the search parameter string.
						searchParam.append(params[i]);
						searchParam.append(" ");
						
						// Try to get the NPC template using the search parameter string.
						searchTemplate = NpcTable.getInstance().getTemplateByName(searchParam.toString().trim());
						
						// If the template is found, update the position and the final template.
						if (searchTemplate != null)
						{
							template = searchTemplate;
							pos = i;
						}
					}
					
					// Check if an NPC template was found.
					if (template != null)
					{
						// Skip tokens that contain the name.
						for (int i = 1; i < pos; i++)
						{
							st.nextToken();
						}
						
						// Set the npcId based on template found.
						npcId = String.valueOf(template.getNpcId());
					}
				}
				
				// Initialize mobCount to 1.
				int mobCount = 1;
				
				// If next token exists, set the mobCount value.
				if (st.hasMoreTokens())
				{
					mobCount = Integer.parseInt(st.nextToken());
				}
				
				// Initialize respawnTime to 60.
				int respawnTime = 60;
				
				// If next token exists, set the respawnTime value.
				if (st.hasMoreTokens())
				{
					respawnTime = Integer.parseInt(st.nextToken());
				}
				
				// Call the spawnMonster method with the appropriate parameters.
				spawnMonster(activeChar, npcId, respawnTime, mobCount, !cmd.equalsIgnoreCase("admin_spawn_once"));
			}
			catch (Exception e)
			{
				// Case of wrong or missing monster data.
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		// Command for unspawn all Npcs on Server, use //repsawnall to respawn the npc
		else if (command.startsWith("admin_unspawnall"))
		{
			for (Player player : World.getInstance().getAllPlayers())
			{
				player.sendPacket(SystemMessageId.THE_NPC_SERVER_IS_NOT_OPERATING_AT_THIS_TIME);
			}
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			World.getInstance().deleteVisibleNpcSpawns();
			AdminData.broadcastMessageToGMs("NPC Unspawn completed!");
		}
		else if (command.startsWith("admin_spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnDayCreatures();
		}
		else if (command.startsWith("admin_spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnNightCreatures();
		}
		else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make sure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			World.getInstance().deleteVisibleNpcSpawns();
			// now respawn all
			NpcTable.getInstance().reloadAllNpc();
			SpawnTable.getInstance().reloadAll();
			RaidBossSpawnManager.getInstance().load();
			SevenSigns.getInstance().spawnSevenSignsNPC();
			AdminData.broadcastMessageToGMs("NPC Respawn completed!");
		}
		else if (command.startsWith("admin_teleport_reload"))
		{
			TeleportLocationTable.getInstance().load();
			AdminData.broadcastMessageToGMs("Teleport List Table reloaded.");
		}
		else if (command.startsWith("admin_topspawncount") || command.startsWith("admin_top_spawn_count"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int count = 5;
			if (st.hasMoreTokens())
			{
				final String nextToken = st.nextToken();
				if (Util.isDigit(nextToken))
				{
					count = Integer.parseInt(nextToken);
				}
				if (count <= 0)
				{
					return true;
				}
			}
			final Map<Integer, Integer> npcsFound = new HashMap<>();
			for (WorldObject obj : World.getInstance().getAllVisibleObjects())
			{
				if (!(obj instanceof Npc))
				{
					continue;
				}
				final int npcId = ((Npc) obj).getNpcId();
				if (npcsFound.containsKey(npcId))
				{
					npcsFound.put(npcId, npcsFound.get(npcId) + 1);
				}
				else
				{
					npcsFound.put(npcId, 1);
				}
			}
			BuilderUtil.sendSysMessage(activeChar, "Top " + count + " spawn count.");
			for (Entry<Integer, Integer> entry : Util.sortByValue(npcsFound, true).entrySet())
			{
				count--;
				if (count < 0)
				{
					break;
				}
				final int npcId = entry.getKey();
				BuilderUtil.sendSysMessage(activeChar, NpcTable.getInstance().getTemplate(npcId).getName() + " (" + npcId + "): " + entry.getValue());
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void spawnMonster(Player activeChar, String monsterIdValue, int respawnTime, int mobCount, boolean permanent)
	{
		WorldObject target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
		if ((target != activeChar) && activeChar.getAccessLevel().isGm())
		{
			target = activeChar;
		}
		
		NpcTemplate template1;
		String monsterId = monsterIdValue;
		if (monsterId.matches("[0-9]*"))
		{
			// First parameter was an ID number
			final int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcTable.getInstance().getTemplateByName(monsterId);
		}
		
		if (template1 == null)
		{
			BuilderUtil.sendSysMessage(activeChar, "Attention, wrong NPC ID/Name");
			return;
		}
		
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setX(target.getX());
			spawn.setY(target.getY());
			spawn.setZ(target.getZ());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			
			if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) || GrandBossManager.getInstance().isDefined(spawn.getNpcId()))
			{
				BuilderUtil.sendSysMessage(activeChar, "Another spawn of " + template1.getName() + " is already present in the database.");
				return;
			}
			
			if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcId()) != null)
			{
				RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getStatSet().getDouble("baseHpMax"), template1.getStatSet().getDouble("baseMpMax"), permanent);
			}
			else
			{
				SpawnTable.getInstance().addNewSpawn(spawn, permanent);
			}
			
			spawn.init();
			
			if (!permanent)
			{
				spawn.stopRespawn();
			}
			
			BuilderUtil.sendSysMessage(activeChar, "Created " + template1.getName() + " on " + target.getObjectId());
		}
		catch (Exception e)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
		}
	}
	
	private void showMonsters(Player activeChar, int level, int from)
	{
		final StringBuilder tb = new StringBuilder();
		final List<NpcTemplate> mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);
		
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> Level " + level + ":<br>Total NPCs : " + mobs.size() + "<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index " + level + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		final String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.size(); i++)
		{
			final String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs.get(i).getNpcId() + "\">" + mobs.get(i).getName() + "</a><br1>";
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			
			tb.append(txt);
		}
		
		// End
		if (ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}
		
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
	
	private void showNpcs(Player activeChar, String starting, int from)
	{
		final StringBuilder tb = new StringBuilder();
		final List<NpcTemplate> mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);
		
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> There are " + mobs.size() + " Npcs whose name starts with " + starting + ":<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index " + starting + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		final String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.size(); i++)
		{
			final String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs.get(i).getNpcId() + "\">" + mobs.get(i).getName() + "</a><br1>";
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			tb.append(txt);
		}
		// End
		if (ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}
		
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
}
