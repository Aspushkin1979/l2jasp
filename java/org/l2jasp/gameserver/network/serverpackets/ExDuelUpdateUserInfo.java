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
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: ch Sddddddddd.
 * @author KenM
 */
public class ExDuelUpdateUserInfo extends ServerPacket
{
	/** The _active char. */
	private final Player _player;
	
	/**
	 * Instantiates a new ex duel update user info.
	 * @param player the cha
	 */
	public ExDuelUpdateUserInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_DUEL_UPDATE_USER_INFO.writeId(this);
		writeString(_player.getName());
		writeInt(_player.getObjectId());
		writeInt(_player.getClassId().getId());
		writeInt(_player.getLevel());
		writeInt((int) _player.getCurrentHp());
		writeInt(_player.getMaxHp());
		writeInt((int) _player.getCurrentMp());
		writeInt(_player.getMaxMp());
		writeInt((int) _player.getCurrentCp());
		writeInt(_player.getMaxCp());
	}
}
