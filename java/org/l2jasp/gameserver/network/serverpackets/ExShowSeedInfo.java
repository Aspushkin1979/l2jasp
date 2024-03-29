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

import org.l2jasp.gameserver.data.xml.ManorSeedData;
import org.l2jasp.gameserver.instancemanager.CastleManorManager.SeedProduction;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * format(packet 0xFE) ch ddd [dddddcdcd] c - id h - sub id d - manor id d d - size [ d - seed id d - left to buy d - started amount d - sell price d - seed level c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowSeedInfo extends ServerPacket
{
	private List<SeedProduction> _seeds;
	private final int _manorId;
	
	public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
		if (_seeds == null)
		{
			_seeds = new ArrayList<>();
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SHOW_SEED_INFO.writeId(this);
		writeByte(0);
		writeInt(_manorId); // Manor ID
		writeInt(0);
		writeInt(_seeds.size());
		for (SeedProduction seed : _seeds)
		{
			writeInt(seed.getId()); // Seed id
			writeInt(seed.getCanProduce()); // Left to buy
			writeInt(seed.getStartProduce()); // Started amount
			writeInt(seed.getPrice()); // Sell Price
			writeInt(ManorSeedData.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeByte(1); // reward 1 Type
			writeInt(ManorSeedData.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeByte(1); // reward 2 Type
			writeInt(ManorSeedData.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
}
