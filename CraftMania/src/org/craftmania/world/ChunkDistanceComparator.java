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

import java.util.Comparator;

import org.craftmania.math.Vec2f;

public class ChunkDistanceComparator implements Comparator<Chunk>
{

	private Vec2f _auxiliaryVector = new Vec2f();
	private Vec2f _center = new Vec2f();
	
	public void setCenter(float x, float z)
	{
		_center.set(x, z);
	}
	
	@Override
	public int compare(Chunk o1, Chunk o2)
	{
		_auxiliaryVector.set(o1.getAbsoluteX(), o1.getAbsoluteZ());
		_auxiliaryVector.sub(_center);
		
		float len1 = _auxiliaryVector.lengthSquared();
		
		_auxiliaryVector.set(o2.getAbsoluteX(), o2.getAbsoluteZ());
		_auxiliaryVector.sub(_center);
		
		float len2 = _auxiliaryVector.lengthSquared();
		
		if (len1 < len2)
		{
			return -1;
		}
		if (len2 > len1)
		{
			return 1;
		}
		return 0;
			
	}
	
	
}
