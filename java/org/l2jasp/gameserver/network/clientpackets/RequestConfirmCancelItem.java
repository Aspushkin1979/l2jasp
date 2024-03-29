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

import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.ItemTemplate;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ExConfirmCancelItem;

/**
 * Format(ch) d
 * @author -Wooden-
 */
public class RequestConfirmCancelItem implements ClientPacket
{
	private int _itemId;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_itemId = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		final Item item = (Item) World.getInstance().findObject(_itemId);
		if ((player == null) || (item == null))
		{
			return;
		}
		
		if (!item.isAugmented())
		{
			player.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}
		
		int price = 0;
		switch (item.getTemplate().getItemGrade())
		{
			case ItemTemplate.CRYSTAL_C:
			{
				if (item.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (item.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_B:
			{
				if (item.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_A:
			{
				if (item.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (item.getCrystalCount() < 2824)
				{
					price = 390000;
				}
				else
				{
					price = 420000;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_S:
			{
				price = 480000;
				break;
			}
			// any other item type is not augmentable
			default:
			{
				return;
			}
		}
		
		player.sendPacket(new ExConfirmCancelItem(_itemId, price));
	}
}
