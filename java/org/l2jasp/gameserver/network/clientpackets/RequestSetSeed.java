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
package org.l2jasp.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jasp.Config;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.instancemanager.CastleManager;
import org.l2jasp.gameserver.instancemanager.CastleManorManager;
import org.l2jasp.gameserver.instancemanager.CastleManorManager.SeedProduction;
import org.l2jasp.gameserver.network.GameClient;

/**
 * Format: (ch) dd [ddd] d - manor id d - size [ d - seed id d - sales d - price ]
 * @author l3x
 */
public class RequestSetSeed implements ClientPacket
{
	private int _size;
	private int _manorId;
	private int[] _items; // _size*3
	
	@Override
	public void read(ReadablePacket packet)
	{
		_manorId = packet.readInt();
		_size = packet.readInt();
		if (((_size * 12) > packet.getRemainingLength()) || (_size > 500) || (_size < 1))
		{
			_size = 0;
			return;
		}
		
		_items = new int[_size * 3];
		for (int i = 0; i < _size; i++)
		{
			final int itemId = packet.readInt();
			_items[(i * 3) + 0] = itemId;
			final int sales = packet.readInt();
			_items[(i * 3) + 1] = sales;
			final int price = packet.readInt();
			_items[(i * 3) + 2] = price;
		}
	}
	
	@Override
	public void run(GameClient client)
	{
		if (_size < 1)
		{
			return;
		}
		
		final List<SeedProduction> seeds = new ArrayList<>();
		for (int i = 0; i < _size; i++)
		{
			final int id = _items[(i * 3) + 0];
			final int sales = _items[(i * 3) + 1];
			final int price = _items[(i * 3) + 2];
			if (id > 0)
			{
				final SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(_manorId).setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(_manorId).saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}
}
