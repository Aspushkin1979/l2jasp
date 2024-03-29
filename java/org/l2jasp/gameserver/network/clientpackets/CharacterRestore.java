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
import org.l2jasp.gameserver.network.serverpackets.CharSelectInfo;

public class CharacterRestore implements ClientPacket
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
			client.markRestoredChar(_charSlot);
		}
		catch (Exception e)
		{
		}
		
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1, 0);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}