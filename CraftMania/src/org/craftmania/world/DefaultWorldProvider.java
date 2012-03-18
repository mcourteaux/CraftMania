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
package org.craftmania.world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.craftmania.utilities.IOUtilities;
import org.craftmania.utilities.SmartRandom;

public class DefaultWorldProvider extends WorldProvider
{

	public static final int SAMPLE_RATE_HORIZONTAL = 8;
	public static final int SAMPLE_RATE_VERTICAL = 8;
	public static final int SAMPLE_RATE_HEIGHTS = 4;
	public static final int SAMPLE_RATE_RAW_HEIGHS = 32;

	public static int SAMPLE_RATE_TEMPERATURE = 32;

	private static boolean DEBUG_WOLRD_PROVIDER = true;

	private World _world;
	private WorldProviderGenerator _generator;
	private List<DataPoint2D> _rawHeights;
	private List<DataPoint2D> _heights;
	private List<DataPoint2D> _humidities;
	private List<DataPoint2D> _temperatures;
	private List<TreeDefinition> _trees;
	private Vec3f _spawnPoint;

	public DefaultWorldProvider(World world)
	{
		_world = world;
		_generator = new WorldProviderGenerator(world);
		_rawHeights = new ArrayList<DefaultWorldProvider.DataPoint2D>();
		_heights = new ArrayList<DefaultWorldProvider.DataPoint2D>();
		_humidities = new ArrayList<DefaultWorldProvider.DataPoint2D>();
		_trees = new ArrayList<TreeDefinition>();
		_temperatures = new ArrayList<DefaultWorldProvider.DataPoint2D>();
	}

	
	public List<TreeDefinition> getTrees()
	{
		return _trees;
	}

	private int biLerpDataPoints(int x, int z, DataPoint2D q11, DataPoint2D q12, DataPoint2D q21, DataPoint2D q22)
	{
		return MathHelper.round(MathHelper.biLerp(x, z, q11.getData(), q12.getData(), q21.getData(), q22.getData(), q11.getX(), q22.getX(), q11.getZ(), q22.getZ()));
	}

	private boolean isPoint(DataPoint2D point, int x, int z)
	{
		return point._x == x && point._z == z;
	}
	
	@Override
	public int getTemperatureAt(int x, int z)
	{
		int lowerX = x;
		int lowerZ = z;

		lowerX = MathHelper.floor((float) x / SAMPLE_RATE_TEMPERATURE) * SAMPLE_RATE_TEMPERATURE;
		lowerZ = MathHelper.floor((float) z / SAMPLE_RATE_TEMPERATURE) * SAMPLE_RATE_TEMPERATURE;

		int upperX = lowerX + SAMPLE_RATE_TEMPERATURE;
		int upperZ = lowerZ + SAMPLE_RATE_TEMPERATURE;

		DataPoint2D q11, q12, q21, q22;
		q11 = q12 = q21 = q22 = null;

		synchronized (_temperatures)
		{

			for (DataPoint2D temp : _temperatures)
			{
				if (isPoint(temp, x, z))
				{
					return temp.getData();
				}

				int hX = temp._x;
				int hZ = temp._z;

				if (lowerX == hX && lowerZ == hZ)
				{
					q11 = temp;
				} else if (lowerX == hX && upperZ == hZ)
				{
					q12 = temp;
				} else if (upperX == hX && lowerZ == hZ)
				{
					q21 = temp;
				} else if (upperX == hX && upperZ == hZ)
				{
					q22 = temp;
				}

				if (q11 != null && q12 != null && q21 != null && q22 != null)
				{
					break;
				}
			}

			if (q11 == null || q12 == null || q21 == null || q22 == null)
			{
				// System.out.println("No temperature data found for this coordinates ("
				// + x + ", " + z + ") !! So, will be generated...");
				if (q11 == null)
				{
					q11 = _generator.generateTemperatureAt(lowerX, lowerZ);
					if (isPoint(q11, x, z))
					{
						return q11._data;
					}
				}
				if (q12 == null)
				{
					q12 = _generator.generateTemperatureAt(lowerX, upperZ);
					if (isPoint(q12, x, z))
					{
						return q12._data;
					}
				}
				if (q21 == null)
				{
					q21 = _generator.generateTemperatureAt(upperX, lowerZ);
					if (isPoint(q21, x, z))
					{
						return q21._data;
					}
				}
				if (q22 == null)
				{
					q22 = _generator.generateTemperatureAt(upperX, upperZ);
					if (isPoint(q22, x, z))
					{
						return q22._data;
					}
				}
			}
		}

		return (int) biLerpDataPoints(x, z, q11, q12, q21, q22);
	}

