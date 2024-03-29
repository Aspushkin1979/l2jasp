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

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.Henna;
import org.l2jasp.gameserver.network.ServerPackets;

public class HennaRemoveList extends ServerPacket
{
	private final Player _player;
	
	public HennaRemoveList(Player player)
	{
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.HENNA_REMOVE_LIST.writeId(this);
		writeInt(_player.getAdena());
		writeInt(_player.getHennaEmptySlots());
		writeInt(Math.abs(_player.getHennaEmptySlots() - 3));
		for (int i = 1; i <= 3; i++)
		{
			final Henna henna = _player.getHenna(i);
			if (henna != null)
			{
				writeInt(henna.getSymbolId());
				writeInt(henna.getDyeId());
				writeInt(Henna.getRequiredDyeAmount() / 2);
				writeInt(henna.getPrice() / 5);
				writeInt(1);
			}
		}
	}
}