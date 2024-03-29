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

import org.l2jasp.gameserver.handler.IAdminCommandHandler;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.serverpackets.LeaveWorld;
import org.l2jasp.gameserver.util.BuilderUtil;

public class AdminKick implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kick",
		"admin_kick_non_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_kick"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			if (activeChar.getTarget() != null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Type //kick name");
			}
			
			if (st.countTokens() > 1)
			{
				st.nextToken();
				
				final String player = st.nextToken();
				final Player plyr = World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.logout(true);
					BuilderUtil.sendSysMessage(activeChar, "You kicked " + plyr.getName() + " from the game.");
				}
				
				if ((plyr != null) && plyr.isInOfflineMode())
				{
					plyr.deleteMe();
					BuilderUtil.sendSysMessage(activeChar, "You kicked Offline Player " + plyr.getName() + " from the game.");
				}
			}
		}
		
		if (command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;
			for (Player player : World.getInstance().getAllPlayers())
			{
				if (!player.isGM())
				{
					counter++;
					player.sendPacket(new LeaveWorld());
					player.logout(true);
				}
			}
			BuilderUtil.sendSysMessage(activeChar, "Kicked " + counter + " players");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
