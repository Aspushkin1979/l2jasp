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

import org.l2jasp.gameserver.model.ShortCut;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * sample 56 01000000 04000000 dd9fb640 01000000 56 02000000 07000000 38000000 03000000 01000000 56 03000000 00000000 02000000 01000000 format dd d/dd/d d
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutRegister extends ServerPacket
{
	private final ShortCut _shortcut;
	
	/**
	 * Register new skill shortcut
	 * @param shortcut
	 */
	public ShortCutRegister(ShortCut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	public void write()
	{
		ServerPackets.SHORT_CUT_REGISTER.writeId(this);
		writeInt(_shortcut.getType());
		writeInt(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case ShortCut.TYPE_ITEM: // 1
			{
				writeInt(_shortcut.getId());
				break;
			}
			case ShortCut.TYPE_SKILL: // 2
			{
				writeInt(_shortcut.getId());
				writeInt(_shortcut.getLevel());
				writeByte(0); // C5
				break;
			}
			case ShortCut.TYPE_ACTION: // 3
			{
				writeInt(_shortcut.getId());
				break;
			}
			case ShortCut.TYPE_MACRO: // 4
			{
				writeInt(_shortcut.getId());
				break;
			}
			case ShortCut.TYPE_RECIPE: // 5
			{
				writeInt(_shortcut.getId());
				break;
			}
			default:
			{
				writeInt(_shortcut.getId());
			}
		}
		writeInt(1); // ??
	}
}
