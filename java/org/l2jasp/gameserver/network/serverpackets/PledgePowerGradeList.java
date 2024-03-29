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

import org.l2jasp.gameserver.model.clan.Clan.RankPrivs;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * sample 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72 .....H.a.m.b.u.r 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 00 00 00000000 ... format dd ??
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgePowerGradeList extends ServerPacket
{
	private final Collection<RankPrivs> _privs;
	
	public PledgePowerGradeList(Collection<RankPrivs> privs)
	{
		_privs = privs;
	}
	
	@Override
	public void write()
	{
		ServerPackets.PLEDGE_POWER_GRADE_LIST.writeId(this);
		writeInt(_privs.size());
		for (RankPrivs priv : _privs)
		{
			writeInt(priv.getRank());
			writeInt(priv.getParty());
		}
	}
}
