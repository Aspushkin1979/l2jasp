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
import org.l2jasp.gameserver.data.sql.ClanTable;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;

public class RequestReplySurrenderPledgeWar implements ClientPacket
{
	private int _answer;
	
	@Override
	public void read(ReadablePacket packet)
	{
		@SuppressWarnings("unused")
		final String reqName = packet.readString();
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
		
		final Player requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 1)
		{
			requestor.deathPenalty(false);
			ClanTable.getInstance().deleteClanWars(requestor.getClanId(), player.getClanId());
		}
		
		player.onTransactionRequest(null);
	}
}
