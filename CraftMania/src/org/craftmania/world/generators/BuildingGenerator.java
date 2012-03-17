package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockType;
import org.craftmania.math.MathHelper;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Chunk;

public class BuildingGenerator extends Generator
{

	public BuildingGenerator()
	{

	}

	public void generateHouseAt(Chunk chunk, int _x, int _y, int _z)
	{
		SmartRandom random = new SmartRandom(new Random(_worldSeed & _x + _y ^ 0xF032DL + _z * _worldSeed));

		BlockType cobblestone = _blockManager.getBlockType("cobblestone");
		BlockType planks = _blockManager.getBlockType("planks");
		BlockType glass = _blockManager.getBlockType("glass");
		BlockType wood = _blockManager.getBlockType("wood0");
		BlockType sand = _blockManager.getBlockType("sand");
		BlockType dirt = _blockManager.getBlockType("dirt");
		BlockType grass = _blockManager.getBlockType("grass");

		int w = random.randomInt(6, 9);
		int d = MathHelper.clamp(70 / w + random.randomInt(-1, 2), 6, 100);
		int h = random.randomInt(3, 5);
		int windows = random.randomInt(6, 8);

		/* Clean the outside */
		for (int x = -2; x <= w + 2; ++x)
		{
			for (int z = -2; z <= d + 2; ++z)
			{
				chunk.removeBlockAbsolute(_x + x, _y, _z + z);
				chunk.setDefaultBlockAbsolute(_x + x, _y - 1, _z + z, grass, (byte) 0, true, true, false);
			}
		}

		/* Build the walls */
		for (int l = 0; l < h + 2; ++l)
		{
			BlockType type = planks;
			if (l == 0)
			{
				type = cobblestone;
			}

			for (int i = 0; i <= w; ++i)
			{
				chunk.setDefaultBlockAbsolute(_x + i, _y + l, _z, type, (byte) 0, true, true, false);
				chunk.setDefaultBlockAbsolute(_x + i, _y + l, _z + d, type, (byte) 0, true, true, false);
			}

			for (int i = 0; i <= d; ++i)
			{
				chunk.setDefaultBlockAbsolute(_x, _y + l, _z + i, type, (byte) 0, true, true, false);
				chunk.setDefaultBlockAbsolute(_x + w, _y + l, _z + i, type, (byte) 0, true, true, false);
			}
		}

		int doorX, doorZ;

		/* Cut out the door */
		{
			int x = random.randomInt(2, w - 2);
			int z = random.randomInt(2, d - 2);
			
			int side = random.randomInt(4);
			if (side == 0)
			{
				z = 0;
			} else if (side == 1)
			{
				x = 0;
			} else if (side == 2)
			{
				z = d;
			} else if (side == 3)
			{
				x = w;
			}
			
			chunk.removeBlockAbsolute(_x + x, _y, _z + z);
			chunk.removeBlockAbsolute(_x + x, _y + 1, _z + z);

			doorX = _x + x;
			doorZ = _z + z;
		}

		/* Make some windows */
		for (int i = 0; i < windows; ++i)
		{
			int side = random.randomInt(4);
			int x = _x;
			int z = _z;
			if (side == 0)
			{
				x = _x + random.randomInt(2, w - 2);
			} else if (side == 2)
			{
				x = _x + random.randomInt(2, w - 2);
				z = _z + d;
			} else if (side == 1)
			{
				z = _z + random.randomInt(2, d - 2);
			} else if (side == 3)
			{
				x = _x + w;
				z = _z + random.randomInt(2, d - 2);
			}

			if (!(Math.abs(x - doorX) <= 1 && Math.abs(z - doorZ) <= 1))
			{
				chunk.setDefaultBlockAbsolute(x, _y + 1, z, glass, (byte) 0, false, false, false);
			}
		}

		/* Build the roof */
		for (int x = 1; x < w; ++x)
		{
			for (int z = 1; z < d; ++z)
			{
				chunk.setDefaultBlockAbsolute(_x + x, _y + h, _z + z, wood, (byte) 0, false, false, false);
			}
		}

		/* Build the support */
		for (int i = 2; i < 5; ++i)
		{
			for (int x = -1; x <= w + 1; ++x)
			{
				for (int z = -1; z <= d + 1; ++z)
				{
					chunk.setDefaultBlockAbsolute(_x + x, _y - i, _z + z, dirt, (byte) 0, false, false, false);
				}
			}
		}

		/* Build the carpet */
		for (int x = 1; x < w; ++x)
		{
			for (int z = 1; z < d; ++z)
			{
				chunk.setDefaultBlockAbsolute(_x + x, _y - 1, _z + z, sand, (byte) 0, false, false, false);
			}
		}

		/* Clean the inside */
		for (int i = 0; i < h; ++i)
		{
			for (int x = 1; x < w; ++x)
			{
				for (int z = 1; z < d; ++z)
				{
					chunk.removeBlockAbsolute(_x + x, _y + i, _z + z);
				}
			}
		}

	}

}
