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

import java.util.Map;
import java.util.Map.Entry;

import org.l2jasp.gameserver.network.ServerPackets;

/**
 * Format: ch ddd [ddd].
 * @author KenM
 */
public class ExGetBossRecord extends ServerPacket
{
	/** The _boss record info. */
	private final Map<Integer, Integer> _bossRecordInfo;
	/** The _ranking. */
	private final int _ranking;
	/** The _total points. */
	private final int _totalPoints;
	
	/**
	 * Instantiates a new ex get boss record.
	 * @param ranking the ranking
	 * @param totalScore the total score
	 * @param list the list
	 */
	public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = list;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_GET_BOSS_RECORD.writeId(this);
		writeInt(_ranking);
		writeInt(_totalPoints);
		if (_bossRecordInfo == null)
		{
			writeInt(0);
			writeInt(0);
			writeInt(0);
			writeInt(0);
		}
		else
		{
			writeInt(_bossRecordInfo.size());
			for (Entry<Integer, Integer> entry : _bossRecordInfo.entrySet())
			{
				writeInt(entry.getKey());
				writeInt(entry.getValue());
				writeInt(0); // ??
			}
		}
	}
}
