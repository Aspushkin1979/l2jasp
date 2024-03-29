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

import java.util.Collection;

import org.l2jasp.gameserver.data.sql.ClanTable;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.model.siege.Castle;
import org.l2jasp.gameserver.model.siege.Fort;
import org.l2jasp.gameserver.model.siege.SiegeClan;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Populates the Siege Attacker List in the SiegeInfo Window<br>
 * <br>
 * packet type id 0xca<br>
 * format: cddddddd + dSSdddSSd<br>
 * <br>
 * c = ca<br>
 * d = CastleID<br>
 * d = unknown (0)<br>
 * d = unknown (1)<br>
 * d = unknown (0)<br>
 * d = Number of Attackers Clans?<br>
 * d = Number of Attackers Clans<br>
 * { //repeats<br>
 * d = ClanID<br>
 * S = ClanName<br>
 * S = ClanLeaderName<br>
 * d = ClanCrestID<br>
 * d = signed time (seconds)<br>
 * d = AllyID<br>
 * S = AllyName<br>
 * S = AllyLeaderName<br>
 * d = AllyCrestID<br>
 * @author KenM
 */
public class SiegeAttackerList extends ServerPacket
{
	private final int _residenceId;
	private final Collection<SiegeClan> _attackers;
	
	public SiegeAttackerList(Castle castle)
	{
		_residenceId = castle.getCastleId();
		_attackers = castle.getSiege().getAttackerClans();
	}
	
	public SiegeAttackerList(Fort fort)
	{
		_residenceId = fort.getFortId();
		_attackers = fort.getSiege().getAttackerClans();
	}
	
	@Override
	public void write()
	{
		ServerPackets.SIEGE_ATTACKER_LIST.writeId(this);
		writeInt(_residenceId);
		writeInt(0); // 0
		writeInt(1); // 1
		writeInt(0); // 0
		final int size = _attackers.size();
		if (size > 0)
		{
			Clan clan;
			writeInt(size);
			writeInt(size);
			for (SiegeClan siegeclan : _attackers)
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}
				writeInt(clan.getClanId());
				writeString(clan.getName());
				writeString(clan.getLeaderName());
				writeInt(clan.getCrestId());
				writeInt(0); // signed time (seconds) (not storated by L2J)
				writeInt(clan.getAllyId());
				writeString(clan.getAllyName());
				writeString(""); // AllyLeaderName
				writeInt(clan.getAllyCrestId());
			}
		}
		else
		{
			writeInt(0);
			writeInt(0);
		}
	}
}
