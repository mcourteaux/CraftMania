package org.craftmania.world;

import java.util.List;

import org.craftmania.math.Vec3f;


public abstract class WorldProvider
{
	public abstract int getHeightAt(int x, int z);
	public abstract float getTemperatureAt(int x, int y, int z);
	public abstract float getHumidityAt(int x, int y, int z);
	public abstract Biome getBiomeAt(int x, int y, int z);
	public abstract Vec3f getInitialSpawnPoint();
	public abstract Vec3f getSpawnPoint();
	public abstract List<TreeDefinition> getTrees();
	
	public abstract void save() throws Exception;
	public abstract void load() throws Exception;
	
	public static class TreeDefinition
	{
		public TreeDefinition(int x, int y, int z, int type)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.type = type;
		}
		
		public int x, y, z;
		public int type;
	}
	
}
