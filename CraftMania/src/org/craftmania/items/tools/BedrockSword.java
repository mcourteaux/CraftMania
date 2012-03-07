package org.craftmania.items.tools;

import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class BedrockSword extends Tool
{

	public BedrockSword()
	{
		super("bedrock_sword", null, Material.BEDROCK, new Vec2i(4, 16), 5.0f);
	}

}
