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

import java.util.List;

import org.l2jasp.Config;
import org.l2jasp.gameserver.enums.ChatType;
import org.l2jasp.gameserver.model.TradeList;
import org.l2jasp.gameserver.model.TradeList.TradeItem;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell extends ServerPacket
{
	private final Player _storePlayer;
	private final Player _player;
	private int _playerAdena;
	private final boolean _packageSale;
	private final List<TradeItem> _items;
	
	// player's private shop
	public PrivateStoreListSell(Player player, Player storePlayer)
	{
		_player = player;
		_storePlayer = storePlayer;
		if (Config.SELL_BY_ITEM)
		{
			_player.sendPacket(new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, "", "ATTENTION: Store System is not based on Adena, be careful!"));
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		_storePlayer.getSellList().updateItems();
		_items = _storePlayer.getSellList().getItems();
		_packageSale = _storePlayer.getSellList().isPackaged();
	}
	
	@Override
	public void write()
	{
		ServerPackets.PRIVATE_STORE_LIST_SELL.writeId(this);
		writeInt(_storePlayer.getObjectId());
		writeInt(_packageSale);
		writeInt(_playerAdena);
		writeInt(_items.size());
		for (TradeList.TradeItem item : _items)
		{
			writeInt(item.getItem().getType2());
			writeInt(item.getObjectId());
			writeInt(item.getItem().getItemId());
			writeInt(item.getCount());
			writeShort(0);
			writeShort(item.getEnchant());
			writeShort(0);
			writeInt(item.getItem().getBodyPart());
			writeInt(item.getPrice()); // your price
			writeInt(item.getItem().getReferencePrice()); // store price
		}
	}
}