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

import org.l2jasp.gameserver.model.ItemInfo;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author Yme
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $ Rebuild 23.2.2006 by Advi
 */
public class PetInventoryUpdate extends ServerPacket
{
	private final List<ItemInfo> _items;
	
	/**
	 * @param items
	 */
	public PetInventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}
	
	public PetInventoryUpdate()
	{
		this(new ArrayList<ItemInfo>());
	}
	
	public void addItem(Item item)
	{
		_items.add(new ItemInfo(item));
	}
	
	public void addNewItem(Item item)
	{
		_items.add(new ItemInfo(item, 1));
	}
	
	public void addModifiedItem(Item item)
	{
		_items.add(new ItemInfo(item, 2));
	}
	
	public void addRemovedItem(Item item)
	{
		_items.add(new ItemInfo(item, 3));
	}
	
	public void addItems(List<Item> items)
	{
		for (Item item : items)
		{
			_items.add(new ItemInfo(item));
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.PET_INVENTORY_UPDATE.writeId(this);
		final int count = _items.size();
		writeShort(count);
		for (ItemInfo item : _items)
		{
			writeShort(item.getChange());
			writeShort(item.getItem().getType1()); // item type1
			writeInt(item.getObjectId());
			writeInt(item.getItem().getItemId());
			writeInt(item.getCount());
			writeShort(item.getItem().getType2()); // item type2
			writeShort(0); // ?
			writeShort(item.getEquipped());
			// writeShort(temp.getItem().getBodyPart()); // rev 377 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeInt(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeShort(item.getEnchant()); // enchant level
			writeShort(0); // ?
		}
	}
}
