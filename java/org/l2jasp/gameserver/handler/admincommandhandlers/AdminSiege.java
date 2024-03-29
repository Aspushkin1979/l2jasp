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

import java.util.StringTokenizer;

import org.l2jasp.gameserver.data.sql.ClanHallTable;
import org.l2jasp.gameserver.data.sql.ClanTable;
import org.l2jasp.gameserver.handler.IAdminCommandHandler;
import org.l2jasp.gameserver.instancemanager.AuctionManager;
import org.l2jasp.gameserver.instancemanager.CastleManager;
import org.l2jasp.gameserver.instancemanager.SiegeManager;
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.model.residences.ClanHall;
import org.l2jasp.gameserver.model.siege.Castle;
import org.l2jasp.gameserver.model.zone.type.ClanHallZone;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.util.BuilderUtil;

/**
 * This class handles all siege commands: TODO: change the class name, and neaten it up
 */
public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setcastle",
		"admin_removecastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};
	
	@Override
	public boolean useAdminCommand(String commandValue, Player activeChar)
	{
		String command = commandValue;
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get castle
		Castle castle = null;
		ClanHall clanhall = null;
		if (command.startsWith("admin_clanhall"))
		{
			clanhall = ClanHallTable.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
		}
		else if (st.hasMoreTokens())
		{
			castle = CastleManager.getInstance().getCastle(st.nextToken());
		}
		
		// Get castle
		String val = "";
		if (st.hasMoreTokens())
		{
			val = st.nextToken();
		}
		
		if (((castle == null) || (castle.getCastleId() < 0)) && (clanhall == null))
		{
			// No castle specified
			showCastleSelectPage(activeChar);
		}
		else
		{
			final WorldObject target = activeChar.getTarget();
			Player player = null;
			if (target instanceof Player)
			{
				player = (Player) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if ((player == null) || (castle == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
				{
					BuilderUtil.sendSysMessage(activeChar, "Clan is already registered!");
				}
				else
				{
					castle.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if ((player == null) || (castle == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else
				{
					castle.getSiege().registerDefender(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_guard"))
			{
				if (castle != null)
				{
					try
					{
						final int npcId = Integer.parseInt(val);
						castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
					}
					catch (Exception e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //add_guard npcId");
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				if (castle != null)
				{
					castle.getSiege().clearSiegeClan();
				}
			}
			else if (command.equalsIgnoreCase("admin_endsiege"))
			{
				if (castle != null)
				{
					castle.getSiege().endSiege();
				}
			}
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				if (castle != null)
				{
					castle.getSiege().listRegisterClan(activeChar);
				}
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else if (castle != null)
				{
					castle.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				if (castle != null)
				{
					final Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
					if (clan != null)
					{
						castle.removeOwner(clan);
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Unable to remove castle");
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if ((player == null) || (player.getClan() == null) || (clanhall == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else if (!ClanHallTable.getInstance().isFree(clanhall.getId()))
				{
					BuilderUtil.sendSysMessage(activeChar, "This ClanHall isn't free!");
				}
				else if (player.getClan().getHideoutId() == 0)
				{
					ClanHallTable.getInstance().setOwner(clanhall.getId(), player.getClan());
					if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
					{
						AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "You have already a ClanHall!");
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if ((clanhall != null) && !ClanHallTable.getInstance().isFree(clanhall.getId()))
				{
					ClanHallTable.getInstance().setFree(clanhall.getId());
					AuctionManager.getInstance().initNPC(clanhall.getId());
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "This ClanHall is already Free!");
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallopendoors"))
			{
				if (clanhall != null)
				{
					clanhall.openCloseDoors(true);
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallclosedoors"))
			{
				if (clanhall != null)
				{
					clanhall.openCloseDoors(false);
				}
			}
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				if (clanhall != null)
				{
					final ClanHallZone zone = clanhall.getZone();
					if (zone != null)
					{
						activeChar.teleToLocation(zone.getSpawnLoc(), true);
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
			{
				if (castle != null)
				{
					castle.spawnDoor();
				}
			}
			else if (command.equalsIgnoreCase("admin_startsiege"))
			{
				if (castle != null)
				{
					castle.getSiege().startSiege();
				}
			}
			
			if (clanhall != null)
			{
				showClanHallPage(activeChar, clanhall);
			}
			else if (castle != null)
			{
				showSiegePage(activeChar, castle.getName());
			}
		}
		
		return true;
	}
	
	private void showCastleSelectPage(Player activeChar)
	{
		int i = 0;
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castles.htm");
		StringBuilder cList = new StringBuilder();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				final String name = castle.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_siege " + name + "\">" + name + "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%castles%", cList.toString());
		cList = new StringBuilder();
		i = 0;
		for (ClanHall clanhall : ClanHallTable.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
				cList.append(clanhall.getName() + "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%clanhalls%", cList.toString());
		cList = new StringBuilder();
		i = 0;
		for (ClanHall clanhall : ClanHallTable.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
				cList.append(clanhall.getName() + "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%freeclanhalls%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showSiegePage(Player activeChar, String castleName)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castle.htm");
		adminReply.replace("%castleName%", castleName);
		activeChar.sendPacket(adminReply);
	}
	
	private void showClanHallPage(Player activeChar, ClanHall clanhall)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/clanhall.htm");
		adminReply.replace("%clanhallName%", clanhall.getName());
		adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
		final Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		if (owner == null)
		{
			adminReply.replace("%clanhallOwner%", "None");
		}
		else
		{
			adminReply.replace("%clanhallOwner%", owner.getName());
		}
		
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
