package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.math.MathHelper;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Biome;
import org.craftmania.world.BlockChunk;
import org.craftmania.world.World;
import org.craftmania.world.WorldProvider;
import org.craftmania.world.WorldProvider.TreeDefinition;

public class ChunkGenerator extends Generator
{

	private WorldProvider _worldProvider;
	private static int SAMPLE_RATE_HORIZONTAL_DENSITY = 2;
	private static int SAMPLE_RATE_VERTICAL_DENSITY = 2;

	public ChunkGenerator(World world)
	{
		_worldProvider = world.getWorldProvider();
	}

	public BlockChunk generateChunk(int _x, int _z)
	{
		System.out.println("---------- Generate chunk: " + _x + ", " + _z);

		SmartRandom random = new SmartRandom(new Random(generateSeedForChunk(_worldSeed, _x, _z)));

		/* Access the new chunk */
		BlockChunk chunk = _chunkManager.getBlockChunk(_x, _z, true, false, false);
		chunk.setGenerated(true);
		chunk.setLoading(true);

		/* Build a density map */
		float densityMap[][][] = new float[BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL + 1][BlockChunk.BLOCKCHUNK_SIZE_VERTICAL + 1][BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL + 1];

		for (int x = 0; x < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL + 1; x += SAMPLE_RATE_HORIZONTAL_DENSITY)
		{
			for (int z = 0; z < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL + 1; z += SAMPLE_RATE_HORIZONTAL_DENSITY)
			{
				for (int y = 0; y < BlockChunk.BLOCKCHUNK_SIZE_VERTICAL + 1; y += SAMPLE_RATE_VERTICAL_DENSITY)
				{
					densityMap[x][y][z] = generateDensity(random, x + chunk.getAbsoluteX(), y, z + chunk.getAbsoluteZ());
				}
			}
		}

		/* Trilerp the density map */
		triLerpDensityMap(densityMap);

		/* Create the blocks using the density map */
		for (int x = 0; x < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; x++)
		{
			for (int z = 0; z < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; z++)
			{
				int baseLevel = _worldProvider.getHeightAt(x + chunk.getAbsoluteX(), z + chunk.getAbsoluteZ());

				Biome topBiome = _worldProvider.getBiomeAt(x + chunk.getAbsoluteX(), baseLevel, z + chunk.getAbsoluteZ());

				for (int y = 0; y < BlockChunk.BLOCKCHUNK_SIZE_VERTICAL && y <= baseLevel; y++)
				{
					if (y < 4)
					{
						/* Create a bedrock layer */
						chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("bedrock"), true, false, false);
						continue;
					}
					int depth = baseLevel - y;
					if (topBiome == Biome.DESERT && y >= baseLevel - 3)
					{
						chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("sand"), true, false, false);
					} else if (topBiome == Biome.SNOW && y == baseLevel)
					{
						chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("snow"), true, false, false);
					} else if ((topBiome == Biome.FOREST || topBiome == Biome.FIELDS) && y == baseLevel)
					{
						chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("grass"), true, false, false);
					} else
					{
						float density = densityMap[x][y][z];
						if (density < 7.3f && depth > 8)
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("gravel"), true, false, false);
						} else if (density < 6.3f)
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("dirt"), true, false, false);
						} else if (density < 9.0f)
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("stone"), true, false, false);
						} else if (density < 9.5f && depth > 5)
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("coal_ore"), true, false, false);
						} else if (density < 10.0f && depth > 10)
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("iron_ore"), true, false, false);
						} else
						{
							chunk.setBlockTypeRelative(x, y, z, BlockManager.getInstance().blockID("stone"), true, false, false);

						}
					}
				}
			}

		}

		/* Generate trees */
		{
			int treeCount = MathHelper.round((1.0f - random.exponentialRandom(1.0f, 3)) * 12.0f);
			TreeGenerator gen = new TreeGenerator(random.randomLong());
			trees: for (int i = 0; i < treeCount; ++i)
			{
				int x = chunk.getAbsoluteX() + random.randomInt(0, BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL);
				int z = chunk.getAbsoluteZ() + random.randomInt(0, BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL);

				/* Check for enough distance from the other trees */
				for (TreeDefinition treeDef : _worldProvider.getTrees())
				{
					float xDiff = x - treeDef.x;
					float zDiff = z - treeDef.z;

					float distSq = xDiff * xDiff + zDiff * zDiff;
					if (distSq < 17)
					{
						continue trees;
					}
				}
				int type = -1;
				int y = _worldProvider.getHeightAt(x, z);

				/*
				 * Check if the root of the tree is INSIDE THIS blockchunk, to
				 * prevent generating trees for one chunk multiple times
				 */
				// if (chunk.contains(x, y, z))
				{
					Biome biome = _worldProvider.getBiomeAt(x, y, z);
					if (biome == Biome.FIELDS)
					{
						continue;
					}
					if (biome == Biome.FOREST)
					{
						gen.generateBroadLeavedTree(chunk, x, y, z, random.randomInt(3) == 0);
						type = 0;
					} else if (biome == Biome.DESERT)
					{
						if (random.randomBoolean())
						{
							gen.generateCactus(chunk, x, y + 1, z);

							type = 1;
						}
					} else if (biome == Biome.SNOW)
					{
						gen.generatePinophyta(chunk, x, y, z);
						type = 2;
					}
					if (type != -1)
					{
						_worldProvider.getTrees().add(new TreeDefinition(x, y, z, type));
					}
				}
			}
		}

		/* Make it accessible for the game */
		chunk.setLoading(false);
		/* Make sure the neighbors are assigned correctly */
		_chunkManager.assignNeighbors(chunk);

		return chunk;
	}

	protected void triLerpDensityMap(float[][][] densityMap)
	{
		for (int x = 0; x < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; x++)
		{
			for (int y = 0; y < BlockChunk.BLOCKCHUNK_SIZE_VERTICAL; y++)
			{
				for (int z = 0; z < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; z++)
				{
					if (!(x % SAMPLE_RATE_HORIZONTAL_DENSITY == 0 && y % SAMPLE_RATE_VERTICAL_DENSITY == 0 && z % SAMPLE_RATE_HORIZONTAL_DENSITY == 0))
					{
						int offsetX = (x / SAMPLE_RATE_HORIZONTAL_DENSITY) * SAMPLE_RATE_HORIZONTAL_DENSITY;
						int offsetY = (y / SAMPLE_RATE_VERTICAL_DENSITY) * SAMPLE_RATE_VERTICAL_DENSITY;
						int offsetZ = (z / SAMPLE_RATE_HORIZONTAL_DENSITY) * SAMPLE_RATE_HORIZONTAL_DENSITY;
						densityMap[x][y][z] = (float) MathHelper.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ],
								densityMap[offsetX][SAMPLE_RATE_VERTICAL_DENSITY + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY],
								densityMap[offsetX][offsetY + SAMPLE_RATE_VERTICAL_DENSITY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY
										+ offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY + SAMPLE_RATE_VERTICAL_DENSITY][offsetZ],
								densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY
										+ offsetX][offsetY + SAMPLE_RATE_VERTICAL_DENSITY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY], offsetX, SAMPLE_RATE_HORIZONTAL_DENSITY
										+ offsetX, offsetY, SAMPLE_RATE_VERTICAL_DENSITY + offsetY, offsetZ, offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY);
					}
				}
			}
		}
	}

	private float generateDensity(SmartRandom random, int x, int y, int z)
	{
		int baseLevel = _worldProvider.getHeightAt(x, z);
		float depth = baseLevel - y;
		if (depth < 0)
		{
			return 0.0f;
		}
		return random.randomFloat(0.1f, 1.4f) * (float) Math.sqrt(random.randomFloat(1.8f * depth, 2.2f * depth) * depth);
	}

	private long generateSeedForChunk(long worldSeed, int x, int z)
	{
		return (((worldSeed << 3L) * x) ^ 0xF37C1E94L) + (worldSeed >> 1) * z;
	}
}
