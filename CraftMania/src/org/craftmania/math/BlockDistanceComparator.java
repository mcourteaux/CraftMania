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
package org.craftmania.math;

import java.util.Comparator;

import org.craftmania.blocks.Block;

/**
 * 
 * @author martijncourteaux
 */
public class BlockDistanceComparator implements Comparator<Block>
{

	private Vec3f _origin;
	private Vec3f _blockVector;
	private int _id;

	public BlockDistanceComparator()
	{
		_blockVector = new Vec3f();
	}
	
	public void newID()
	{
		_id++;
		if (_id == 10000)
		{
			_id = 0;
		}
	}

	public void setOrigin(Vec3f origin)
	{
		_origin = origin;
	}

	private Vec3f relativeToOrigin(Vec3f point)
	{
		return _blockVector.set(point).sub(_origin);
	}

	@Override
	public int compare(Block o1, Block o2)
	{
		if (o1._distanceID != _id)
		{
			o1._distanceID = _id;
			o1._distance = relativeToOrigin(o1.getAABB().getPosition()).lengthSquared();
		}
		if (o2._distanceID != _id)
		{
			o2._distanceID = _id;
			o2._distance = relativeToOrigin(o2.getAABB().getPosition()).lengthSquared();
		}

		if (o1._distance < o2._distance)
			return 1;
		if (o1._distance > o2._distance)
			return -1;
		return 0;
	}
}
