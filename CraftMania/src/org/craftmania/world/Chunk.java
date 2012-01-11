package org.craftmania.world;

import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.datastructures.Fast3DArray;

public class Chunk<T extends AABBObject> implements AABBObject
{
	public static final int CHUNK_SIZE_X = 8;
	public static final int CHUNK_SIZE_Z = 8;

	private int _x, _z;
	private Fast3DArray<T> _content;
	private AABB _aabb;
	private int _objectCount;

	public Chunk(int x, int z)
	{
		_x = x;
		_z = z;
		_content = new Fast3DArray<T>(CHUNK_SIZE_X, 1, CHUNK_SIZE_Z);
		_aabb = null;
	}

	public int getX()
	{
		return _x;
	}

	public int getZ()
	{
		return _z;
	}

	public T get(int x, int z)
	{
		return _content.get(x, 0, z);
	}

	public void set(int x, int z, T obj)
	{
		T old = _content.set(x, 0, z, obj);
		if (obj == null)
		{
			createAABB();
		} else
		{
			if (_aabb == null)
			{
				_aabb = new AABB(obj.getAABB());
			} else
			{
				_aabb.include(obj.getAABB());
			}
		}

		if (obj != null && old == null)
		{
			_objectCount++;
		}
		if (obj == null && old != null)
		{
			_objectCount--;
		}
	}

	private void createAABB()
	{
		_aabb = null;
		for (int i = 0; i < _content.size(); ++i)
		{
			T obj = _content.getRawObject(i);
			if (obj == null) continue;
			if (_aabb == null)
			{
				_aabb = new AABB(obj.getAABB());
			} else
			{
				_aabb.include(obj.getAABB());
			}
		}
	}

	@Override
	public AABB getAABB()
	{
		return _aabb;
	}

	public int objectCount()
	{
		return _objectCount;
	}
}
