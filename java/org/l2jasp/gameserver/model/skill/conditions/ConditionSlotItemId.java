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
package org.l2jasp.gameserver.model.skill.conditions;

import org.l2jasp.gameserver.model.actor.Player;
import org.l2jasp.gameserver.model.item.instance.Item;
import org.l2jasp.gameserver.model.itemcontainer.Inventory;
import org.l2jasp.gameserver.model.skill.Env;

/**
 * @author mkizub
 */
public class ConditionSlotItemId extends ConditionInventory
{
	private final int _itemId;
	private final int _enchantLevel;
	
	public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
	{
		super(slot);
		_itemId = itemId;
		_enchantLevel = enchantLevel;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof Player))
		{
			return false;
		}
		final Inventory inv = ((Player) env.player).getInventory();
		final Item item = inv.getPaperdollItem(_slot);
		if (item == null)
		{
			return _itemId == 0;
		}
		return (item.getItemId() == _itemId) && (item.getEnchantLevel() >= _enchantLevel);
	}
}
