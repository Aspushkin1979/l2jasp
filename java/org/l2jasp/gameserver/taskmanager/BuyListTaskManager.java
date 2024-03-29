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
package org.l2jasp.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jasp.commons.threads.ThreadPool;
import org.l2jasp.gameserver.instancemanager.TradeManager;

/**
 * @author Asp
 */
public class BuyListTaskManager
{
	private static final Map<Integer, Long> REFRESH_TIME = new ConcurrentHashMap<>();
	private static final List<Integer> PENDING_UPDATES = new ArrayList<>();
	private static boolean _workingTimes = false;
	private static boolean _workingSaves = false;
	
	protected BuyListTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new BuyListTimeTask(), 1000, 60000);
		ThreadPool.scheduleAtFixedRate(new BuyListSaveTask(), 50, 50);
	}
	
	protected class BuyListTimeTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingTimes)
			{
				return;
			}
			_workingTimes = true;
			
			final long currentTime = System.currentTimeMillis();
			for (Entry<Integer, Long> entry : REFRESH_TIME.entrySet())
			{
				if (currentTime > entry.getValue().longValue())
				{
					final Integer time = entry.getKey();
					synchronized (PENDING_UPDATES)
					{
						if (!PENDING_UPDATES.contains(time))
						{
							PENDING_UPDATES.add(time);
						}
					}
					REFRESH_TIME.put(time, currentTime + (time.intValue() * 60 * 60 * 1000L));
				}
			}
			
			_workingTimes = false;
		}
	}
	
	protected class BuyListSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingSaves)
			{
				return;
			}
			_workingSaves = true;
			
			if (!PENDING_UPDATES.isEmpty())
			{
				final Integer time;
				synchronized (PENDING_UPDATES)
				{
					time = PENDING_UPDATES.get(0);
					PENDING_UPDATES.remove(time);
				}
				TradeManager.getInstance().restoreCount(time.intValue());
				TradeManager.getInstance().dataTimerSave(time.intValue());
			}
			
			_workingSaves = false;
		}
	}
	
	public void addTime(int timeValue, long updateValue)
	{
		final Integer time = Integer.valueOf(timeValue);
		if (updateValue == 0)
		{
			synchronized (PENDING_UPDATES)
			{
				if (!PENDING_UPDATES.contains(time))
				{
					PENDING_UPDATES.add(time);
				}
			}
			REFRESH_TIME.put(time, System.currentTimeMillis() + (timeValue * 60 * 60 * 1000L));
		}
		else
		{
			REFRESH_TIME.put(time, updateValue);
		}
	}
	
	public static BuyListTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BuyListTaskManager INSTANCE = new BuyListTaskManager();
	}
}
