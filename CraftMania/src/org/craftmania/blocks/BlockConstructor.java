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
package org.craftmania.blocks;

import java.lang.reflect.Constructor;

import org.craftmania.blocks.customblocks.TallGrass;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;

public class BlockConstructor
{
	@SuppressWarnings("unchecked")
	public static Block construct(int x, int y, int z, Chunk chunk, byte blockType, byte metadata)
	{
		if (blockType == 0)
			return null;
		BlockType type = BlockManager.getInstance().getBlockType(blockType);
		String customClass = type.getCustomClass();
		boolean crossed = type.isCrossed();
		if (crossed)
		{
			if (customClass == null)
			{
				return new CrossedBlock(type, chunk, new Vec3i(x, y, z));
			}
		} else
		{
			if (customClass == null)
			{
				return new DefaultBlock(type, chunk, new Vec3i(x, y, z));
			}
		}

		if (blockType == BlockManager.getInstance().blockID("tallgrass"))
		{
			return new TallGrass(chunk, new Vec3i(x, y, z), metadata);
		}

		try
		{
			Class<? extends Block> blockClass = (Class<? extends Block>) Class.forName(customClass);
			Constructor<? extends Block> constructor = blockClass.getConstructor(Chunk.class, Vec3i.class);
			Block block = constructor.newInstance(chunk, new Vec3i(x, y, z));
			return block;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