	@Override
	public int getTemperatureAt(int x, int y, int z)
	{
		int temperature2D = getTemperatureAt(x, z);
		return calculateTemperature(temperature2D, y);
	}
	
	public int calculateTemperature(int temperature, int y)
	{
		return (int) MathHelper.clamp(temperature - y / 3.0f + 10.0f, 2, 40);
	}
	
	public int getHumidityAt(int x, int y, int z)
	{
		int humidity = getHumidityAt(x, z);
		return (int) MathHelper.clamp(humidity * 10 / getTemperatureAt(x, y, z), 0, 100);
	}


	@Override
	public int getHumidityAt(int x, int z)
	{
		int lowerX = x;
		int lowerZ = z;

		lowerX = MathHelper.floorDivision(x, SAMPLE_RATE_HORIZONTAL) * SAMPLE_RATE_HORIZONTAL;
		lowerZ = MathHelper.floorDivision(z, SAMPLE_RATE_HORIZONTAL) * SAMPLE_RATE_HORIZONTAL;

		int upperX = lowerX + SAMPLE_RATE_HORIZONTAL;
		int upperZ = lowerZ + SAMPLE_RATE_HORIZONTAL;

		DataPoint2D q11, q12, q21, q22;
		q11 = q12 = q21 = q22 = null;
		synchronized (_humidities)
		{

			for (DataPoint2D humidity : _humidities)
			{
				if (isPoint(humidity, x, z))
				{
					return humidity.getData();
				}

				int hX = humidity._x;
				int hZ = humidity._z;

				if (lowerX == hX && lowerZ == hZ)
				{
					q11 = humidity;
				} else if (lowerX == hX && upperZ == hZ)
				{
					q12 = humidity;
				} else if (upperX == hX && lowerZ == hZ)
				{
					q21 = humidity;
				} else if (upperX == hX && upperZ == hZ)
				{
					q22 = humidity;
				}

				if (q11 != null && q12 != null && q21 != null && q22 != null)
				{
					break;
				}
			}

			if (q11 == null || q12 == null || q21 == null || q22 == null)
			{
				// System.out.println("No humidity data found for this coordinates ("
				// + x + ", " + z + ") !! So, will be generated...");
				if (q11 == null)
				{
					q11 = _generator.generateHumidityAt(lowerX, lowerZ);
					if (isPoint(q11, x, z))
					{
						return q11._data;
					}
				}
				if (q12 == null)
				{
					q12 = _generator.generateHumidityAt(lowerX, upperZ);
					if (isPoint(q12, x, z))
					{
						return q12._data;
					}
				}
				if (q21 == null)
				{
					q21 = _generator.generateHumidityAt(upperX, lowerZ);
					if (isPoint(q21, x, z))
					{
						return q21._data;
					}
				}
				if (q22 == null)
				{
					q22 = _generator.generateHumidityAt(upperX, upperZ);
					if (isPoint(q22, x, z))
					{
						return q22._data;
					}
				}
			}
		}
		float humidity2D = biLerpDataPoints(x, z, q11, q12, q21, q22);

		return MathHelper.clamp((int) humidity2D, 10, 95);
	}

