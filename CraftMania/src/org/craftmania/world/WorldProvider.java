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
