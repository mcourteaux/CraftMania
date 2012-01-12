package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class WoodenAxe extends Tool
{
	public WoodenAxe()
	{
		super("wooden_axe", BlockClass.WOOD, Material.WOOD, new Vec2i(0, 7), 7.2f);
	}
}
