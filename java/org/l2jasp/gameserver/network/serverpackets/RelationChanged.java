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

import org.l2jasp.gameserver.model.actor.Playable;
import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.actor.instance.Servitor;
import org.l2jasp.gameserver.network.ServerPackets;

/**
 * @author Luca Baldi
 */
public class RelationChanged extends ServerPacket
{
	public static final int RELATION_PVP_FLAG = 0x00002; // pvp ???
	public static final int RELATION_HAS_KARMA = 0x00004; // karma ???
	public static final int RELATION_LEADER = 0x00080; // leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x08000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x10000; // single fist
	
	private final int _objId;
	private final int _relation;
	private final boolean _autoAttackable;
	private int _karma;
	private int _pvpFlag;
	
	public RelationChanged(Playable activeChar, int relation, boolean autoattackable)
	{
		_objId = activeChar.getObjectId();
		_relation = relation;
		_autoAttackable = autoattackable;
		if (activeChar instanceof Player)
		{
			_karma = ((Player) activeChar).getKarma();
			_pvpFlag = ((Player) activeChar).getPvpFlag();
		}
		else if (activeChar instanceof Servitor)
		{
			_karma = ((Servitor) activeChar).getOwner().getKarma();
			_pvpFlag = ((Servitor) activeChar).getOwner().getPvpFlag();
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.RELATION_CHANGED.writeId(this);
		writeInt(_objId);
		writeInt(_relation);
		writeInt(_autoAttackable);
		writeInt(_karma);
		writeInt(_pvpFlag);
	}
}