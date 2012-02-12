package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.math.Vec3f;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Chunk;

public class TreeGenerator extends Generator
{
	private SmartRandom _random;

	public TreeGenerator(long seed)
	{
		super();
		_random = new SmartRandom(new Random(seed));
	}

	public void generateBroadLeavedTree(Chunk targetChunk, int _x, int _y, int _z, boolean flatBottom)
	{
		String leafType = "leafs" + _random.randomInt(2);
		int height = _random.randomInt(9, 13);

		int radius = _random.randomInt(3, (int) (height / 2.2f)) + (flatBottom ? 1 : 0);
		float radiusSq = radius * radius;
		height -= (flatBottom ? 1 : 0);

		// Treetrunk of wood
		BlockType trunk = BlockManager.getInstance().getBlockType("wood0");
		for (int i = 0; i < height - 1; ++i)
		{
			targetChunk.setDefaultBlockAbsolute(_x, _y + i, _z, trunk, (byte) 0, true, true, false);
		}

		// Leafs
		BlockType blLeafs = BlockManager.getInstance().getBlockType(leafType);
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
						targetChunk.setDefaultBlockAbsolute(_x + x, _y + height - radius + y, _z + z, blLeafs, (byte) 0, true, true, false);
					}
				}
			}
		}
		

	}

	public void generateCactus(Chunk targetChunk, int x, int y, int z)
	{
		int height = _random.randomInt(3, 5);
		BlockType bt = BlockManager.getInstance().getBlockType("cactus");
		for (int i = 0; i < height; ++i)
		{
			targetChunk.setDefaultBlockAbsolute(x, y + i, z, bt, (byte) 0, true, true, false);
		}
	}

	public void generatePinophyta(Chunk chunk, int _x, int _y, int _z)
	{
		int trunkHeight = _random.randomInt(6, 8);
		int needlesHeight = _random.randomInt(trunkHeight, trunkHeight + 5);
		int needlesElevation = _random.randomInt(trunkHeight - 3, trunkHeight - 1);
		int needlesRadius = _random.randomInt(2, 4);
		
		BlockType wood = BlockManager.getInstance().getBlockType("wood1");
		BlockType needles = BlockManager.getInstance().getBlockType("needles");
		
		for (int h = 0; h <= needlesHeight; ++h)
		{
			int radius = (int) ((float) needlesRadius * (1.0f - ((float) h / (float)needlesHeight)));
			if (h % 2 == 1)
			{
				radius--;
			}
			if (radius < 0)
			{
				radius++;
			}
			for (int x = -radius; x <= radius; ++x)
			{
				for (int z = -radius; z <= radius; ++z)
				{
					if (x == 0 && z == 0 && h + needlesElevation < trunkHeight)
					{
						continue;
					}
					chunk.setDefaultBlockAbsolute(_x + x, _y + h + needlesElevation, _z + z, needles, (byte) 0, true, true, false);
				}
			}
		}
		
		// Treetrunk of wood
		for (int i = 0; i < trunkHeight; ++i)
		{
			chunk.setDefaultBlockAbsolute(_x, _y + i, _z, wood, (byte) 0, true, true, false);
		}
		
	}
}
