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

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TradeStart extends ServerPacket
{
	private final Player _player;
	private final List<Item> _itemList;
	
	public TradeStart(Player player)
	{
		_player = player;
		_itemList = _player.getInventory().getAvailableItems(true);
	}
	
	@Override
	public void write()
	{
		// 0x2e TradeStart d h (h dddhh dhhh)
		if ((_player.getActiveTradeList() == null) || (_player.getActiveTradeList().getPartner() == null))
		{
			return;
		}
		
		ServerPackets.TRADE_START.writeId(this);
		writeInt(_player.getActiveTradeList().getPartner().getObjectId());
		// writeInt((_activeChar != null || _activeChar.getTransactionRequester() != null)? _activeChar.getTransactionRequester().getObjectId() : 0);
		writeShort(_itemList.size());
		for (Item item : _itemList)// int i = 0; i < count; i++)
		{
			writeShort(item.getTemplate().getType1()); // item type1
			writeInt(item.getObjectId());
			writeInt(item.getItemId());
			writeInt(item.getCount());
			writeShort(item.getTemplate().getType2()); // item type2
			writeShort(0); // ?
			writeInt(item.getTemplate().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeShort(item.getEnchantLevel()); // enchant level
			writeShort(0); // ?
			writeShort(0);
		}
	}
}
