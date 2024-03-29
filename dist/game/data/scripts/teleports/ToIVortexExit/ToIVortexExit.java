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
package teleports.ToIVortexExit;

import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.quest.Quest;

/**
 * @author Asp
 * @note Based on python script
 */
public class ToIVortexExit extends Quest
{
	// NPC
	private static final int TELE_CUBE = 29055;
	
	private ToIVortexExit()
	{
		super(-1, "teleports");
		
		addStartNpc(TELE_CUBE);
		addTalkId(TELE_CUBE);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		int x;
		int y;
		int z;
		final int chance = getRandom(3);
		if (chance == 0)
		{
			x = 108784 + getRandom(100);
			y = 16000 + getRandom(100);
			z = -4928;
		}
		else if (chance == 1)
		{
			x = 113824 + getRandom(100);
			y = 10448 + getRandom(100);
			z = -5164;
		}
		else
		{
			x = 115488 + getRandom(100);
			y = 22096 + getRandom(100);
			z = -5168;
		}
		player.teleToLocation(x, y, z);
		return null;
	}
	
	public static void main(String[] args)
	{
		new ToIVortexExit();
	}
}