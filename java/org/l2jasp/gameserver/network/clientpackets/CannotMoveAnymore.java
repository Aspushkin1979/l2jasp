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
import org.l2jasp.gameserver.ai.CtrlEvent;
import org.l2jasp.gameserver.model.Location;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;

public class CannotMoveAnymore implements ClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_x = packet.readInt();
		_y = packet.readInt();
		_z = packet.readInt();
		_heading = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getAI() != null)
		{
			player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new Location(_x, _y, _z, _heading));
		}
	}
}
