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
package org.l2jasp.gameserver.network.serverpackets;

import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Send Private (Friend) Message Format: c dSSS d: Unknown S: Sending Player S: Receiving Player S: Message
 * @author Tempy
 */
public class FriendRecvMsg extends ServerPacket
{
	private final String _sender;
	private final String _receiver;
	private final String _message;
	
	public FriendRecvMsg(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}
	
	@Override
	public void write()
	{
		ServerPackets.FRIEND_RECV_MSG.writeId(this);
		writeInt(0); // ??
		writeString(_receiver);
		writeString(_sender);
		writeString(_message);
	}
}
