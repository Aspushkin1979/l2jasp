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

import org.l2jasp.gameserver.enums.ChatType;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0)(type)(speaker)(name)
 */
public class Snoop extends ServerPacket
{
	private final Player _snooped;
	private final ChatType _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(Player snooped, ChatType chatType, String speaker, String msg)
	{
		_snooped = snooped;
		_type = chatType;
		_speaker = speaker;
		_msg = msg;
	}
	
	@Override
	public void write()
	{
		ServerPackets.SNOOP.writeId(this);
		writeInt(_snooped.getObjectId());
		writeString(_snooped.getName());
		writeInt(0); // ??
		writeInt(_type.getClientId());
		writeString(_speaker);
		writeString(_msg);
	}
}