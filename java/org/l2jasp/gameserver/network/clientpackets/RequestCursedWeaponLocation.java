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

import java.util.ArrayList;
import java.util.List;

import org.l2jasp.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jasp.gameserver.model.CursedWeapon;
import org.l2jasp.gameserver.model.Location;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.serverpackets.ExCursedWeaponLocation;
import org.l2jasp.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;

/**
 * Format: (ch)
 * @author -Wooden-
 */
public class RequestCursedWeaponLocation implements ClientPacket
{
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final List<CursedWeaponInfo> list = new ArrayList<>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
			{
				continue;
			}
			
			final Location location = cw.getWorldPosition();
			if (location != null)
			{
				list.add(new CursedWeaponInfo(location, cw.getItemId(), cw.isActivated() ? 1 : 0));
			}
		}
		
		if (!list.isEmpty())
		{
			player.sendPacket(new ExCursedWeaponLocation(list));
		}
	}
}
