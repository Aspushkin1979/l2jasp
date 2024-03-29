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

import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * 15 ee cc 11 43 object id 39 00 00 00 item id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 01 00 00 00 show item count 7a 00 00 00 count . format dddddddd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SpawnItem extends ServerPacket
{
	private final int _objectId;
	private final int _itemId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final boolean _stackable;
	private final int _count;
	
	public SpawnItem(Item item)
	{
		_objectId = item.getObjectId();
		_itemId = item.getItemId();
		_x = item.getX();
		_y = item.getY();
		_z = item.getZ();
		_stackable = item.isStackable();
		_count = item.getCount();
	}
	
	@Override
	public void write()
	{
		ServerPackets.SPAWN_ITEM.writeId(this);
		writeInt(_objectId);
		writeInt(_itemId);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		// only show item count if it is a stackable item
		writeInt(_stackable);
		writeInt(_count);
		writeInt(0); // c2
	}
}
