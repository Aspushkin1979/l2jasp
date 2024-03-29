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

import java.util.ArrayList;
import java.util.List;

import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.instance.Pet;
import org.l2jasp.gameserver.model.actor.instance.Servitor;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends ServerPacket
{
	private final List<Effect> _effects;
	private final Creature _creature;
	
	private class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
	
	public PartySpelled(Creature creature)
	{
		_effects = new ArrayList<>();
		_creature = creature;
	}
	
	@Override
	public void write()
	{
		if (_creature == null)
		{
			return;
		}
		
		ServerPackets.PARTY_SPELLED.writeId(this);
		writeInt(_creature instanceof Servitor ? 2 : _creature instanceof Pet ? 1 : 0);
		writeInt(_creature.getObjectId());
		writeInt(_effects.size());
		for (Effect temp : _effects)
		{
			writeInt(temp._skillId);
			writeShort(temp._dat);
			writeInt(temp._duration / 1000);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
}
