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
import org.l2jasp.gameserver.network.serverpackets.ExConfirmVariationGemstone;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public class RequestConfirmGemStone implements ClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_targetItemObjId = packet.readInt();
		_refinerItemObjId = packet.readInt();
		_gemstoneItemObjId = packet.readInt();
		_gemstoneCount = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		final Item targetItem = (Item) World.getInstance().findObject(_targetItemObjId);
		final Item refinerItem = (Item) World.getInstance().findObject(_refinerItemObjId);
		final Item gemstoneItem = (Item) World.getInstance().findObject(_gemstoneItemObjId);
		if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null))
		{
			return;
		}
		
		// Make sure the item is a gemstone
		final int gemstoneItemId = gemstoneItem.getTemplate().getItemId();
		if ((gemstoneItemId != 2130) && (gemstoneItemId != 2131))
		{
			player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		// Check if the gemstoneCount is sufficant
		final int itemGrade = targetItem.getTemplate().getItemGrade();
		
		switch (itemGrade)
		{
			case ItemTemplate.CRYSTAL_C:
			{
				if ((_gemstoneCount != 20) || (gemstoneItemId != 2130))
				{
					player.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_B:
			{
				if ((_gemstoneCount != 30) || (gemstoneItemId != 2130))
				{
					player.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_A:
			{
				if ((_gemstoneCount != 20) || (gemstoneItemId != 2131))
				{
					player.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_S:
			{
				if ((_gemstoneCount != 25) || (gemstoneItemId != 2131))
				{
					player.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
		}
		
		player.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemstoneCount));
		player.sendPacket(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
}
