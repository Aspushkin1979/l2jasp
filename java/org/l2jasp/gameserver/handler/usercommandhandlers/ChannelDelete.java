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
import org.l2jasp.gameserver.model.CommandChannel;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelDelete implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		93
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if ((player != null) && player.isInParty())
		{
			if (player.getParty().isLeader(player) && player.getParty().isInCommandChannel() && player.getParty().getCommandChannel().getChannelLeader().equals(player))
			{
				final CommandChannel channel = player.getParty().getCommandChannel();
				channel.broadcastToChannelMembers(new SystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED));
				channel.disbandChannel();
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
