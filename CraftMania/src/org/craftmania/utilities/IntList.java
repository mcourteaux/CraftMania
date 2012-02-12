package org.craftmania.utilities;

import java.util.ArrayList;
import java.util.List;

public class IntList
{

	private List<Integer> _modificationBuffer;
	private int _adds;

	private int[] _data;

	private int _size;

	public IntList()
	{
		this(10);
	}

	public IntList(int initialCapacity)
	{
		_size = 0;
		_data = new int[initialCapacity];
		_modificationBuffer = new ArrayList<Integer>();
	}

	public int size()
	{
		return _size;
	}

	public int get(int index)
	{
		return _data[index];
	}

	public void set(int index, int value)
	{
		_data[index] = value;
	}

	public void add(int value)
	{
		ensureCapacity(_size + 1);
		_data[_size++] = value;
	}

	public int removeIndex(int index)
	{
		int ret = _data[index];

		int numMoved = _size - index - 1;
		if (numMoved > 0)
			System.arraycopy(_data, index + 1, _data, index, numMoved);
		_data[--_size] = 0;

		return ret;
	}

	public int removeValue(int value)
	{
		for (int index = 0; index < _size; ++index)
		{
			if (get(index) == value)
			{
				removeIndex(index);
				return index;
			}
		}
		return -1;
	}

	private void ensureCapacity(int size)
	{
		if (size > _data.length)
		{
			int newSize = _data.length * 3 / 2 + 1;
			int newData[] = new int[newSize];
			System.arraycopy(_data, 0, newData, 0, _size);
			_data = newData;
		}
	}

	public void bufferAdd(int value)
	{
		synchronized (_modificationBuffer)
		{
			_modificationBuffer.add(value + 1);
			_adds++;
		}
	}

	public void bufferRemove(int value)
	{
		synchronized (_modificationBuffer)
		{
			_modificationBuffer.add(-(value + 1));
		}
	}

	public void executeModificationBuffer()
	{
		synchronized (_modificationBuffer)
		{
			if (!_modificationBuffer.isEmpty())
			{
				ensureCapacity(_size + _adds + 5);
				int val = 0;
				for (Integer i : _modificationBuffer)
				{
					val = i.intValue();
					if (val > 0)
					{
						add(val - 1);
					} else if (val < 0)
					{
						removeValue(-val - 1);
					}
				}
				_modificationBuffer.clear();
				_adds = 0;
			}
		}

	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(20 + 5 * _size);
		sb.append("IntList (");
		sb.append(size());
		sb.append("/");
		sb.append(_data.length);
		sb.append("): ");

		for (int i = 0; i < size(); ++i)
		{
			if (i > 0)
				sb.append(", ");
			sb.append(_data[i]);
		}

		return sb.toString();
	}

	public void clear()
	{
		_size = 0;
	}

}
