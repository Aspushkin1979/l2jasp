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
import org.l2jasp.gameserver.model.WorldObject;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;

public class Action implements ClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	private int _actionId;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_objectId = packet.readInt(); // Target object Identifier
		_originX = packet.readInt();
		_originY = packet.readInt();
		_originZ = packet.readInt();
		_actionId = packet.readByte(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformPlayerAction())
		{
			return;
		}
		
		// Get the current Player of the player
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final WorldObject obj;
		if (player.getTargetId() == _objectId)
		{
			obj = player.getTarget();
		}
		else
		{
			obj = World.getInstance().findObject(_objectId);
		}
		
		// If object requested does not exist
		// pressing e.g. pickup many times quickly would get you here
		if (obj == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't interact with objects in the other instances except from multiverse
		if ((obj.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters
		if ((obj instanceof Player) && (((Player) obj).getAppearance().isInvisible()) && !player.isGM())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (player.getPrivateStoreType() == 0/* && activeChar.getActiveRequester() == null */)
		{
			switch (_actionId)
			{
				case 0:
				{
					obj.onAction(player);
					break;
				}
				case 1:
				{
					if ((obj instanceof Creature) && ((Creature) obj).isAlikeDead())
					{
						obj.onAction(player);
					}
					else
					{
						obj.onActionShift(client);
					}
					break;
				}
				default:
				{
					// Invalid action detected (probably client cheating), LOGGER this
					PacketLogger.warning("Character: " + player.getName() + " requested invalid action: " + _actionId);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					break;
				}
			}
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET); // Actions prohibited when in trade
		}
	}
}