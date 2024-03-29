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
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class CreatureSay extends ServerPacket
{
	private final int _objectId;
	private final ChatType _chatType;
	private final String _charName;
	private final String _text;
	
	/**
	 * @param objectId
	 * @param chatType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, ChatType chatType, String charName, String text)
	{
		super(128);
		
		_objectId = objectId;
		_chatType = chatType;
		_charName = charName;
		_text = text;
	}
	
	@Override
	public void write()
	{
		ServerPackets.CREATURE_SAY.writeId(this);
		writeInt(_objectId);
		writeInt(_chatType.getClientId());
		writeString(_charName);
		writeString(_text);
	}
	
	@Override
	public void run()
	{
		final Player player = getPlayer();
		if (player != null)
		{
			player.broadcastSnoop(_chatType, _charName, _text, this);
		}
	}
}
