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

import org.l2jasp.Config;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.communitybbs.CommunityBoard;
import org.l2jasp.gameserver.network.GameClient;

public class RequestShowBoard implements ClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;
	
	/**
	 * packet type id 0x57 sample 57 01 00 00 00 // unknown (always 1?) format: cd
	 */
	@Override
	public void read(ReadablePacket packet)
	{
		_unknown = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		CommunityBoard.getInstance().handleCommands(client, Config.BBS_DEFAULT);
	}
}
