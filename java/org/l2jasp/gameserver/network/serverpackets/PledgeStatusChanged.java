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

import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * sample 0000: cd b0 98 a0 48 1e 01 00 00 00 00 00 00 00 00 00 ....H........... 0010: 00 00 00 00 00 ..... format ddddd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeStatusChanged extends ServerPacket
{
	private final Clan _clan;
	
	public PledgeStatusChanged(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public void write()
	{
		ServerPackets.PLEDGE_STATUS_CHANGED.writeId(this);
		writeInt(_clan.getLeaderId());
		writeInt(_clan.getClanId());
		writeInt(_clan.getCrestId());
		writeInt(_clan.getAllyId());
		writeInt(_clan.getAllyCrestId());
		writeInt(0);
		writeInt(0);
	}
}
