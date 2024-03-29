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
import org.l2jasp.gameserver.model.StoreTradeList;
import org.l2jasp.gameserver.model.item.ItemTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * sample 1d 1e 00 00 00 // ?? 5c 4a a0 7c // buy list id 02 00 // item count 04 00 // itemType1 0-weapon/ring/earring/necklace 1-armor/shield 4-item/questitem/adena 00 00 00 00 // objectid 32 04 00 00 // itemid 00 00 00 00 // count 05 00 // itemType2 0-weapon 1-shield/armor 2-ring/earring/necklace
 * 3-questitem 4-adena 5-item 00 00 60 09 00 00 // price 00 00 00 00 00 00 b6 00 00 00 00 00 00 00 00 00 00 00 80 00 // body slot these 4 values are only used if itemtype1 = 0 or 1 00 00 // 00 00 // 00 00 // 50 c6 0c 00 format dd h (h dddhh hhhh d) revision 377 format dd h (h dddhh dhhh d)
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class BuyList extends ServerPacket
{
	private final int _listId;
	private final List<Item> _list;
	private final int _money;
	private double _taxRate = 0;
	
	public BuyList(StoreTradeList list, int currentMoney)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
	}
	
	public BuyList(StoreTradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	public BuyList(List<Item> list, int listId, int currentMoney)
	{
		_listId = listId;
		_list = list;
		_money = currentMoney;
	}
	
	@Override
	public void write()
	{
		ServerPackets.BUY_LIST.writeId(this);
		writeInt(_money); // current money
		writeInt(_listId);
		writeShort(_list.size());
		for (Item item : _list)
		{
			if ((item.getCount() > 0) || (item.getCount() == -1))
			{
				writeShort(item.getTemplate().getType1()); // item type1
				writeInt(item.getObjectId());
				writeInt(item.getItemId());
				if (item.getCount() < 0)
				{
					writeInt(0); // max amount of items that a player can buy at a time (with this itemid)
				}
				else
				{
					writeInt(item.getCount());
				}
				writeShort(item.getTemplate().getType2()); // item type2
				writeShort(0); // ?
				if (item.getTemplate().getType1() != ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeInt(item.getTemplate().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeShort(item.getEnchantLevel()); // enchant level
					writeShort(0); // ?
					writeShort(0);
				}
				else
				{
					writeInt(0); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeShort(0); // enchant level
					writeShort(0); // ?
					writeShort(0);
				}
				if ((item.getItemId() >= 3960) && (item.getItemId() <= 4026))
				{
					writeInt((int) (item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				}
				else
				{
					writeInt((int) (item.getPriceToSell() * (1 + _taxRate)));
				}
			}
		}
	}
}
