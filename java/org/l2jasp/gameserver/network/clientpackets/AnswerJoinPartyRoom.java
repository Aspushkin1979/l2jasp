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

import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jasp.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jasp.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ExManagePartyRoomMember;
import org.l2jasp.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jasp.gameserver.network.serverpackets.PartyMatchDetail;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

public class AnswerJoinPartyRoom implements ClientPacket
{
	private int _answer; // 1 or 0
	
	@Override
	public void read(ReadablePacket packet)
	{
		_answer = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Player partner = player.getActiveRequester();
		if (partner == null)
		{
			// Partner hasn't be found, cancel the invitation
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			player.setActiveRequester(null);
			return;
		}
		else if (World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Partner hasn't be found, cancel the invitation
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			player.setActiveRequester(null);
			return;
		}
		
		// If answer is positive, join the requester's PartyRoom.
		if ((_answer == 1) && !partner.isRequestExpired())
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(partner.getPartyRoom());
			if (room == null)
			{
				return;
			}
			
			if ((player.getLevel() >= room.getMinLevel()) && (player.getLevel() <= room.getMaxLevel()))
			{
				// Remove from waiting list
				PartyMatchWaitingList.getInstance().removePlayer(player);
				
				player.setPartyRoom(partner.getPartyRoom());
				
				player.sendPacket(new PartyMatchDetail(room));
				player.sendPacket(new ExPartyRoomMember(room, 0));
				for (Player member : room.getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}
					
					member.sendPacket(new ExManagePartyRoomMember(player, room, 0));
					member.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_ENTERED_THE_PARTY_ROOM).addString(player.getName()));
				}
				room.addMember(player);
				
				// Info Broadcast
				player.broadcastUserInfo();
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM);
			}
		}
		// Else, send a message to requester.
		else
		{
			partner.sendPacket(SystemMessageId.THE_RECIPIENT_OF_YOUR_INVITATION_DID_NOT_ACCEPT_THE_PARTY_MATCHING_INVITATION);
		}
		
		// reset transaction timers
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
}