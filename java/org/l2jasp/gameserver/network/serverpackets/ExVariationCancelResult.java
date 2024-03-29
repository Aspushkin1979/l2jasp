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
 * Format: (ch)ddd.
 */
public class ExVariationCancelResult extends ServerPacket
{
	/** The _close window. */
	private final int _closeWindow;
	/** The _unk1. */
	private final int _unk1;
	
	/**
	 * Instantiates a new ex variation cancel result.
	 * @param result the result
	 */
	public ExVariationCancelResult(int result)
	{
		_closeWindow = 1;
		_unk1 = result;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_VARIATION_CANCEL_RESULT.writeId(this);
		writeInt(_closeWindow);
		writeInt(_unk1);
	}
}
