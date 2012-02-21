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
package org.craftmania.items.tools;

import org.craftmania.blocks.BlockManager;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class BedrockPickaxe extends Tool
{
	public BedrockPickaxe()
	{
		super("bedrock_pickaxe", null, Material.DIAMOND, new Vec2i(12, 6), 6.0f);
	}
	
	@Override
	public float calcDamageFactorToBlock(byte block)
	{
		if (block == BlockManager.getInstance().blockID("bedrock"))
		{
			return 100000.0f;
		}
		return 1.2f;
	}
}
