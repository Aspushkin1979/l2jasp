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

import java.util.List;

import org.l2jasp.commons.util.StringUtil;
import org.l2jasp.gameserver.network.ServerPackets;

public class ShowBoard extends ServerPacket
{
	public static final ShowBoard STATIC_SHOWBOARD_102 = new ShowBoard(null, "102");
	public static final ShowBoard STATIC_SHOWBOARD_103 = new ShowBoard(null, "103");
	
	private static final String TOP = "bypass _bbshome";
	private static final String FAV = "bypass _bbsgetfav";
	private static final String REGION = "bypass _bbsloc";
	private static final String CLAN = "bypass _bbsclan";
	private static final String MEMO = "bypass _bbsmemo";
	private static final String MAIL = "bypass _maillist_0_1_0_";
	private static final String FRIENDS = "bypass _friendlist_0_";
	private static final String ADDFAV = "bypass bbs_add_fav";
	
	private final StringBuilder _htmlCode = new StringBuilder();
	
	public ShowBoard(String htmlCode, String id)
	{
		StringUtil.append(_htmlCode, id, "\u0008", htmlCode);
	}
	
	public ShowBoard(List<String> arg)
	{
		_htmlCode.append("1002\u0008");
		for (String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.SHOW_BOARD.writeId(this);
		writeByte(1); // 1 to show, 0 to hide
		writeString(TOP);
		writeString(FAV);
		writeString(REGION);
		writeString(CLAN);
		writeString(MEMO);
		writeString(MAIL);
		writeString(FRIENDS);
		writeString(ADDFAV);
		writeString(_htmlCode.toString());
	}
}
