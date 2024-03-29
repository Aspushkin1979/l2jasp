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

import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharCreateFail extends ServerPacket
{
	public static final int REASON_CREATION_FAILED = 0;
	public static final int REASON_TOO_MANY_CHARACTERS = 1;
	public static final int REASON_NAME_ALREADY_EXISTS = 2;
	public static final int REASON_16_ENG_CHARS = 3;
	public static final int REASON_INCORRECT_NAME = 4;
	
	private final int _error;
	
	public CharCreateFail(int errorCode)
	{
		_error = errorCode;
	}
	
	@Override
	public void write()
	{
		ServerPackets.CHAR_CREATE_FAIL.writeId(this);
		writeInt(_error);
	}
}
