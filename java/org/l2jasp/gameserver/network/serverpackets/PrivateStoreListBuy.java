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
 * @version $Revision: 1.7.2.2.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreListBuy extends ServerPacket
{
	private final Player _storePlayer;
	private final Player _player;
	private int _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(Player player, Player storePlayer)
	{
		_storePlayer = storePlayer;
		_player = player;
		if (Config.SELL_BY_ITEM)
		{
			_player.sendPacket(new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, "", "ATTENTION: Store System is not based on Adena, be careful!"));
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		// _storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		// this items must be the items available into the _activeChar (seller) inventory
		_items = _storePlayer.getBuyList().getAvailableItems(_player.getInventory());
	}
	
	@Override
	public void write()
	{
		ServerPackets.PRIVATE_STORE_LIST_BUY.writeId(this);
		writeInt(_storePlayer.getObjectId());
		writeInt(_playerAdena);
		writeInt(_items.size());
		for (TradeList.TradeItem item : _items)
		{
			writeInt(item.getObjectId());
			writeInt(item.getItem().getItemId());
			writeShort(item.getEnchant());
			// writeD(item.getCount()); //give max possible sell amount
			writeInt(item.getCurCount());
			writeInt(item.getItem().getReferencePrice());
			writeShort(0);
			writeInt(item.getItem().getBodyPart());
			writeShort(item.getItem().getType2());
			writeInt(item.getPrice()); // buyers price
			writeInt(item.getCount()); // maximum possible tradecount
		}
	}
}