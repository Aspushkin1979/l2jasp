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
import org.l2jasp.gameserver.data.sql.CrestTable;
import org.l2jasp.gameserver.enums.CrestType;
import org.l2jasp.gameserver.model.Crest;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;

/**
 * Client packet for setting ally crest.
 */
public class RequestSetAllyCrest implements ClientPacket
{
	private int _length;
	private byte[] _data = null;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_length = packet.readInt();
		if (_length > 192)
		{
			return;
		}
		_data = packet.readBytes(_length);
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			player.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 192)
		{
			player.sendPacket(SystemMessageId.PLEASE_ADJUST_THE_IMAGE_SIZE_TO_8X12);
			return;
		}
		
		if (player.getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS);
			return;
		}
		
		final Clan leaderClan = ClanTable.getInstance().getClan(player.getAllyId());
		if ((player.getClanId() != leaderClan.getClanId()) || !player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS);
			return;
		}
		
		if (_length == 0)
		{
			if (leaderClan.getAllyCrestId() != 0)
			{
				leaderClan.changeAllyCrest(0, false);
			}
		}
		else
		{
			final Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.ALLY);
			if (crest != null)
			{
				leaderClan.changeAllyCrest(crest.getId(), false);
				player.sendMessage("The crest was successfully registered.");
			}
		}
	}
}
