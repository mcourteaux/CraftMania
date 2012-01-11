package org.craftmania.recipes;

import java.util.ArrayList;
import java.util.List;

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
