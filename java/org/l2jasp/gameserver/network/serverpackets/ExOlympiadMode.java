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
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadMode extends ServerPacket
{
	private static int _mode;
	private final Player _player;
	
	/**
	 * @param mode (0 = return, 3 = spectate)
	 * @param player
	 */
	public ExOlympiadMode(int mode, Player player)
	{
		_player = player;
		_mode = mode;
	}
	
	@Override
	public void write()
	{
		if (_player == null)
		{
			return;
		}
		
		if (_mode == 3)
		{
			_player.setObserverMode(true);
		}
		
		ServerPackets.EX_OLYMPIAD_MODE.writeId(this);
		writeByte(_mode);
	}
}
