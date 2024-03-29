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
package org.l2jasp.gameserver.model.zone.form;

import org.l2jasp.gameserver.model.zone.ZoneForm;

/**
 * A not so primitive npoly zone
 * @author durgus
 */
public class ZoneNPoly extends ZoneForm
{
	private final int[] _x;
	private final int[] _y;
	private final int _z1;
	private final int _z2;
	
	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
	}
	
	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		if ((z < _z1) || (z > _z2))
		{
			return false;
		}
		
		boolean inside = false;
		for (int i = 0, j = _x.length - 1; i < _x.length; j = i++)
		{
			if ((((_y[i] <= y) && (y < _y[j])) || ((_y[j] <= y) && (y < _y[i]))) && (x < ((((_x[j] - _x[i]) * (y - _y[i])) / (_y[j] - _y[i])) + _x[i])))
			{
				inside = !inside;
			}
		}
		return inside;
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		int tX;
		int tY;
		int uX;
		int uY;
		
		// First check if a point of the polygon lies inside the rectangle
		if ((_x[0] > ax1) && (_x[0] < ax2) && (_y[0] > ay1) && (_y[0] < ay2))
		{
			return true;
		}
		
		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, (_z2 - 1)))
		{
			return true;
		}
		
		// If the first point wasn't inside the rectangle it might still have any line crossing any side of the rectangle
		// Check every possible line of the polygon for a collision with any of the rectangles side
		for (int i = 0; i < _y.length; i++)
		{
			tX = _x[i];
			tY = _y[i];
			uX = _x[(i + 1) % _x.length];
			uY = _y[(i + 1) % _x.length];
			
			// Check if this line intersects any of the four sites of the rectangle
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test;
		double shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);
		for (int i = 1; i < _y.length; i++)
		{
			test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
			if (test < shortestDist)
			{
				shortestDist = test;
			}
		}
		return Math.sqrt(shortestDist);
	}
	
	@Override
	public int getLowZ()
	{
		return _z1;
	}
	
	@Override
	public int getHighZ()
	{
		return _z2;
	}
	
	@Override
	public void visualizeZone(int id, int z)
	{
		for (int i = 0; i < _x.length; i++)
		{
			int nextIndex = i + 1;
			// ending point to first one
			if (nextIndex == _x.length)
			{
				nextIndex = 0;
			}
			int vx = _x[nextIndex] - _x[i];
			int vy = _y[nextIndex] - _y[i];
			float length = (float) Math.sqrt((vx * vx) + (vy * vy));
			length /= STEP;
			for (int o = 1; o <= length; o++)
			{
				float k = o / length;
				dropDebugItem(id, (int) (_x[i] + (k * vx)), (int) (_y[i] + (k * vy)), z);
			}
		}
	}
}