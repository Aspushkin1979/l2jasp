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
package ai.bosses;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jasp.gameserver.model.actor.Npc;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.model.quest.Quest;
import org.l2jasp.gameserver.model.siege.clanhalls.FortressOfResistance;

/**
 * @author Asp
 * @note Based on python script
 */
public class Nurka extends Quest
{
	// NPCs
	private static final int NURKA = 35368;
	private static final int MESSENGER = 35382;
	// Misc
	private static final Collection<Clan> _clans = ConcurrentHashMap.newKeySet();
	
	private Nurka()
	{
		super(-1, "ai/bosses");
		
		addTalkId(MESSENGER);
		addStartNpc(MESSENGER);
		addAttackId(NURKA);
		addKillId(NURKA);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final Clan playerClan = player.getClan();
		for (Clan clan : _clans)
		{
			if (clan == playerClan)
			{
				return "<html><body>You already registered!</body></html>";
			}
		}
		
		if (FortressOfResistance.getInstance().Conditions(player))
		{
			if (!_clans.contains(playerClan))
			{
				_clans.add(playerClan);
			}
			return "<html><body>You have successful registered on a siege.</body></html>";
		}
		
		return "<html><body>You are not allowed to do that!</body></html>";
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		final Clan playerClan = attacker.getClan();
		if (playerClan != null)
		{
			for (Clan clan : _clans)
			{
				if (clan == playerClan)
				{
					FortressOfResistance.getInstance().addSiegeDamage(clan, damage);
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		FortressOfResistance.getInstance().CaptureFinish();
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Nurka();
	}
}
