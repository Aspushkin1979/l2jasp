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
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.AskJoinPledge;

public class RequestJoinPledge implements ClientPacket
{
	private int _target;
	private int _pledgeType;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_target = packet.readInt();
		_pledgeType = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!(World.getInstance().findObject(_target) instanceof Player))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		final Player target = (Player) World.getInstance().findObject(_target);
		final Clan clan = player.getClan();
		if (!clan.checkClanJoinCondition(player, target, _pledgeType))
		{
			return;
		}
		
		if (!player.getRequest().setRequest(target, this))
		{
			return;
		}
		
		target.sendPacket(new AskJoinPledge(player.getObjectId(), player.getClan().getName()));
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
}