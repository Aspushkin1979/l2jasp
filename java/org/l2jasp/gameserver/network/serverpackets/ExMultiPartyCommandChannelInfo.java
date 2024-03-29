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

import org.l2jasp.gameserver.model.CommandChannel;
import org.l2jasp.gameserver.model.Party;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author chris_00 ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo extends ServerPacket
{
	private final CommandChannel _channel;
	
	public ExMultiPartyCommandChannelInfo(CommandChannel channel)
	{
		_channel = channel;
	}
	
	@Override
	public void write()
	{
		if (_channel == null)
		{
			return;
		}
		
		ServerPackets.EX_MULTI_PARTY_COMMAND_CHANNEL_INFO.writeId(this);
		writeString(_channel.getChannelLeader().getName());
		writeInt(0); // Channel loot
		writeInt(_channel.getMemberCount());
		writeInt(_channel.getParties().size());
		for (Party p : _channel.getParties())
		{
			writeString(p.getLeader().getName());
			writeInt(p.getPartyLeaderOID());
			writeInt(p.getMemberCount());
		}
	}
}