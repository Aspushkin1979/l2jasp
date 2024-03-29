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
package org.l2jasp.gameserver.model.skill;

import java.io.File;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jasp.Config;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author Nik
 */
public class hitConditionBonus
{
	protected static final Logger LOGGER = Logger.getLogger(hitConditionBonus.class.getName());
	
	private static int frontBonus = 0;
	private static int sideBonus = 0;
	private static int backBonus = 0;
	private static int highBonus = 0;
	private static int lowBonus = 0;
	private static int darkBonus = 0;
	
	protected static double getConditionBonus(Creature attacker, Creature target)
	{
		double mod = 100;
		// Get high or low bonus
		if ((attacker.getZ() - target.getZ()) > 50)
		{
			mod += highBonus;
		}
		else if ((attacker.getZ() - target.getZ()) < -50)
		{
			mod += lowBonus;
		}
		
		// Get weather bonus
		if (GameTimeTaskManager.getInstance().isNight())
		{
			mod += darkBonus;
		}
		
		// Get side bonus
		if (attacker.isBehindTarget())
		{
			mod += backBonus;
		}
		else if (attacker.isFrontTarget())
		{
			mod += frontBonus;
		}
		else
		{
			mod += sideBonus;
		}
		
		// If (mod / 10) is less than 0, return 0, because we cant lower more than 100%.
		return Math.max(mod / 100, 0);
	}
	
	static
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		final File file = new File(Config.DATAPACK_ROOT, "data/stats/hitConditionBonus.xml");
		if (file.exists())
		{
			try
			{
				String name;
				final Document doc = factory.newDocumentBuilder().parse(file);
				for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
				{
					if ("hitConditionBonus".equalsIgnoreCase(list.getNodeName()) || "list".equalsIgnoreCase(list.getNodeName()))
					{
						for (Node cond = list.getFirstChild(); cond != null; cond = cond.getNextSibling())
						{
							int bonus = 0;
							name = cond.getNodeName();
							try
							{
								if (cond.hasAttributes())
								{
									bonus = Integer.parseInt(cond.getAttributes().getNamedItem("val").getNodeValue());
								}
							}
							catch (Exception e)
							{
								LOGGER.warning("[hitConditionBonus] Could not parse condition: " + e.getMessage());
							}
							finally
							{
								switch (name)
								{
									case "front":
									{
										frontBonus = bonus;
										break;
									}
									case "side":
									{
										sideBonus = bonus;
										break;
									}
									case "back":
									{
										backBonus = bonus;
										break;
									}
									case "high":
									{
										highBonus = bonus;
										break;
									}
									case "low":
									{
										lowBonus = bonus;
										break;
									}
									case "dark":
									{
										darkBonus = bonus;
										break;
									}
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("[hitConditionBonus] Could not parse file: " + e.getMessage());
			}
		}
		else
		{
			throw new Error("[hitConditionBonus] File not found: " + file.getName());
		}
	}
}