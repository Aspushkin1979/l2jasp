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
import org.l2jasp.gameserver.model.TradeList;
import org.l2jasp.gameserver.model.World;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;
import org.l2jasp.gameserver.network.serverpackets.TradeOtherAdd;
import org.l2jasp.gameserver.network.serverpackets.TradeOwnAdd;
import org.l2jasp.gameserver.network.serverpackets.TradeUpdate;

public class AddTradeItem implements ClientPacket
{
	private int _tradeId;
	private int _objectId;
	private int _count;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_tradeId = packet.readInt();
		_objectId = packet.readInt();
		_count = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null) // Trade null
		{
			PacketLogger.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check Partner and ocbjectId
		if ((trade.getPartner() == null) || (World.getInstance().findObject(trade.getPartner().getObjectId()) == null))
		{
			// Trade partner not found, cancel trade
			if (trade.getPartner() != null)
			{
				PacketLogger.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
			}
			
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check if player has Access level for Transaction
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level.");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check validateItemManipulation
		if (!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Java Emulator Security
		if ((player.getInventory().getItemByObjectId(_objectId) == null) || (_count <= 0))
		{
			PacketLogger.info("Character:" + player.getName() + " requested invalid trade object.");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList.TradeItem item = trade.addItem(_objectId, _count);
		if (item == null)
		{
			return;
		}
		
		if (item.isAugmented())
		{
			return;
		}
		
		player.sendPacket(new TradeOwnAdd(item));
		player.sendPacket(new TradeUpdate(trade, player));
		trade.getPartner().sendPacket(new TradeOtherAdd(item));
	}
}