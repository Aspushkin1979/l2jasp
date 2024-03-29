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
package org.l2jasp.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jasp.gameserver.model.Skill;
import org.l2jasp.gameserver.model.clan.Clan;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: (ch) d [dd].
 * @author -Wooden-
 */
public class PledgeSkillList extends ServerPacket
{
	private final Collection<Skill> _skills;
	
	public PledgeSkillList(Clan clan)
	{
		_skills = clan.getAllSkills();
	}
	
	@Override
	public void write()
	{
		ServerPackets.PLEDGE_SKILL_LIST.writeId(this);
		writeInt(_skills.size());
		for (Skill skill : _skills)
		{
			writeInt(skill.getId());
			writeInt(skill.getLevel());
		}
	}
}
