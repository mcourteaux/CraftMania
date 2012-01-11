package org.craftmania.datastructures;

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
