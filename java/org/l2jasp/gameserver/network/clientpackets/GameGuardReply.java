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
import org.l2jasp.gameserver.network.GameClient;

/**
 * @author zabbix Lets drink to code! Unknown Packet: ca 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00 E....7..........
 */
public class GameGuardReply implements ClientPacket
{
	private final int[] _reply = new int[4];
	
	@Override
	public void read(ReadablePacket packet)
	{
		_reply[0] = packet.readInt();
		_reply[1] = packet.readInt();
		_reply[2] = packet.readInt();
		_reply[3] = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		client.setGameGuardOk(true);
	}
}