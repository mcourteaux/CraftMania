package org.craftmania.world.generators;

import java.awt.Polygon;
import java.util.Random;

import org.craftmania.blocks.BlockType;
import org.craftmania.math.MathHelper;
import org.craftmania.utilities.PerlinNoise;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Chunk;
import org.craftmania.world.WorldProvider;

public class FloatingIslandGenerator extends Generator
{

	private SmartRandom _random;
	private PerlinNoise _pNoise;
	private BlockType _dirt;
	private BlockType _grass;
	private WorldProvider _provider;

	public FloatingIslandGenerator(WorldProvider provider)
	{
		_dirt = _blockManager.getBlockType("dirt");
		_grass = _blockManager.getBlockType("grass");
		_provider = provider;
	}
	
	public void generateNiceFloatingIsland(Chunk chunk, int ix, int iy, int iz)
	{
		System.out.println("Gen Floating Island " + ix + ", " + iz);
		
		_random = new SmartRandom(new Random(_worldSeed + ix * 1024L + iy * 512L + iz));
		_pNoise = new PerlinNoise(_random.randomLong());
		
		int dimX = _random.randomInt(10, 20);
		int dimY = _random.randomInt(10, 20);
		int dimZ = _random.randomInt(10, 20);
		
		for (int x = 0; x < dimX; ++x)
		{
			for (int y = 0; y < dimY; ++y)
			{
				for (int z = 0; z < dimZ; ++z)
				{
					float noise = 100.0f * _pNoise.noise((float) x / dimX, (float) y / dimY, (float) z / dimZ);
					if (noise > 0.25f)
					{
						chunk.setDefaultBlockAbsolute(ix + x, iy + y, iz + z, _dirt, (byte) 0, true, true, false);
					}
				}
			}
		}
	}

	public void generateFloatingIsland(Chunk chunk, int ix, int iy, int iz)
	{
		_random = new SmartRandom(new Random(_worldSeed + ix * 1024L + iy * 512L + iz));
		_pNoise = new PerlinNoise(_random.randomLong());

		Polygon polygon = new Polygon();

		float size = _random.randomFloat(8, 14);
		int points = MathHelper.ceil(_random.randomFloat(size * size / 2.0f, size * size / 1.5f + 4));

		/* Generate random shape */
		{
			float s = size;
			for (int i = 0; i < points; ++i)
			{
				float angle = MathHelper.f_2PI * i / points;

				float pX = MathHelper.cos(angle);
				float pY = MathHelper.sin(angle);

				s = _random.randomFloat(s - size / 5.0f, s + size / 5.0f);
				s = MathHelper.clamp(s, size / 2.0f, size * 2.0f);

				polygon.addPoint((int) (pX * s), (int) (pY * s));
			}
		}

		int radius = MathHelper.ceil((float) Math.sqrt(polygon.getBounds2D().getWidth() * polygon.getBounds2D().getHeight()));

		int referenceHeight = _provider.getHeightAt(ix + 20, iz - 20);
		boolean treeBuilt = false;
		
		for (int x = -radius; x <= radius; ++x)
		{
			for (int z = -radius; z <= radius; ++z)
			{
				if (polygon.contains(x, z))
				{
						
					/* Generate land here */
					int height = _provider.getHeightAt(ix + x + 20, iz + z - 20) - referenceHeight;
					int depth = Math.abs(MathHelper.round((MathHelper.cos(x * z / 4.0f) * MathHelper.sin(x * z / 8.0f) * (size - MathHelper.sqrt(x * x + z * z) - 2))));
					for (int i = -depth - 2; i < height; ++i)
					{
						chunk.setDefaultBlockAbsolute(ix + x, iy + i, iz + z, _dirt, (byte) 0, true, true, false);
					}
					chunk.setDefaultBlockAbsolute(ix + x, iy + height, iz + z, _grass, (byte) 0, true, true, false);
				
					if (!treeBuilt && x * x + z * z < size * size - 5)
					{
						if (_random.randomInt(80) == 0)
						{
							treeBuilt = true;
							TreeGenerator gen = new TreeGenerator(_random.randomLong());
							gen.generateNiceBroadLeavedTree(chunk, ix + x, iy + height, iz + z);
						}
					}
				}
			}
		}
	}
}
