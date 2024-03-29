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
import org.l2jasp.gameserver.instancemanager.CastleManager;
import org.l2jasp.gameserver.instancemanager.FortManager;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.siege.Castle;
import org.l2jasp.gameserver.model.siege.Fort;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;

/**
 * @author KenM
 */
public class RequestJoinSiege implements ClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_castleId = packet.readInt();
		_isAttacker = packet.readInt();
		_isJoining = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isClanLeader())
		{
			return;
		}
		
		if (_castleId < 100)
		{
			final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
			if (castle == null)
			{
				return;
			}
			
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
				{
					player.sendPacket(SystemMessageId.YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION);
					return;
				}
				
				if (_isAttacker == 1)
				{
					castle.getSiege().registerAttacker(player);
				}
				else
				{
					castle.getSiege().registerDefender(player);
				}
			}
			else
			{
				castle.getSiege().removeSiegeClan(player);
			}
			
			castle.getSiege().listRegisterClan(player);
		}
		else
		{
			final Fort fort = FortManager.getInstance().getFortById(_castleId);
			if (fort == null)
			{
				return;
			}
			
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
				{
					player.sendPacket(SystemMessageId.YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION);
					return;
				}
				
				if (_isAttacker == 1)
				{
					fort.getSiege().registerAttacker(player);
				}
				else
				{
					fort.getSiege().registerDefender(player);
				}
			}
			else
			{
				fort.getSiege().removeSiegeClan(player);
			}
			
			fort.getSiege().listRegisterClan(player);
		}
	}
}
