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
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jasp.Config;
import org.l2jasp.gameserver.model.actor.Creature;
import org.l2jasp.gameserver.model.actor.instance.GrandBoss;
import org.l2jasp.gameserver.model.actor.instance.Monster;
import org.l2jasp.gameserver.model.actor.instance.Pet;
import org.l2jasp.gameserver.model.actor.instance.RaidBoss;

/**
 * @author DS
 */
public enum BaseStat
{
	STR(new STR()),
	INT(new INT()),
	DEX(new DEX()),
	WIT(new WIT()),
	CON(new CON()),
	MEN(new MEN()),
	NULL(new NULL());
	
	protected static final Logger LOGGER = Logger.getLogger(BaseStat.class.getName());
	
	public static final int MAX_STAT_VALUE = 100;
	
	protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
	protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
	protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
	protected static final double[] CONbonus = new double[MAX_STAT_VALUE];
	protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
	
	private final IBaseStatFunction _stat;
	
	public String getValue()
	{
		return _stat.getClass().getSimpleName();
	}
	
	private BaseStat(IBaseStatFunction s)
	{
		_stat = s;
	}
	
	public double calcBonus(Creature actor)
	{
		if (actor != null)
		{
			return _stat.calcBonus(actor);
		}
		
		return 1;
	}
	
	public static final BaseStat valueOfXml(String name)
	{
		String internName = name.intern();
		for (BaseStat s : values())
		{
			if (s.getValue().equalsIgnoreCase(internName))
			{
				return s;
			}
		}
		
		throw new NoSuchElementException("Unknown name '" + internName + "' for enum BaseStats");
	}
	
	protected static class STR implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getSTR() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has STR over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return STRbonus[MAX_STAT_VALUE];
			}
			return STRbonus[actor.getSTR()];
		}
	}
	
	protected static class INT implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getINT() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has INT over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return INTbonus[MAX_STAT_VALUE];
			}
			return INTbonus[actor.getINT()];
		}
	}
	
	protected static class DEX implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getDEX() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has DEX over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return DEXbonus[MAX_STAT_VALUE];
			}
			return DEXbonus[actor.getDEX()];
		}
	}
	
	protected static class WIT implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getWIT() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has WIT over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return WITbonus[MAX_STAT_VALUE];
			}
			return WITbonus[actor.getWIT()];
		}
	}
	
	protected static class CON implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getCON() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has CON over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return CONbonus[MAX_STAT_VALUE];
			}
			return CONbonus[actor.getCON()];
		}
	}
	
	protected static class MEN implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			if (((actor instanceof RaidBoss) || (actor instanceof GrandBoss)) && Config.ALT_RAIDS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Monster) && Config.ALT_MOBS_STATS_BONUS)
			{
				return 1;
			}
			
			if ((actor instanceof Pet) && Config.ALT_PETS_STATS_BONUS)
			{
				return 1;
			}
			
			if (actor.getMEN() > MAX_STAT_VALUE)
			{
				LOGGER.warning("Character " + actor.getName() + " has MEN over max value " + MAX_STAT_VALUE + "... Using " + MAX_STAT_VALUE);
				return MENbonus[MAX_STAT_VALUE];
			}
			return MENbonus[actor.getMEN()];
		}
	}
	
	protected static class NULL implements IBaseStatFunction
	{
		@Override
		public double calcBonus(Creature actor)
		{
			return 1f;
		}
	}
	
	static
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		final File file = new File(Config.DATAPACK_ROOT, "data/stats/statBonus.xml");
		Document doc = null;
		
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				LOGGER.warning("[BaseStats] Could not parse file: " + e.getMessage());
			}
			
			if (doc != null)
			{
				String statName;
				int val;
				double bonus;
				NamedNodeMap attrs;
				for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
				{
					if ("list".equalsIgnoreCase(list.getNodeName()))
					{
						for (Node stat = list.getFirstChild(); stat != null; stat = stat.getNextSibling())
						{
							statName = stat.getNodeName();
							for (Node value = stat.getFirstChild(); value != null; value = value.getNextSibling())
							{
								if ("stat".equalsIgnoreCase(value.getNodeName()))
								{
									attrs = value.getAttributes();
									try
									{
										val = Integer.parseInt(attrs.getNamedItem("value").getNodeValue());
										bonus = Double.parseDouble(attrs.getNamedItem("bonus").getNodeValue());
									}
									catch (Exception e)
									{
										LOGGER.warning("[BaseStats] Invalid stats value: " + value.getNodeValue() + ", skipping " + e);
										continue;
									}
									
									if ("STR".equalsIgnoreCase(statName))
									{
										STRbonus[val] = bonus;
									}
									else if ("INT".equalsIgnoreCase(statName))
									{
										INTbonus[val] = bonus;
									}
									else if ("DEX".equalsIgnoreCase(statName))
									{
										DEXbonus[val] = bonus;
									}
									else if ("WIT".equalsIgnoreCase(statName))
									{
										WITbonus[val] = bonus;
									}
									else if ("CON".equalsIgnoreCase(statName))
									{
										CONbonus[val] = bonus;
									}
									else if ("MEN".equalsIgnoreCase(statName))
									{
										MENbonus[val] = bonus;
									}
									else
									{
										LOGGER.warning("[BaseStats] Invalid stats name: " + statName + ", skipping");
									}
								}
							}
						}
					}
				}
			}
		}
		else
		{
			throw new Error("[BaseStats] File not found: " + file.getName());
		}
	}
}