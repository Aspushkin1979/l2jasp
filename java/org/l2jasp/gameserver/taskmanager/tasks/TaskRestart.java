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
package org.l2jasp.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import org.l2jasp.gameserver.Shutdown;
import org.l2jasp.gameserver.taskmanager.Task;
import org.l2jasp.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public class TaskRestart extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskRestart.class.getName());
	public static final String NAME = "restart";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		LOGGER.info("[GlobalTask] Server Restart launched.");
		Shutdown.getInstance().startShutdown(null, Integer.parseInt(task.getParams()[2]), true);
	}
}