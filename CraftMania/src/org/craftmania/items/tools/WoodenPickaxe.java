package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class WoodenPickaxe extends Tool
{

	public WoodenPickaxe()
	{
		super("wooden_pickaxe", BlockClass.STONE, Material.WOOD, new Vec2i(0, 6), 7.5f);
	}
}
