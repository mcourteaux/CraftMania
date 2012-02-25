package org.craftmania.items.tools;

import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class WoodenSword extends Tool
{

	public WoodenSword()
	{
		super("wooden_sword", null, Material.WOOD, new Vec2i(0, 4), 7.0f);
	}

}
