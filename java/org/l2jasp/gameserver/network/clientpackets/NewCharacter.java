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

import org.l2jasp.gameserver.data.xml.PlayerTemplateData;
import org.l2jasp.gameserver.enums.ClassId;
import org.l2jasp.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jasp.gameserver.network.GameClient;
import org.l2jasp.gameserver.network.serverpackets.CharTemplates;

public class NewCharacter implements ClientPacket
{
	@Override
	public void run(GameClient client)
	{
		final CharTemplates ct = new CharTemplates();
		PlayerTemplate template = PlayerTemplateData.getInstance().getTemplate(0);
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.FIGHTER); // Human Fighter
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.MAGE); // Human Mage
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.ELVEN_FIGHTER); // Elf Fighter
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.ELVEN_MAGE); // Elf Mage
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.DARK_FIGHTER); // DE Fighter
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.DARK_MAGE); // DE Mage
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.ORC_FIGHTER); // Orc Fighter
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.ORC_MAGE); // Orc Mage
		ct.addChar(template);
		
		template = PlayerTemplateData.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER); // Dwarf Fighter
		ct.addChar(template);
		
		// Finally
		client.sendPacket(ct);
	}
}