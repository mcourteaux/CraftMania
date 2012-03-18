package org.craftmania.world;

import org.craftmania.world.Chunk.LightType;

public class LightBuffer
{

	private int _x, _y, _z;
	private byte[] _buffer;
	private boolean _buffered;
	private Chunk _owner;

	public LightBuffer(Chunk owner)
	{
		_buffer = new byte[27];
		_owner = owner;
	}
	
	public void setDirty()
	{
		_buffered = false;
	}

	public void fill(int x, int y, int z)
	{
		manualFill(x, y, z);
//		if (_buffered)
//		{
//			if (x == _x + 1)
//			{
//				moveRight();
//			} else if (x == _x - 1)
//			{
//				moveLeft();
//			}
//			if (y == _y + 1)
//			{
//				moveUp();
//			} else if (y == _y - 1)
//			{
//				moveDown();
//			}
//			if (z == _z + 1)
//			{
//				moveForward();
//			} else if (z == _z - 1)
//			{
//				moveBackward();
//			} else
//			{
//				manualFill(x, y, z);
//			}
//		} else
//		{
//			/* Fill manually */
//			manualFill(x, y, z);
//		}
//		_buffered = true;
	}

	private void manualFill(int x, int y, int z)
	{
		byte rawlight, blockLight, sunlight;
		for (int xx = -1; xx <= 1; ++xx)
		{
			for (int yy = -1; yy <= 1; ++yy)
			{
				for (int zz = -1; zz <= 1; ++zz)
				{
					rawlight = _owner.getLightAbsolute(xx + x, yy + y, zz + z, LightType.RAW);
					blockLight = (byte) (rawlight & 0xF);
					sunlight = (byte) ((rawlight & 0xF0) >>> 4);

					sunlight *= _owner.getWorld().getSunlight() * 2.0f;

					set(xx + 1, yy + 1, zz + 1, (byte) Math.max(blockLight * 2, sunlight));
				}
			}
		}

		_x = x;
		_y = y;
		_z = z;
	}

	private void moveLeft()
	{
		System.out.println("ml");
		_x--;
		for (int i = 0; i < 3; ++i)
		{
			set(2, 0, i, get(1, 0, i));
			set(2, 1, i, get(1, 1, i));
			set(2, 2, i, get(1, 2, i));
			set(1, 0, i, get(0, 0, i));
			set(1, 1, i, get(0, 1, i));
			set(1, 2, i, get(0, 2, i));

			set(0, 0, i, calculateLightAt(_x - 1, _y - 1, _z + i - 1));
			set(0, 1, i, calculateLightAt(_x - 1, _y + 0, _z + i - 1));
			set(0, 2, i, calculateLightAt(_x - 1, _y + 1, _z + i - 1));
		}
	}

	private void moveRight()
	{
		System.out.println("mr");
		_x++;
		for (int i = 0; i < 3; ++i)
		{
			set(0, 0, i, get(1, 0, i));
			set(0, 1, i, get(1, 1, i));
			set(0, 2, i, get(1, 2, i));
			set(1, 0, i, get(2, 0, i));
			set(1, 1, i, get(2, 1, i));
			set(1, 2, i, get(2, 2, i));

			set(2, 0, i, calculateLightAt(_x + 1, _y - 1, _z + i - 1));
			set(2, 1, i, calculateLightAt(_x + 1, _y + 0, _z + i - 1));
			set(2, 2, i, calculateLightAt(_x + 1, _y + 1, _z + i - 1));
		}
	}

	private void moveUp()
	{
		System.out.println("mu");
		_y++;
		for (int i = 0; i < 3; ++i)
		{
			set(0, 0, i, get(0, 1, i));
			set(1, 0, i, get(1, 1, i));
			set(2, 0, i, get(2, 1, i));
			set(0, 1, i, get(0, 2, i));
			set(1, 1, i, get(1, 2, i));
			set(2, 1, i, get(2, 2, i));

			set(0, 2, i, calculateLightAt(_x - 1, _y + 1, _z + i - 1));
			set(1, 2, i, calculateLightAt(_x + 0, _y + 1, _z + i - 1));
			set(2, 2, i, calculateLightAt(_x + 1, _y + 1, _z + i - 1));
		}
	}

	private void moveDown()
	{
		System.out.println("md");
		_y--;
		for (int i = 0; i < 3; ++i)
		{
			set(0, 2, i, get(0, 1, i));
			set(1, 2, i, get(1, 1, i));
			set(2, 2, i, get(2, 1, i));
			set(0, 1, i, get(0, 0, i));
			set(1, 1, i, get(1, 0, i));
			set(2, 1, i, get(2, 0, i));

			set(0, 0, i, calculateLightAt(_x - 1, _y - 1, _z + i - 1));
			set(1, 0, i, calculateLightAt(_x + 0, _y - 1, _z + i - 1));
			set(2, 0, i, calculateLightAt(_x + 1, _y - 1, _z + i - 1));
		}
	}

	private void moveBackward()
	{
		System.out.println("mb");
		_z--;
		for (int i = 0; i < 3; ++i)
		{
			set(0, i, 2, get(0, i, 1));
			set(1, i, 2, get(1, i, 1));
			set(2, i, 2, get(2, i, 1));
			set(0, i, 1, get(0, i, 0));
			set(1, i, 1, get(1, i, 0));
			set(2, i, 1, get(2, i, 0));

			set(0, i, 0, calculateLightAt(_x - 1, _y + i - 1, _z - 1));
			set(1, i, 0, calculateLightAt(_x + 0, _y + i - 1, _z - 1));
			set(2, i, 0, calculateLightAt(_x + 1, _y + i - 1, _z - 1));
		}
	}

	private void moveForward()
	{
		_z++;
		for (int i = 0; i < 3; ++i)
		{
			set(0, i, 0, get(0, i, 1));
			set(1, i, 0, get(1, i, 1));
			set(2, i, 0, get(2, i, 1));
			set(0, i, 1, get(0, i, 2));
			set(1, i, 1, get(1, i, 2));
			set(2, i, 1, get(2, i, 2));
		
			set(0, i, 2, calculateLightAt(_x - 1, _y + i - 1, _z + 1));
			set(1, i, 2, calculateLightAt(_x + 0, _y + i - 1, _z + 1));
			set(2, i, 2, calculateLightAt(_x + 1, _y + i - 1, _z + 1));
		}
	}

	private void set(int x, int y, int z, byte b)
	{
		_buffer[x * 9 + y * 3 + z] = b;
	}

	public byte get(int x, int y, int z)
	{
		return _buffer[x * 9 + y * 3 + z];
	}

	private byte calculateLightAt(int x, int y, int z)
	{
		byte rawlight = _owner.getLightAbsolute(x, y, z, LightType.RAW);
		byte blockLight = (byte) (rawlight & 0xF);
		byte sunlight = (byte) ((rawlight & 0xF0) >>> 4);

		sunlight *= _owner.getWorld().getSunlight() * 2.0f;

		rawlight = (byte) Math.max(blockLight * 2, sunlight);

		return rawlight;
	}
}
