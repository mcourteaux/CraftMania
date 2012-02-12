package org.craftmania.utilities;

import java.util.BitSet;

public class IntegerPool
{
	private int _size;
	private int _integersInUsage;
	private BitSet _bitset;

	public IntegerPool(int size)
	{
		_size = size;
		_bitset = new BitSet(size);
		_integersInUsage = 0;
	}

	public int newInteger()
	{
		if (_integersInUsage < _size)
		{
			for (int i = 0; i < _size; ++i)
			{
				boolean b = _bitset.get(i);
				if (!b)
				{
					_bitset.set(i);
					_integersInUsage++;
					return i;
				}
			}
		}
		return -1;
	}

	public void releaseInteger(int integer)
	{
		boolean bit = _bitset.get(integer);
		if (bit)
		{
			_bitset.clear(integer);
			_integersInUsage--;
		}
	}
	
	public int available()
	{
		return _size - _integersInUsage;
	}
	
	public int size()
	{
		return _size;
	}
	
	public int integersInUsage()
	{
		return _integersInUsage;
	}
}
