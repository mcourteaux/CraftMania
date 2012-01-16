package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.math.Vec3f;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.BlockChunk;

public class TreeGenerator extends Generator
{
	private SmartRandom _random;

	public TreeGenerator(long seed)
	{
		super();
		_random = new SmartRandom(new Random(seed));
	}

	public void generateBroadLeavedTree(BlockChunk targetChunk, int _x, int _y, int _z, boolean flatBottom)
	{
		String leafType = "leafs" + _random.randomInt(2);
		int height = _random.randomInt(9, 13);

		int radius = _random.randomInt(3, (int) (height / 2.2f)) + (flatBottom ? 1 : 0);
		float radiusSq = radius * radius;
		height -= (flatBottom ? 1 : 0);

		// Treetrunk of wood
		for (int i = 0; i < height - 1; ++i)
		{
			byte bl = BlockManager.getInstance().blockID("wood0");
			targetChunk.setBlockTypeAbsolute(_x, _y + i, _z, bl, true, false);
		}

		// Leafs
		byte blLeafs = BlockManager.getInstance().blockID(leafType);
		for (int x = -radius; x < radius; ++x)
		{
			for (int y = -radius; y < radius; ++y)
			{
				for (int z = -radius; z < radius; ++z)
				{
					if (x == 0 && z == 0 && y < radius - 1)
					{
						continue;
					}
					if (flatBottom && y < -radius + 3)
					{
						continue;
					}
					Vec3f distanceFromCenter = new Vec3f(x, y, z);
					if (distanceFromCenter.lengthSquared() < radiusSq)
					{
						/*
						 * Do NOT generate (but create) the chunks where the
						 * leafs are in, otherwise this will cause continuously
						 * chunks, that contains tree which invokes the
						 * generation of a new chunk.
						 */
						targetChunk.setBlockTypeAbsolute(_x + x, _y + height - radius + y, _z + z, blLeafs, true, false);
					}
				}
			}
		}

	}

	public void generateCactus(BlockChunk targetChunk, int x, int y, int z)
	{
		int height = _random.randomInt(3, 5);
		for (int i = 0; i < height; ++i)
		{
			targetChunk.setBlockTypeAbsolute(x, y + i, z, BlockManager.getInstance().blockID("cactus"), true, false);
		}
	}

	public void generatePinophyta(BlockChunk chunk, int x, int y, int z)
	{
		// TODO Auto-generated method stub
		
	}
}
