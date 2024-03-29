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

import org.l2jasp.Config;
import org.l2jasp.commons.network.ReadablePacket;
import org.l2jasp.gameserver.data.xml.AdminData;
import org.l2jasp.gameserver.enums.ChatType;
import org.l2jasp.gameserver.instancemanager.PetitionManager;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.SystemMessageId;
import org.l2jasp.gameserver.network.serverpackets.CreatureSay;
import org.l2jasp.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (c) d
 * @author -Wooden-, TempyIncursion
 */
public class RequestPetitionCancel implements ClientPacket
{
	// private int _unknown;
	@Override
	public void read(ReadablePacket packet)
	{
		// _unknown = packet.readInt(); This is pretty much a trigger packet.
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerInConsultation(player))
		{
			if (player.isGM())
			{
				PetitionManager.getInstance().endActivePetition(player);
			}
			else
			{
				player.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
			}
		}
		else if (PetitionManager.getInstance().isPlayerPetitionPending(player))
		{
			if (PetitionManager.getInstance().cancelActivePetition(player))
			{
				final int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(player);
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITION_S_TODAY);
				sm.addString(String.valueOf(numRemaining));
				player.sendPacket(sm);
				
				// Notify all GMs that the player's pending petition has been cancelled.
				final String msgContent = player.getName() + " has canceled a pending petition.";
				AdminData.broadcastToGMs(new CreatureSay(player.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
			}
			else
			{
				player.sendPacket(SystemMessageId.FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_NOT_SUBMITTED_A_PETITION);
		}
	}
}
