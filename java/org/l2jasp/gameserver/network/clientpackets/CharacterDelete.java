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
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.serverpackets.CharDeleteFail;
import org.l2jasp.gameserver.network.serverpackets.CharDeleteOk;
import org.l2jasp.gameserver.network.serverpackets.CharSelectInfo;

/**
 * @author eX1steam
 */
public class CharacterDelete implements ClientPacket
{
	private int _charSlot;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_charSlot = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canSelectCharacter())
		{
			return;
		}
		
		try
		{
			final byte answer = client.markToDeleteChar(_charSlot);
			switch (answer)
			{
				default:
				case -1: // Error
				{
					break;
				}
				case 0: // Success!
				{
					client.sendPacket(new CharDeleteOk());
					break;
				}
				case 1:
				{
					client.sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				}
				case 2:
				{
					client.sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
				}
			}
		}
		catch (Exception e)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1, 0);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
