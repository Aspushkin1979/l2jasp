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

import org.l2jasp.gameserver.model.quest.EventType;
import org.l2jasp.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */
public class Hallate extends Quest
{
	// Hallate NpcID
	private static final int HALLATE = 25220;
	
	public Hallate()
	{
		super(-1, "ai/bosses");
		
		addEventId(HALLATE, EventType.ON_ATTACK);
	}
	
	// FIXME: Asp - AI does nothing?
	
	public static void main(String[] args)
	{
		new Hallate();
	}
}
