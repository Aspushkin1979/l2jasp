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
import org.l2jasp.gameserver.data.SkillTable;
import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.skill.SkillType;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.PacketLogger;
import org.l2jasp.gameserver.network.serverpackets.ActionFailed;

/**
 * @version $Revision: 1.7.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestMagicSkillUse implements ClientPacket
{
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_magicId = packet.readInt(); // Identifier of the used skill
		_ctrlPressed = packet.readInt() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = packet.readByte() != 0; // True if Shift pressed
	}
	
	@Override
	public void run(GameClient client)
	{
		// Get the current Player of the player
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Get the level of the used skill
		final int level = player.getSkillLevel(_magicId);
		if (level <= 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the Skill template corresponding to the skillID received from the client
		final Skill skill = SkillTable.getInstance().getSkill(_magicId, level);
		
		// Check the validity of the skill
		if (skill != null)
		{
			// LOGGER.fine(" [FINE] skill:"+skill.getName() + " level:"+skill.getLevel() + " passive:"+skill.isPassive());
			// LOGGER.fine(" [FINE] range:"+skill.getCastRange()+" targettype:"+skill.getTargetType()+" optype:"+skill.getOperateType()+" power:"+skill.getPower());
			// LOGGER.fine(" [FINE] reusedelay:"+skill.getReuseDelay()+" hittime:"+skill.getHitTime());
			// LOGGER.fine(" [FINE] currentState:"+activeChar.getCurrentState()); //for debug
			
			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if ((skill.getSkillType() == SkillType.RECALL) && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (player.getKarma() > 0))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// players mounted on pets cannot use any toggle skills
			if (skill.isToggle() && player.isMounted())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			player.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			PacketLogger.warning("No skill found with id " + _magicId + " and level " + level + " !!");
		}
	}
}