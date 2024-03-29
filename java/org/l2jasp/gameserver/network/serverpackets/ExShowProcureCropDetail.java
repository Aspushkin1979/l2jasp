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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jasp.gameserver.instancemanager.CastleManager;
import org.l2jasp.gameserver.instancemanager.CastleManorManager;
import org.l2jasp.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jasp.gameserver.model.siege.Castle;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * format(packet 0xFE) ch dd [dddc] c - id h - sub id d - crop id d - size [ d - manor name d - buy residual d - buy price c - reward type ]
 * @author l3x
 */
public class ExShowProcureCropDetail extends ServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			final CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if ((cropItem != null) && (cropItem.getAmount() > 0))
			{
				_castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SHOW_PROCURE_CROP_DETAIL.writeId(this);
		writeInt(_cropId); // crop id
		writeInt(_castleCrops.size()); // size
		for (Entry<Integer, CropProcure> entry : _castleCrops.entrySet())
		{
			final CropProcure crop = entry.getValue();
			writeInt(entry.getKey()); // manor name
			writeInt(crop.getAmount()); // buy residual
			writeInt(crop.getPrice()); // buy price
			writeByte(crop.getReward()); // reward type
		}
	}
}