	@Override
	public int getHeightAt(int x, int z)
	{
		int lowerX = x;
		int lowerZ = z;

		lowerX = MathHelper.floorDivision(x, SAMPLE_RATE_HEIGHTS) * SAMPLE_RATE_HEIGHTS;
		lowerZ = MathHelper.floorDivision(z, SAMPLE_RATE_HEIGHTS) * SAMPLE_RATE_HEIGHTS;

		int upperX = lowerX + SAMPLE_RATE_HEIGHTS;
		int upperZ = lowerZ + SAMPLE_RATE_HEIGHTS;

		DataPoint2D q11, q12, q21, q22;
		q11 = q12 = q21 = q22 = null;

		synchronized (_heights)
		{

			for (DataPoint2D height : _heights)
			{
				if (isPoint(height, x, z))
				{
					return height.getData();
				}

				int hX = height._x;
				int hZ = height._z;

				if (lowerX == hX && lowerZ == hZ)
				{
					q11 = height;
				} else if (lowerX == hX && upperZ == hZ)
				{
					q12 = height;
				} else if (upperX == hX && lowerZ == hZ)
				{
					q21 = height;
				} else if (upperX == hX && upperZ == hZ)
				{
					q22 = height;
				}

				if (q11 != null && q12 != null && q21 != null && q22 != null)
				{
					break;
				}
			}

			if (q11 == null || q12 == null || q21 == null || q22 == null)
			{
				// System.out.println("No level data found for this coordinates ("
				// +
				// x + ", " + z + ") !! So, will be generated...");
				if (q11 == null)
				{
					q11 = _generator.generateHeightAt(lowerX, lowerZ);
					if (isPoint(q11, x, z))
					{
						return q11._data;
					}
				}
				if (q12 == null)
				{
					q12 = _generator.generateHeightAt(lowerX, upperZ);
					if (isPoint(q12, x, z))
					{
						return q12._data;
					}
				}
				if (q21 == null)
				{
					q21 = _generator.generateHeightAt(upperX, lowerZ);
					if (isPoint(q21, x, z))
					{
						return q21._data;
					}
				}
				if (q22 == null)
				{
					q22 = _generator.generateHeightAt(upperX, upperZ);
					if (isPoint(q22, x, z))
					{
						return q22._data;
					}
				}
			}
		}

		return biLerpDataPoints(x, z, q11, q12, q21, q22);
	}

	public int getRawHeightAt(int x, int z)
	{
		int lowerX = x;
		int lowerZ = z;

		lowerX = MathHelper.floorDivision(x, SAMPLE_RATE_RAW_HEIGHS) * SAMPLE_RATE_RAW_HEIGHS;
		lowerZ = MathHelper.floorDivision(z, SAMPLE_RATE_RAW_HEIGHS) * SAMPLE_RATE_RAW_HEIGHS;

		int upperX = lowerX + SAMPLE_RATE_RAW_HEIGHS;
		int upperZ = lowerZ + SAMPLE_RATE_RAW_HEIGHS;

		DataPoint2D q11, q12, q21, q22;
		q11 = q12 = q21 = q22 = null;

		synchronized (_rawHeights)
		{

			for (DataPoint2D height : _rawHeights)
			{
				if (isPoint(height, x, z))
				{
					return height.getData();
				}

				int hX = height._x;
				int hZ = height._z;

				if (lowerX == hX && lowerZ == hZ)
				{
					q11 = height;
				} else if (lowerX == hX && upperZ == hZ)
				{
					q12 = height;
				} else if (upperX == hX && lowerZ == hZ)
				{
					q21 = height;
				} else if (upperX == hX && upperZ == hZ)
				{
					q22 = height;
				}

				if (q11 != null && q12 != null && q21 != null && q22 != null)
				{
					break;
				}
			}

			if (q11 == null || q12 == null || q21 == null || q22 == null)
			{
				// System.out.println("No level data found for this coordinates ("
				// +
				// x + ", " + z + ") !! So, will be generated...");
				if (q11 == null)
				{
					q11 = _generator.generateRawHeightAt(lowerX, lowerZ);
					if (isPoint(q11, x, z))
					{
						return q11._data;
					}
				}
				if (q12 == null)
				{
					q12 = _generator.generateRawHeightAt(lowerX, upperZ);
					if (isPoint(q12, x, z))
					{
						return q12._data;
					}
				}
				if (q21 == null)
				{
					q21 = _generator.generateRawHeightAt(upperX, lowerZ);
					if (isPoint(q21, x, z))
					{
						return q21._data;
					}
				}
				if (q22 == null)
				{
					q22 = _generator.generateRawHeightAt(upperX, upperZ);
					if (isPoint(q22, x, z))
					{
						return q22._data;
					}
				}
			}
		}

		return biLerpDataPoints(x, z, q11, q12, q21, q22);
	}

