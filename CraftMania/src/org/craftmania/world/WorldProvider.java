package org.craftmania.world;

import org.craftmania.math.Vec3f;


public abstract class WorldProvider
{
	public abstract int getHeightAt(int x, int z);
	public abstract float getTemperatureAt(int x, int y, int z);
	public abstract float getHumidityAt(int x, int y, int z);
	public abstract Biome getBiomeAt(int x, int y, int z);
	public abstract Vec3f getSpawnPoint();
}
