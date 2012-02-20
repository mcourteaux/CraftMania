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
package org.craftmania.datastructures;

import org.craftmania.math.Vec3i;

/**
 * 
 * @author martijncourteaux
 */
public class Fast3DArray<T>
{

	private Object _array[];
	private final int _lX, _lY, _lZ;
	private final int _size;

	/**
	 * Init. a new 3D array with the given dimensions.
	 */
	public Fast3DArray(int x, int y, int z)
	{
		_lX = x;
		_lY = y;
		_lZ = z;

		_size = _lX * _lY * _lZ;
		_array = new Object[_size];
	}
	
	public void rawIndexToVec3i(int index, Vec3i vec)
	{
		vec.setX(index / (_lX * _lY));
		vec.setY((index / _lX) % _lY);
		vec.setZ(index % _lZ);
	}

	/**
	 * Returns the byte value at the given position.
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y, int z)
	{

		int pos = (x * _lX * _lY) + (y * _lX) + z;

		if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
		{
			return null;
		}

		return (T) _array[pos];
	}

	/**
	 * Sets the value for the given position.
	 * 
	 * @return The previous value
	 */
	@SuppressWarnings("unchecked")
	public T set(int x, int y, int z, T b)
	{
		if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
		{
			return null;
		}
		
		int pos = (x * _lX * _lY) + (y * _lX) + z;
		
		T obj = (T) _array[pos];
		_array[pos] = b;
		return obj;
	}

	/**
	 * Returns the raw object at the given index.
	 */
	@SuppressWarnings("unchecked")
	public T getRawObject(int i)
	{
		return (T) _array[i];
	}

	/**
	 * Sets the raw object for the given index.
	 */
	@SuppressWarnings("unchecked")
	public T setRawObject(int i, T b)
	{
		T old = (T) _array[i];
		_array[i] = b;
		return old;
	}

	/**
	 * Returns the size of this array.
	 */
	public int size()
	{
		return _size;
	}

	public void clear()
	{
		_array = null;
	}
}