	@Override
	public Biome getBiomeAt(int x, int y, int z)
	{
		float temp = getTemperatureAt(x, y, z);
		float humidity = getHumidityAt(x, y, z);

		if (DEBUG_WOLRD_PROVIDER && x % 8 == 0 && z % 8 == 0)
		{
			System.out.println();
			System.out.println("Temp = " + temp + ", Humidity = " + humidity);
			System.out.println();

			float humidity2 = getHumidityAt(x - 1, y, z);
			float temp2 = getTemperatureAt(x - 1, y, z);

			if (Math.abs(humidity - humidity2) > 6.0f)
			{
				System.out.println("Humidity is the problem! " + (humidity - humidity2));
			}
			if (Math.abs(temp - temp2) > 6.0f)
			{
				System.out.println("Temperature is the problem! " + (temp - temp2));
			}
		}

		return calculateBiome((int) temp, (int) humidity);
	}
	
	@Override
	public Biome calculateBiome(int temperature, int humidity)
	{
		if (temperature > 25.0f && humidity < 30.0f)
		{
			return Biome.DESERT;
		}
		if (temperature < 5.0f)
		{
			return Biome.SNOW;
		}
		if (humidity < 50.0f)
		{
			return Biome.FIELDS;
		}
		return Biome.FOREST;
	}

	@Override
	public Vec3f getSpawnPoint()
	{
		if (_spawnPoint == null)
		{
			generateSpawnPoint();
		}
		return new Vec3f(_spawnPoint);
	}

	private void generateSpawnPoint()
	{
		SmartRandom random = new SmartRandom(new Random());
		for (int i = 1; i <= 15; ++i)
		{
			int x = random.randomInt(-5 * i, 5 * i);
			int z = random.randomInt(-5 * i, 5 * i);
			int y = getHeightAt(x, z);

			_spawnPoint = new Vec3f(x + 0.5f, y + 1.5f, z + 0.5f);
			byte spawnPointBlock = 0;
			while (spawnPointBlock <= 0)
			{
				spawnPointBlock = _world.getChunkManager().getBlock(x, y, z, true, true, true);
				try
				{
					Thread.sleep(100);
				} catch (Exception e)
				{
				}
			}
			if (BlockManager.getInstance().getBlockType(spawnPointBlock).getName().equals("grass"))
			{
				byte above = _world.getChunkManager().getBlock(x, y + 1, z, true, true, true);
				if (above == 0)
				{
					above = _world.getChunkManager().getBlock(x, y + 2, z, true, true, true);
					if (above == 0)
					{
						break;
					}
				}
			}
		}
	}

	public static class DataPoint2D
	{

		private int _x, _z, _data;

		public DataPoint2D(int x, int z, int data)
		{
			_x = x;
			_z = z;
			_data = data;
		}

		public int getData()
		{
			return _data;
		}

		public int getX()
		{
			return _x;
		}

		public int getZ()
		{
			return _z;
		}

		public void setData(int data)
		{
			this._data = data;
		}
	}

	private class WorldProviderGenerator
	{

		@SuppressWarnings("unused")
		private class DataResults
		{

			public float min, max, avg;
			public float closest, closestDistanceSq;
			public int count;

			public DataResults()
			{
				min = Float.MAX_VALUE;
				max = Float.MIN_VALUE;
				count = 0;
				closestDistanceSq = Float.MAX_VALUE;
			}
		}

		private SmartRandom _random;

		public WorldProviderGenerator(World world)
		{
			_random = new SmartRandom(new Random(world.getWorldSeed()));
		}

