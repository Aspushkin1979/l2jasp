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

import org.l2jasp.gameserver.network.ServerPackets;

/**
 * <code>
 * sample
 * 
 * a4
 * 4d000000 01000000 98030000 			Attack Aura, level 1, sp cost
 * 01000000 							number of requirements
 * 05000000 47040000 0100000 000000000	   1 x spellbook advanced ATTACK                                                 .
 * </code> format ddd d (dddd)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class AquireSkillInfo extends ServerPacket
{
	private final List<Req> _reqs;
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final int _mode;
	
	private class Req
	{
		public int itemId;
		public int count;
		public int type;
		public int unk;
		
		public Req(int pType, int pItemId, int pCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
	
	public AquireSkillInfo(int id, int level, int spCost, int mode)
	{
		_reqs = new ArrayList<>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_mode = mode;
	}
	
	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}
	
	@Override
	public void write()
	{
		ServerPackets.AQUIRE_SKILL_INFO.writeId(this);
		writeInt(_id);
		writeInt(_level);
		writeInt(_spCost);
		writeInt(_mode); // c4
		writeInt(_reqs.size());
		for (Req temp : _reqs)
		{
			writeInt(temp.type);
			writeInt(temp.itemId);
			writeInt(temp.count);
			writeInt(temp.unk);
		}
	}
}
