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

	public BlockDistanceComparator()
	{
		_blockVector = new Vec3f();
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
		float d1, d2;
		synchronized (_blockVector)
		{
			d1 = relativeToOrigin(o1.getAABB().getPosition()).lengthSquared();
			d2 = relativeToOrigin(o2.getAABB().getPosition()).lengthSquared();
		}

		if (d1 < d2)
			return 1;
		if (d1 > d2)
			return -1;
		return 0;
	}
}
