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
package org.l2jasp.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.l2jasp.commons.database.DatabaseFactory;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.FriendList;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 5F 01 00 00 00 format cdd
 */
public class RequestAnswerFriendInvite implements ClientPacket
{
	private int _response;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_response = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player != null)
		{
			final Player requestor = player.getActiveRequester();
			if (requestor == null)
			{
				return;
			}
			
			if (_response == 1)
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id) VALUES (?,?), (?,?)");
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.setInt(3, player.getObjectId());
					statement.setInt(4, requestor.getObjectId());
					statement.execute();
					statement.close();
					
					requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND_TO_YOUR_FRIENDS_LIST);
					
					// Player added to your friendlist
					requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST).addString(player.getName()));
					requestor.getFriendList().add(player.getObjectId());
					
					// has joined as friend.
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_AS_A_FRIEND).addString(requestor.getName()));
					player.getFriendList().add(requestor.getObjectId());
					
					// update friendLists *heavy method*
					requestor.sendPacket(new FriendList(requestor));
					player.sendPacket(new FriendList(player));
				}
				catch (Exception e)
				{
					PacketLogger.warning("Could not add friend objectid: " + e);
				}
			}
			else
			{
				requestor.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}