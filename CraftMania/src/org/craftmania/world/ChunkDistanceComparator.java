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
