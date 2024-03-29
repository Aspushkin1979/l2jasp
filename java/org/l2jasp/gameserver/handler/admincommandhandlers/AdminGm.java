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

import org.l2jasp.gameserver.data.xml.AdminData;
import org.l2jasp.gameserver.handler.IAdminCommandHandler;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - GM = turns GM mode on/off
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminGm implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_gm"))
		{
			handleGm(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleGm(Player activeChar)
	{
		if (activeChar.isGM())
		{
			AdminData.getInstance().deleteGm(activeChar);
			BuilderUtil.sendSysMessage(activeChar, "You no longer have GM status.");
		}
		else
		{
			AdminData.getInstance().addGm(activeChar, false);
			BuilderUtil.sendSysMessage(activeChar, "You now have GM status.");
		}
	}
}
