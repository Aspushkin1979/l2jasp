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

import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format (ch)dddcc
 * @author -Wooden-
 */
public class ExFishingStartCombat extends ServerPacket
{
	private final Creature _creature;
	private final int _time;
	private final int _hp;
	private final int _lureType;
	private final int _deceptiveMode;
	private final int _mode;
	
	public ExFishingStartCombat(Creature creature, int time, int hp, int mode, int lureType, int deceptiveMode)
	{
		_creature = creature;
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_FISHING_START_COMBAT.writeId(this);
		writeInt(_creature.getObjectId());
		writeInt(_time);
		writeInt(_hp);
		writeByte(_mode); // mode: 0 = resting, 1 = fighting
		writeByte(_lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		writeByte(_deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
}
