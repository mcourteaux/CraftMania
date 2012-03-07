package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class BedrockAxe extends Tool

{

	public BedrockAxe()
	{
		 super("bedrock_axe", BlockClass.WOOD, Material.BEDROCK, new Vec2i(1, 16), 7.0f);		 
	}
}
