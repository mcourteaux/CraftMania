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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.craftmania.game.Game;

public class RecipeManager
{
	private static RecipeManager __instance;

	public static RecipeManager getInstance()
	{
		if (__instance == null)
		{
			__instance = new RecipeManager();
		}
		return __instance;
	}
	
	public void loadRecipes() throws IOException
	{
		File file = Game.getInstance().getRelativeFile(Game.FILE_BASE_APPLICATION, "res/recipes.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		for (String line = br.readLine(); line != null; line = br.readLine())
		{
			line = line.trim();
			if (line.isEmpty() || line.startsWith("/*") || line.startsWith("//"))
			{
				continue;
			}
			
			String[] parts = line.split(" ");
			
			String recipe = parts[0];
			String result = parts[1];
			int amount = Integer.parseInt(parts[2]);
			
			addRecipe(new Recipe(recipe, result, amount));
		}
		
		br.close();
	}

	private List<Recipe> _recipes;

	private RecipeManager()
	{
		_recipes = new ArrayList<Recipe>();
	}

	public void addRecipe(Recipe r)
	{
		_recipes.add(r);
	}

	public Recipe getRecipe(int[][] ingredients)
	{
		for (Recipe r : _recipes)
		{
			if (r.equalsRecipe(ingredients))
			{
				return r;
			}
		}
		return null;
	}
}