		/**
		 * Generates a new data point, stores it in the heights array. Returns
		 * the newly generated terrain height
		 * 
		 * @param x
		 * @param z
		 * @return
		 */
		public DataPoint2D generateHeightAt(int x, int z)
		{
			/* Rasterize the coordinates */
			x = MathHelper.floorDivision(x, DefaultWorldProvider.SAMPLE_RATE_HEIGHTS) * DefaultWorldProvider.SAMPLE_RATE_HEIGHTS;
			z = MathHelper.floorDivision(z, DefaultWorldProvider.SAMPLE_RATE_HEIGHTS) * DefaultWorldProvider.SAMPLE_RATE_HEIGHTS;

			DataResults td = gatherDataAround(_heights, x, z, 20);

			int rawHeight = getRawHeightAt(x, z);

			if (td.count == 0)
			{
				DataPoint2D data = new DataPoint2D(x, z, rawHeight);
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Height (" + x + ", " + z + ") = " + data.getData());
				_heights.add(data);
				return data;
			} else
			{
				float diff = _random.exponentialRandom(5.0f, 3);

				float height = rawHeight * 0.6f + td.avg * 0.4f + diff;

				height = Math.max(10, height);
				DataPoint2D data = new DataPoint2D(x, z, MathHelper.round(height));
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Height (" + x + ", " + z + ") = " + data.getData());

				_heights.add(data);
				return data;
			}
		}

		/**
		 * Generates a new data point, stores it in the heights array. Returns
		 * the newly generated terrain height
		 * 
		 * @param x
		 * @param z
		 * @return
		 */
		public DataPoint2D generateRawHeightAt(int x, int z)
		{
			/* Rasterize the coordinates */
			x = MathHelper.floorDivision(x, DefaultWorldProvider.SAMPLE_RATE_RAW_HEIGHS) * DefaultWorldProvider.SAMPLE_RATE_RAW_HEIGHS;
			z = MathHelper.floorDivision(z, DefaultWorldProvider.SAMPLE_RATE_RAW_HEIGHS) * DefaultWorldProvider.SAMPLE_RATE_RAW_HEIGHS;

			DataResults td = gatherDataAround(_rawHeights, x, z, 60);

			if (td.count == 0)
			{
				DataPoint2D data = new DataPoint2D(x, z, MathHelper.floor(_random.randomFloat(40, 53)));
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Height (" + x + ", " + z + ") = " + data.getData());
				_rawHeights.add(data);
				return data;
			} else
			{
				float diff = _random.exponentialRandom(120.0f, 4);

				float avgHeight = _random.randomFloat(td.avg - diff, td.avg + diff);

				float height = avgHeight;

				height = MathHelper.clamp(height, 15, 200);
				DataPoint2D data = new DataPoint2D(x, z, MathHelper.round(height));
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Height (" + x + ", " + z + ") = " + data.getData());

				_rawHeights.add(data);
				return data;
			}
		}

		private DataResults gatherDataAround(List<DataPoint2D> list, int x, int z, float radius)
		{
			DataResults td = new DataResults();

			float radiusSq = radius * radius;
			float total = 0.0f;

			for (DataPoint2D data : list)
			{
				float pX = data.getX();
				float pZ = data.getZ();

				pX -= x;
				pZ -= z;

				float distanceSq = pX * pX + pZ * pZ;

				if (distanceSq < radiusSq)
				{
					total += data.getData();
					td.count++;

					td.min = Math.min(td.min, data.getData());
					td.max = Math.max(td.max, data.getData());

					if (td.closestDistanceSq < distanceSq)
					{
						td.closestDistanceSq = distanceSq;
						td.closest = data.getData();
					}
				}
			}

			if (td.count != 0)
			{
				td.avg = total / td.count;
			}

			return td;
		}

		public DataPoint2D generateHumidityAt(int x, int z)
		{
			/* Rasterize the coordinates */
			x = MathHelper.floor((float) x / DefaultWorldProvider.SAMPLE_RATE_HORIZONTAL) * DefaultWorldProvider.SAMPLE_RATE_HORIZONTAL;
			z = MathHelper.floor((float) z / DefaultWorldProvider.SAMPLE_RATE_HORIZONTAL) * DefaultWorldProvider.SAMPLE_RATE_HORIZONTAL;

			DataResults dr = gatherDataAround(_humidities, x, z, 40);

			if (dr.count == 0)
			{
				DataPoint2D data = new DataPoint2D(x, z, _random.randomInt(60, 80));
				_humidities.add(data);
				return data;
			} else
			{
				float diff = _random.exponentialRandom(100.0f, 4);
				diff = _random.randomBoolean() ? diff : -diff;
				DataPoint2D data = new DataPoint2D(x, z, (int) MathHelper.clamp(dr.avg + diff, 30.0f, 95.0f));
				_humidities.add(data);
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Humidity (" + x + ", " + z + ") = " + data.getData());
				return data;
			}
		}

