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
package org.l2jasp.gameserver.handler.usercommandhandlers;

import org.l2jasp.gameserver.handler.IUserCommandHandler;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;
import org.l2jasp.gameserver.taskmanager.GameTimeTaskManager;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		if (COMMAND_IDS[0] != id)
		{
			return false;
		}
		
		final int t = GameTimeTaskManager.getInstance().getGameTime();
		final String h = "" + ((t / 60) % 24);
		String m;
		if ((t % 60) < 10)
		{
			m = "0" + (t % 60);
		}
		else
		{
			m = "" + (t % 60);
		}
		
		SystemMessage sm;
		if (GameTimeTaskManager.getInstance().isNight())
		{
			sm = new SystemMessage(SystemMessageId.THE_CURRENT_TIME_IS_S1_S2_IN_THE_NIGHT);
			sm.addString(h);
			sm.addString(m);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.THE_CURRENT_TIME_IS_S1_S2_IN_THE_DAY);
			sm.addString(h);
			sm.addString(m);
		}
		player.sendPacket(sm);
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
