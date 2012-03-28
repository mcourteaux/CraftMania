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
package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockConstructor;
import org.craftmania.blocks.BlockManager;
import org.craftmania.math.MathHelper;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Biome;
import org.craftmania.world.Chunk;
import org.craftmania.world.World;
import org.craftmania.world.WorldProvider;
import org.craftmania.world.WorldProvider.TreeDefinition;

public class ChunkGenerator extends Generator
{

	private WorldProvider _worldProvider;
	private static int SAMPLE_RATE_HORIZONTAL_DENSITY = 2;
	private static int SAMPLE_RATE_VERTICAL_DENSITY = 2;
	private int _x;
	private int _z;
	private int[][] _heightMap;
	private int[][] _temperatureMap;
	private int[][] _humidityMap;

	public ChunkGenerator(World world, int x, int z)
	{
		_worldProvider = world.getWorldProvider();
		_x = x;
		_z = z;
	}

	public Chunk generateChunk()
	{
		System.out.println("---------- Generate chunk: " + _x + ", " + _z);

		SmartRandom random = new SmartRandom(new Random(generateSeedForChunk(_worldSeed, _x, _z)));

		/* Access the new chunk */
		Chunk chunk = _chunkManager.getChunk(_x, _z, true, false, false);
		chunk.setGenerated(true);
		chunk.setLoading(true);

		/* Build the height map */
		_heightMap = new int[Chunk.CHUNK_SIZE_HORIZONTAL][Chunk.CHUNK_SIZE_HORIZONTAL];
		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; ++z)
			{
				_heightMap[x][z] = _worldProvider.getHeightAt(x + chunk.getAbsoluteX(), z + chunk.getAbsoluteZ());
			}
		}

		/* Build the temperature map */
		_temperatureMap = new int[Chunk.CHUNK_SIZE_HORIZONTAL][Chunk.CHUNK_SIZE_HORIZONTAL];
		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; ++z)
			{
				_temperatureMap[x][z] = _worldProvider.getTemperatureAt(x + chunk.getAbsoluteX(), z + chunk.getAbsoluteZ());
			}
		}

		/* Build the humidity map */
		_humidityMap = new int[Chunk.CHUNK_SIZE_HORIZONTAL][Chunk.CHUNK_SIZE_HORIZONTAL];
		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; ++z)
			{
				_humidityMap[x][z] = _worldProvider.getHumidityAt(x + chunk.getAbsoluteX(), z + chunk.getAbsoluteZ());
			}
		}

		/* Build a density map */
		float densityMap[][][] = new float[Chunk.CHUNK_SIZE_HORIZONTAL + 1][Chunk.CHUNK_SIZE_VERTICAL + 1][Chunk.CHUNK_SIZE_HORIZONTAL + 1];

		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL + 1; x += SAMPLE_RATE_HORIZONTAL_DENSITY)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL + 1; z += SAMPLE_RATE_HORIZONTAL_DENSITY)
			{
				for (int y = 0; y < Chunk.CHUNK_SIZE_VERTICAL + 1; y += SAMPLE_RATE_VERTICAL_DENSITY)
				{
					densityMap[x][y][z] = generateDensity(random, x, y, z);
				}
			}
		}

		/* Trilerp the density map */
		triLerpDensityMap(densityMap);

		/* Create the blocks using the density map */
		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; x++)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; z++)
			{
				int baseLevel = _heightMap[x][z];

				Biome topBiome = _worldProvider.calculateBiome(_worldProvider.calculateTemperature(_temperatureMap[x][z], baseLevel), _worldProvider.getHumidityAt(x + chunk.getAbsoluteX(), baseLevel, z + chunk.getAbsoluteZ()));

				for (int y = 0; y < Chunk.CHUNK_SIZE_VERTICAL && y <= baseLevel; y++)
				{
					if (y < 4)
					{
						/* Create a bedrock layer */
						chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("bedrock"), (byte) 0, true, false, false);
						continue;
					}
					int depth = baseLevel - y;
					if (topBiome == Biome.DESERT && y >= baseLevel - 3)
					{
						chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("sand"), (byte) 0, true, false, false);
					} else if (topBiome == Biome.SNOW && y == baseLevel)
					{
						chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("snow"), (byte) 0, true, false, false);
					} else if ((topBiome == Biome.FOREST || topBiome == Biome.FIELDS) && y == baseLevel)
					{
						chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("grass"), (byte) 0, true, false, false);
					} else
					{
						float density = densityMap[x][y][z];
						if (density < 7.3f && depth > 8)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("gravel"), (byte) 0, true, false, false);
						} else if (density < 6.3f)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("dirt"), (byte) 0, true, false, false);
						} else if (density < 9.0f)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("stone"), (byte) 0, true, false, false);
						} else if (density < 9.5f && depth > 5)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("coal_ore"), (byte) 0, true, false, false);
						} else if (density < 10.0f && depth > 10)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("iron_ore"), (byte) 0, true, false, false);
						} else if (y < 16 && density < 13.0f)
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("redstone_ore"), (byte) 0, true, false, false);
						} else
						{
							chunk.setDefaultBlockRelative(x, y, z, BlockManager.getInstance().getBlockType("stone"), (byte) 0, true, false, false);
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
				int x = chunk.getAbsoluteX() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);
				int z = chunk.getAbsoluteZ() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);

				/* Check for enough distance from the other trees */
				for (TreeDefinition treeDef : _worldProvider.getTrees())
				{
					float xDiff = x - treeDef.x;
					float zDiff = z - treeDef.z;

					float distSq = xDiff * xDiff + zDiff * zDiff;
					if (distSq < 60)
					{
						continue trees;
					}
				}
				int type = -1;
				int y = _heightMap[x - chunk.getAbsoluteX()][z - chunk.getAbsoluteZ()];

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
						gen.generateNiceBroadLeavedTree(chunk, x, y, z);
						// gen.generateBroadLeavedTree(chunk, x, y, z, true);
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

		/* Generate flora */
		{
			int grassCount = random.randomInt(5, 10);

			for (int i = 0; i < grassCount; ++i)
			{
				int x = chunk.getAbsoluteX() + random.randomInt(0, Chunk.CHUNK_SIZE_HORIZONTAL);
				int z = chunk.getAbsoluteZ() + random.randomInt(0, Chunk.CHUNK_SIZE_HORIZONTAL);
				int y = _heightMap[x - chunk.getAbsoluteX()][z - chunk.getAbsoluteZ()];

				Biome biome = _worldProvider.getBiomeAt(x, y, z);
				if (biome == Biome.FIELDS || biome == Biome.FOREST)
				{
					if (chunk.getBlockTypeAbsolute(x, y + 1, z, false, false, false) == 0)
					{
						chunk.setSpecialBlockAbsolute(x, y + 1, z, BlockConstructor.construct(x, y + 1, z, chunk, _blockManager.blockID("tallgrass"), (byte) random.randomInt(6)), false, false, false);
					}
				}
			}
		}
		{
			int flowerCount = random.randomInt(5, 10);

			for (int i = 0; i < flowerCount; ++i)
			{
				int x = chunk.getAbsoluteX() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);
				int z = chunk.getAbsoluteZ() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);
				int y = _heightMap[x - chunk.getAbsoluteX()][z - chunk.getAbsoluteZ()];

				Biome biome = _worldProvider.getBiomeAt(x, y, z);
				if (biome == Biome.FIELDS || biome == Biome.FOREST)
				{
					if (chunk.getBlockTypeAbsolute(x, y + 1, z, false, false, false) == 0)
					{
						if (random.randomBoolean())
						{
							chunk.setSpecialBlockAbsolute(x, y + 1, z, BlockConstructor.construct(x, y + 1, z, chunk, _blockManager.blockID("redflower"), (byte) 0), false, false, false);
						} else
						{
							chunk.setSpecialBlockAbsolute(x, y + 1, z, BlockConstructor.construct(x, y + 1, z, chunk, _blockManager.blockID("yellowflower"), (byte) 0), false, false, false);
						}
					}
				}
			}
		}

		/* Generate some floating islands */
		if (random.randomInt(35) == -1)
		{
			FloatingIslandGenerator gen = new FloatingIslandGenerator(_worldProvider);
			int x = chunk.getAbsoluteX() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);
			int z = chunk.getAbsoluteZ() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL);
			int y = _heightMap[x - chunk.getAbsoluteX()][z - chunk.getAbsoluteZ()];

			gen.generateFloatingIsland(chunk, x, y + random.randomInt(40, 80), z);
		}

		/* Generate a structure */
		if (random.randomInt(10) == 0)
		{
			BuildingGenerator gen = new BuildingGenerator();
			int x = chunk.getAbsoluteX() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL / 2);
			int z = chunk.getAbsoluteZ() + random.randomInt(Chunk.CHUNK_SIZE_HORIZONTAL / 2);
			int y = _heightMap[x - chunk.getAbsoluteX()][z - chunk.getAbsoluteZ()] + 1;

			if (_worldProvider.getBiomeAt(x, y, z) == Biome.FIELDS)
			{
				System.out.println("Generate building at (" + x + ", " + y + ", " + z + ")");
				gen.generateHouseAt(chunk, x, y, z);
			}
		}

		/* Make it accessible for the game */
		chunk.setLoading(false);
		/* Make sure the neighbors are assigned correctly */
		_chunkManager.assignNeighbors(chunk);

		return chunk;
	}

	/**
	 * Based on the code of Benjamin Glatzel in his project Terasology.
	 * 
	 * @author Benjamin Glatzel
	 * @author martijncourteaux
	 */
	protected void triLerpDensityMap(float[][][] densityMap)
	{
		for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; x++)
		{
			for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; z++)
			{
				for (int y = 0; y < _heightMap[x][z]; y++)
				{
					if (!(x % SAMPLE_RATE_HORIZONTAL_DENSITY == 0 && y % SAMPLE_RATE_VERTICAL_DENSITY == 0 && z % SAMPLE_RATE_HORIZONTAL_DENSITY == 0))
					{
						int offsetX = (x / SAMPLE_RATE_HORIZONTAL_DENSITY) * SAMPLE_RATE_HORIZONTAL_DENSITY;
						int offsetY = (y / SAMPLE_RATE_VERTICAL_DENSITY) * SAMPLE_RATE_VERTICAL_DENSITY;
						int offsetZ = (z / SAMPLE_RATE_HORIZONTAL_DENSITY) * SAMPLE_RATE_HORIZONTAL_DENSITY;
						densityMap[x][y][z] = MathHelper.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ], densityMap[offsetX][SAMPLE_RATE_VERTICAL_DENSITY + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY],
								densityMap[offsetX][offsetY + SAMPLE_RATE_VERTICAL_DENSITY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY
										+ SAMPLE_RATE_VERTICAL_DENSITY][offsetZ], densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY],
								densityMap[SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX][offsetY + SAMPLE_RATE_VERTICAL_DENSITY][offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY], offsetX, SAMPLE_RATE_HORIZONTAL_DENSITY + offsetX, offsetY, SAMPLE_RATE_VERTICAL_DENSITY + offsetY, offsetZ,
								offsetZ + SAMPLE_RATE_HORIZONTAL_DENSITY);
					}
				}
			}
		}
	}

	private float generateDensity(SmartRandom random, int x, int y, int z)
	{
		int baseLevel;
		if (x >= Chunk.CHUNK_SIZE_HORIZONTAL || z >= Chunk.CHUNK_SIZE_HORIZONTAL)
		{
			baseLevel = _heightMap[Chunk.CHUNK_SIZE_HORIZONTAL - 1][Chunk.CHUNK_SIZE_HORIZONTAL - 1];
		} else
		{
			baseLevel = _heightMap[x][z];
		}
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
