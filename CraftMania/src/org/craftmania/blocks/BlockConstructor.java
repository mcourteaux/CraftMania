package org.craftmania.blocks;

import org.craftmania.math.Vec3i;
import org.craftmania.world.BlockChunk;

public class BlockConstructor
{
	public static Block construct(int x, int y, int z, BlockChunk chunk, byte blockType, byte metadata)
	{
		if (blockType == 0) return null;
		return new DefaultBlock(BlockManager.getInstance().getBlockType(blockType), chunk, new Vec3i(x, y, z));
	}
}
