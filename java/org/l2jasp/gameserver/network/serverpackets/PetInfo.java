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
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo extends ServerPacket
{
	private final Summon _summon;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final boolean _isSummoned;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private final int _maxHp;
	private final int _maxMp;
	private int _maxFed;
	private int _curFed;
	
	/**
	 * rev 478 dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 * @param summon
	 */
	public PetInfo(Summon summon)
	{
		_summon = summon;
		_isSummoned = _summon.isShowSummonAnimation();
		_x = _summon.getX();
		_y = _summon.getY();
		_z = _summon.getZ();
		_heading = _summon.getHeading();
		_mAtkSpd = _summon.getMAtkSpd();
		_pAtkSpd = _summon.getPAtkSpd();
		_runSpd = _summon.getRunSpeed();
		_walkSpd = _summon.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
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
		ServerPackets.PET_INFO.writeId(this);
		writeInt(_summon.getSummonType());
		writeInt(_summon.getObjectId());
		writeInt(_summon.getTemplate().getDisplayId() + 1000000);
		writeInt(0); // 1=attackable
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		writeInt(_heading);
		writeInt(0);
		writeInt(_mAtkSpd);
		writeInt(_pAtkSpd);
		writeInt(_runSpd);
		writeInt(_walkSpd);
		writeInt(_swimRunSpd);
		writeInt(_swimWalkSpd);
		writeInt(_flRunSpd);
		writeInt(_flWalkSpd);
		writeInt(_flyRunSpd);
		writeInt(_flyWalkSpd);
		writeDouble(1/* _cha.getProperMultiplier() */);
		writeDouble(1/* _cha.getAttackSpeedMultiplier() */);
		writeDouble(_summon.getTemplate().getFCollisionRadius());
		writeDouble(_summon.getTemplate().getFCollisionHeight());
		writeInt(0); // right hand weapon
		writeInt(0);
		writeInt(0); // left hand weapon
		writeByte(1); // name above char 1=true ... ??
		writeByte(_summon.isRunning()); // running=1
		writeByte(_summon.isInCombat()); // attacking 1=true
		writeByte(_summon.isAlikeDead()); // dead 1=true
		writeByte(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeString(_summon.getName());
		writeString(_summon.getTitle());
		writeInt(1);
		writeInt(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() : 0); // 0 = white,2= purpleblink, if its greater then karma = purple
		writeInt(_summon.getOwner() != null ? _summon.getOwner().getKarma() : 0); // karma
		writeInt(_curFed); // how fed it is
		writeInt(_maxFed); // max fed it can be
		writeInt((int) _summon.getCurrentHp()); // current hp
		writeInt(_maxHp); // max hp
		writeInt((int) _summon.getCurrentMp()); // current mp
		writeInt(_maxMp); // max mp
		writeInt(_summon.getStat().getSp()); // sp
		writeInt(_summon.getLevel()); // level
		writeLong(_summon.getStat().getExp());
		writeLong(_summon.getExpForThisLevel()); // 0% absolute value
		writeLong(_summon.getExpForNextLevel()); // 100% absoulte value
		writeInt(_summon instanceof Pet ? _summon.getInventory().getTotalWeight() : 0); // weight
		writeInt(_summon.getMaxLoad()); // max weight it can carry
		writeInt(_summon.getPAtk(null)); // patk
		writeInt(_summon.getPDef(null)); // pdef
		writeInt(_summon.getMAtk(null, null)); // matk
		writeInt(_summon.getMDef(null, null)); // mdef
		writeInt(_summon.getAccuracy()); // accuracy
		writeInt(_summon.getEvasionRate(null)); // evasion
		writeInt(_summon.getCriticalHit(null, null)); // critical
		writeInt(_runSpd); // speed
		writeInt(_summon.getPAtkSpd()); // atkspeed
		writeInt(_summon.getMAtkSpd()); // casting speed
		writeInt(0); // c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		final int npcId = _summon.getTemplate().getNpcId();
		if ((npcId >= 12526) && (npcId <= 12528))
		{
			writeShort(1); // c2 ride button
		}
		else
		{
			writeShort(0);
		}
		writeByte(0); // c2
		// Following all added in C4.
		writeShort(0); // ??
		writeByte(0); // team aura (1 = blue, 2 = red)
		writeInt(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeInt(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
	}
}
