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
import org.l2jasp.gameserver.data.xml.HennaData;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.Henna;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;

public class RequestHennaEquip implements ClientPacket
{
	private int _symbolId;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_symbolId = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			return;
		}
		
		if (!henna.canBeUsedBy(player))
		{
			player.sendPacket(SystemMessageId.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}
		
		if (player.getHennaEmptySlots() == 0)
		{
			player.sendPacket(SystemMessageId.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return;
		}
		
		final Item ownedDyes = player.getInventory().getItemByItemId(henna.getDyeId());
		final int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
		if (count < Henna.getRequiredDyeAmount())
		{
			player.sendPacket(SystemMessageId.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}
		
		// reduceAdena sends a message.
		if (!player.reduceAdena("Henna", henna.getPrice(), player.getLastFolkNPC(), true))
		{
			return;
		}
		
		// destroyItemByItemId sends a message.
		if (!player.destroyItemByItemId("Henna", henna.getDyeId(), Henna.getRequiredDyeAmount(), player, true))
		{
			return;
		}
		
		player.addHenna(henna);
	}
}
