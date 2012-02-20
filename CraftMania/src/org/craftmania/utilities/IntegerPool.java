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
