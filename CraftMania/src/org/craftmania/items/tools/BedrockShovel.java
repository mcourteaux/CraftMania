package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class BedrockShovel extends Tool

{
	public BedrockShovel()
	{
		super("bedrock_shovel", BlockClass.SAND, Material.BEDROCK, new Vec2i(2, 16), 7.0f);
	}

}
