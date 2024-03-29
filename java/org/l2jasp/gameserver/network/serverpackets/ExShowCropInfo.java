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
import org.l2jasp.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: ch cddd[ddddcdcdcd] c - id (0xFE) h - sub id (0x1D) c d - manor id d d - size [ d - crop id d - residual buy d - start buy d - buy price c - reward type d - seed level c - reward 1 items d - reward 1 item id c - reward 2 items d - reward 2 item id ]
 * @author l3x
 */
public class ExShowCropInfo extends ServerPacket
{
	private List<CropProcure> _crops;
	private final int _manorId;
	
	public ExShowCropInfo(int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if (_crops == null)
		{
			_crops = new ArrayList<>();
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SHOW_CROP_INFO.writeId(this);
		writeByte(0);
		writeInt(_manorId); // Manor ID
		writeInt(0);
		writeInt(_crops.size());
		for (CropProcure crop : _crops)
		{
			writeInt(crop.getId()); // Crop id
			writeInt(crop.getAmount()); // Buy residual
			writeInt(crop.getStartAmount()); // Buy
			writeInt(crop.getPrice()); // Buy price
			writeByte(crop.getReward()); // Reward
			writeInt(ManorSeedData.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeByte(1); // reward 1 Type
			writeInt(ManorSeedData.getInstance().getRewardItem(crop.getId(), 1)); // Reward 1 Type Item Id
			writeByte(1); // reward 2 Type
			writeInt(ManorSeedData.getInstance().getRewardItem(crop.getId(), 2)); // Reward 2 Type Item Id
		}
	}
}
