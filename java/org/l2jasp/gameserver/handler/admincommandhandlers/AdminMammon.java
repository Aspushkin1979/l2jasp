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

import java.util.Queue;

import org.l2jasp.gameserver.data.sql.NpcTable;
import org.l2jasp.gameserver.data.sql.SpawnTable;
import org.l2jasp.gameserver.handler.IAdminCommandHandler;
import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.sevensigns.SevenSigns;
import org.l2jasp.gameserver.model.spawn.AutoSpawnHandler;
import org.l2jasp.gameserver.model.spawn.AutoSpawnHandler.AutoSpawnInstance;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.util.BuilderUtil;
import org.l2jasp.gameserver.util.Util;

/**
 * Admin Command Handler for Mammon NPCs
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mammon_find",
		"admin_mammon_respawn",
		"admin_list_spawns",
		"admin_msg"
	};
	
	private final boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean useAdminCommand(String command, Player activeChar)
	{
		int npcId = 0;
		int teleportIndex = -1;
		
		final AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		final AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);
		if (command.startsWith("admin_mammon_find"))
		{
			try
			{
				if (command.length() > 17)
				{
					teleportIndex = Integer.parseInt(command.substring(18));
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
			}
			
			if (!_isSealValidation)
			{
				BuilderUtil.sendSysMessage(activeChar, "The competition period is currently in effect.");
				return true;
			}
			
			if (blackSpawnInst != null)
			{
				final Queue<Npc> blackInst = blackSpawnInst.getNpcList();
				if (!blackInst.isEmpty())
				{
					final Npc npc = blackInst.stream().findFirst().get();
					final int x1 = npc.getX();
					final int y1 = npc.getY();
					final int z1 = npc.getZ();
					BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
					if (teleportIndex == 1)
					{
						activeChar.teleToLocation(x1, y1, z1, true);
					}
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon isn't registered for spawn.");
			}
			
			if (merchSpawnInst != null)
			{
				final Queue<Npc> merchInst = merchSpawnInst.getNpcList();
				if (!merchInst.isEmpty())
				{
					final Npc npc = merchInst.stream().findFirst().get();
					final int x2 = npc.getX();
					final int y2 = npc.getY();
					final int z2 = npc.getZ();
					BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
					if (teleportIndex == 2)
					{
						activeChar.teleToLocation(x2, y2, z2, true);
					}
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon isn't registered for spawn.");
			}
		}
		
		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!_isSealValidation)
			{
				BuilderUtil.sendSysMessage(activeChar, "The competition period is currently in effect.");
				return true;
			}
			
			if (merchSpawnInst != null)
			{
				final long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);
				BuilderUtil.sendSysMessage(activeChar, "The Merchant of Mammon will respawn in " + (merchRespawn / 60000) + " minute(s).");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon isn't registered for spawn.");
			}
			
			if (blackSpawnInst != null)
			{
				final long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
				BuilderUtil.sendSysMessage(activeChar, "The Blacksmith of Mammon will respawn in " + (blackRespawn / 60000) + " minute(s).");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon isn't registered for spawn.");
			}
		}
		
		else if (command.startsWith("admin_list_spawns"))
		{
			try
			{
				// Split the command into an array of words.
				final String[] params = command.split(" ");
				final StringBuilder searchParam = new StringBuilder();
				int pos = -1;
				
				// Concatenate all words in the command except the first and last word.
				for (String param : params)
				{
					pos++;
					if ((pos > 0) && (pos < (params.length - 1)))
					{
						searchParam.append(param);
						searchParam.append(" ");
					}
				}
				
				final String searchString = searchParam.toString().trim();
				// If the search string is a number, use it as the NPC ID.
				if (Util.isDigit(searchString))
				{
					npcId = Integer.parseInt(searchString);
				}
				else
				{
					// Otherwise, use it as the NPC name and look up the NPC ID.
					npcId = NpcTable.getInstance().getTemplateByName(searchString).getNpcId();
				}
				
				// If there are more than two words in the command, try to parse the last word as the teleport index.
				if (params.length > 2)
				{
					final String lastParam = params[params.length - 1];
					if (Util.isDigit(lastParam))
					{
						teleportIndex = Integer.parseInt(lastParam);
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Command format is //list_spawns <npcId|npc_name> [tele_index]");
			}
			
			// Call the findNpcs method with the parsed NPC ID and teleport index.
			SpawnTable.getInstance().findNpcs(activeChar, npcId, teleportIndex);
		}
		
		// Used for testing SystemMessage IDs - Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			int msgId = -1;
			
			try
			{
				msgId = Integer.parseInt(command.substring(10).trim());
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			activeChar.sendPacket(new SystemMessage(msgId));
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
