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

import org.l2jasp.gameserver.model.actor.Summon;
import org.l2jasp.gameserver.model.actor.instance.Pet;
import org.l2jasp.gameserver.model.actor.instance.Servitor;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.5.2.3.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusUpdate extends ServerPacket
{
	private final Summon _summon;
	private final int _maxHp;
	private final int _maxMp;
	private int _maxFed;
	private int _curFed;
	
	public PetStatusUpdate(Summon summon)
	{
		_summon = summon;
		_maxHp = _summon.getMaxHp();
		_maxMp = _summon.getMaxMp();
		if (_summon instanceof Pet)
		{
			final Pet pet = (Pet) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (_summon instanceof Servitor)
		{
			final Servitor sum = (Servitor) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.PET_STATUS_UPDATE.writeId(this);
		writeInt(_summon.getSummonType());
		writeInt(_summon.getObjectId());
		writeInt(_summon.getX());
		writeInt(_summon.getY());
		writeInt(_summon.getZ());
		writeString(_summon.getOwner().getName());
		writeInt(_curFed);
		writeInt(_maxFed);
		writeInt((int) _summon.getCurrentHp());
		writeInt(_maxHp);
		writeInt((int) _summon.getCurrentMp());
		writeInt(_maxMp);
		writeInt(_summon.getLevel());
		writeLong(_summon.getStat().getExp());
		writeLong(_summon.getExpForThisLevel()); // 0% absolute value
		writeLong(_summon.getExpForNextLevel()); // 100% absolute value
	}
}
