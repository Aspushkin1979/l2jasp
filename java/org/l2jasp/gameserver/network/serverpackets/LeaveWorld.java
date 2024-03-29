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
 * @version $Revision: 1.3.3 $ $Date: 2009/05/12 19:06:39 $
 */
public class LeaveWorld extends ServerPacket
{
	public static final LeaveWorld STATIC_PACKET = new LeaveWorld();
	
	public LeaveWorld()
	{
	}
	
	@Override
	public void write()
	{
		ServerPackets.LEAVE_WORLD.writeId(this);
	}
}
