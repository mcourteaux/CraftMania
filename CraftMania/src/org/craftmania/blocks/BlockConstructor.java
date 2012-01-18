package org.craftmania.blocks;

import java.lang.reflect.Constructor;

import org.craftmania.math.Vec3i;
import org.craftmania.world.BlockChunk;

public class BlockConstructor
{
	public static Block construct(int x, int y, int z, BlockChunk chunk, byte blockType, byte metadata)
	{
		if (blockType == 0)
			return null;
		BlockType type = BlockManager.getInstance().getBlockType(blockType);
		String customClass = type.getCustomClass();

		if (customClass == null)
		{
			return new DefaultBlock(type, chunk, new Vec3i(x, y, z));
		}
		try
		{
			Class<? extends Block> blockClass = (Class<? extends Block>) Class.forName(customClass);
			Constructor<? extends Block> constructor = blockClass.getConstructor(BlockChunk.class, Vec3i.class);
			Block block = constructor.newInstance(chunk, new Vec3i(x, y, z));
			return block;
		} catch (Exception e)
		{
			return null;
		}
	}
}
