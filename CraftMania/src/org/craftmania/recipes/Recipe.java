/*******************************************************************************
 * Copyright 2012 Martijn Courteaux <martijn.courteaux@skynet.be>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.craftmania.recipes;

import org.craftmania.items.ItemManager;


/**
 * 
 * @author martijncourteaux
 */
public class Recipe
{

	private int _resultingItem;
	private int _resultAmount;
	private int[][] _ingredients;
	private int _width;
	private int _height;

	public Recipe(String ingredients, int result, int resultAmount)
	{
		this._resultAmount = resultAmount;
		this._resultingItem = result;
		this._ingredients = parseIngredients(ingredients);
		this._height = _ingredients.length;
		this._width = _ingredients[0].length;
	}

	public static int[][] parseIngredients(String ingredients)
	{
		String[] lines = ingredients.split(";");
		int height = lines.length;

		int width = 0;
		for (String line : lines)
		{
			width = Math.max(width, line.split(",").length);
		}
		int[][] ret = new int[height][width];

		for (int y = 0; y < height; ++y)
		{
			String line = lines[y];
			String[] elems = line.split(",");
			for (int x = 0; x < elems.length; ++x)
			{
				int elem = 0;
				if (elems[x].length() > 0)
				{
					try
					{
						elem = Integer.parseInt(elems[x]);
					} catch (Exception e)
					{
						elem = ItemManager.getInstance().getItemID(elems[x]);
					}
				}

				ret[y][x] = elem;
			}
		}

		return ret;
	}

	public Recipe(String recipe, String result, int count)
	{
		this(recipe, ItemManager.getInstance().getItemID(result), count);
	}

	public int getHeight()
	{
		return _height;
	}

	public int getWidth()
	{
		return _width;
	}

	public int[][] getIngredients()
	{
		return _ingredients;
	}

	public int getResultingItem()
	{
		return _resultingItem;
	}

	public int getResultAmount()
	{
		return _resultAmount;
	}

	public boolean equalsRecipe(int[][] rec)
	{
		int minX = 10;
		int minY = 10;
		int maxX = 0;
		int maxY = 0;
		boolean containsData = false;

		for (int y = 0; y < rec.length; ++y)
		{
			for (int x = 0; x < rec[0].length; ++x)
			{
				int elem = rec[y][x];
				if (elem != 0)
				{
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
					containsData = true;
				}
			}
		}
		if (!containsData)
		{
			return false;
		}

		int w = maxX - minX + 1;
		int h = maxY - minY + 1;

		if (w != _width || h != _height)
		{
			return false;
		}

		for (int x = 0; x < _width; ++x)
		{
			for (int y = 0; y < _height; ++y)
			{
				if (rec[minY + y][minX + x] != _ingredients[y][x])
				{
					return false;
				}
			}
		}
		return true;
	}
}
