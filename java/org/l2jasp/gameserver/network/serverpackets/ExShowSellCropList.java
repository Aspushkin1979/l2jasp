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
import java.util.List;
import java.util.Map;

import org.l2jasp.gameserver.data.xml.ManorSeedData;
import org.l2jasp.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * format(packet 0xFE) ch dd [ddddcdcdddc] c - id h - sub id d - manor id d - size [ d - Object id d - crop id d - seed level c d - reward 1 id c d - reward 2 id d - manor d - buy residual d - buy price d - reward ]
 * @author l3x
 */
public class ExShowSellCropList extends ServerPacket
{
	private int _manorId = 1;
	private final Map<Integer, Item> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(Player player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();
		final List<Integer> allCrops = ManorSeedData.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			final Item item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		for (CropProcure crop : crops)
		{
			if (_cropsItems.containsKey(crop.getId()) && (crop.getAmount() > 0))
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SHOW_SELL_CROP_LIST.writeId(this);
		writeInt(_manorId); // manor id
		writeInt(_cropsItems.size()); // size
		for (Item item : _cropsItems.values())
		{
			writeInt(item.getObjectId()); // Object id
			writeInt(item.getItemId()); // crop id
			writeInt(ManorSeedData.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			writeByte(1);
			writeInt(ManorSeedData.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			writeByte(1);
			writeInt(ManorSeedData.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id
			if (_castleCrops.containsKey(item.getItemId()))
			{
				final CropProcure crop = _castleCrops.get(item.getItemId());
				writeInt(_manorId); // manor
				writeInt(crop.getAmount()); // buy residual
				writeInt(crop.getPrice()); // buy price
				writeByte(crop.getReward()); // reward
			}
			else
			{
				writeInt(0xFFFFFFFF); // manor
				writeInt(0); // buy residual
				writeInt(0); // buy price
				writeByte(0); // reward
			}
			writeInt(item.getCount()); // my crops
		}
	}
}