		public DataPoint2D generateTemperatureAt(int x, int z)
		{
			/* Rasterize the coordinates */
			x = MathHelper.floorDivision(x, DefaultWorldProvider.SAMPLE_RATE_TEMPERATURE) * DefaultWorldProvider.SAMPLE_RATE_TEMPERATURE;
			z = MathHelper.floorDivision(z, DefaultWorldProvider.SAMPLE_RATE_TEMPERATURE) * DefaultWorldProvider.SAMPLE_RATE_TEMPERATURE;

			DataResults dr = gatherDataAround(_temperatures, x, z, 50);

			if (dr.count == 0)
			{
				DataPoint2D data = new DataPoint2D(x, z, _random.randomInt(25, 32));
				_temperatures.add(data);
				return data;
			} else
			{
				float diff = _random.exponentialRandom(16.0f, 2);
				diff = _random.randomBoolean() ? diff : -diff;
				DataPoint2D data = new DataPoint2D(x, z, (int) MathHelper.clamp(dr.avg + diff, 10.0f, 50.0f));
				_temperatures.add(data);
				if (DEBUG_WOLRD_PROVIDER)
					System.out.println("Temp (" + x + ", " + z + ") = " + data.getData());
				return data;
			}
		}
	}

	@Override
	public void save() throws Exception
	{
		File file = Game.getInstance().getRelativeFile(Game.FILE_BASE_USER_DATA, "${world}/world.dat");

		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

		dos.writeFloat(_world.getTime());

		IOUtilities.writeVec3f(dos, _spawnPoint);

		writeDataPointList(dos, _rawHeights);
		writeDataPointList(dos, _heights);
		writeDataPointList(dos, _humidities);
		writeDataPointList(dos, _temperatures);

		/* Tree Definitions */
		{
			dos.writeInt(_trees.size());
			for (int i = 0; i < _trees.size(); ++i)
			{
				TreeDefinition dp2d = _trees.get(i);
				dos.writeInt(dp2d.x);
				dos.writeInt(dp2d.y);
				dos.writeInt(dp2d.z);
				dos.writeByte(dp2d.type);
			}
		}

		dos.flush();
		dos.close();
	}

	@Override
	public void load() throws Exception
	{
		File file = Game.getInstance().getRelativeFile(Game.FILE_BASE_USER_DATA, "${world}/world.dat");

		if (!file.exists())
		{
			System.err.println("No level data found! " + file.getPath());
			return;
		}

		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		_world.setTime(dis.readFloat());

		_spawnPoint = new Vec3f();
		IOUtilities.readVec3f(dis, _spawnPoint);

		readDataPointList(dis, _rawHeights);
		readDataPointList(dis, _heights);
		readDataPointList(dis, _humidities);
		readDataPointList(dis, _temperatures);

		/* Tree Definitions */
		{
			int size = dis.readInt();
			for (int i = 0; i < size; ++i)
			{
				TreeDefinition dp2d = new TreeDefinition(0, 0, 0, 0);
				dp2d.x = dis.readInt();
				dp2d.y = dis.readInt();
				dp2d.z = dis.readInt();
				dp2d.type = dis.readByte();
			}
		}

		dis.close();

	}

	private void writeDataPointList(DataOutputStream dos, List<DataPoint2D> list) throws IOException
	{
		dos.writeInt(list.size());
		for (int i = 0; i < list.size(); ++i)
		{
			DataPoint2D dp2d = list.get(i);
			dos.writeInt(dp2d.getX());
			dos.writeInt(dp2d.getZ());
			dos.writeInt(dp2d.getData());
		}
	}

	private void readDataPointList(DataInputStream dis, List<DataPoint2D> list) throws IOException
	{
		int size = dis.readInt();
		for (int i = 0; i < size; ++i)
		{
			DataPoint2D dp2d = new DataPoint2D(0, 0, 0);
			dp2d._x = dis.readInt();
			dp2d._z = dis.readInt();
			dp2d._data = dis.readInt();
			list.add(dp2d);
		}
	}

}
