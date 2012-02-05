package org.craftmania.world;

import org.craftmania.math.Vec3i;

/**
 * Stores the data for the blocks.
 * 
 * Each block takes 24 bits. Each instance of this class will take 768 KB. The
 * design for a default block is this:
 * 
 * <pre>
 *   |........|0|0|.....|........|
 *    type    sp r faces metadata
 * </pre>
 * 
 * Where, for default blocks
 * <ul>
 * <li>type(8) for the type of block</li>
 * <li>sp(1) to specify if we have a special instance for that block</li>
 * <li>r(1) reserved</li>
 * <li>faces(6) holds the visibility value for each face</li>
 * <li>metadata(8) 1 byte to specify metadata for the block</li>
 * </ul>
 * 
 * If the special bit is set, then this design is used:
 * 
 * <pre>
 *    |........|1|...............|
 *     type    sp  position
 * </pre>
 * 
 * Where, for the special blocks
 * 
 * <ul>
 * <li>type(8) type of the block</li>
 * <li>sp(1) specifies if we have a special block, in this case true</li>
 * <li>position(15) specifies the index in the array of special blocks</li>
 * </ul>
 * 
 * @author martijncourteaux
 * 
 */
public class ChunkData
{

	private static final int BLOCK_DATA_SIZE = 3;
	private static final byte SPECIAL_BIT = (byte) (1 << 7); // 128

	private byte[] _data;

	public ChunkData()
	{
		_data = new byte[BLOCK_DATA_SIZE * 16 * 16 * 128];
	}

	public int getBlockData(int index)
	{
		return (_data[BLOCK_DATA_SIZE * index] << 16) + (_data[BLOCK_DATA_SIZE * index + 1] << 8) + (_data[BLOCK_DATA_SIZE * index + 2]);
	}

	public byte getBlockType(int index)
	{
		return _data[BLOCK_DATA_SIZE * index];
	}

	public boolean isSpecial(int index)
	{
		return 1 == (_data[BLOCK_DATA_SIZE * index + 1] >> 7);
	}

	public byte getFaceMask(int index)
	{
		return (byte) (0x3F & _data[BLOCK_DATA_SIZE * index + 1]);
	}

	public byte getMetaData(int index)
	{
		return _data[BLOCK_DATA_SIZE * index + 2];
	}

	public void setDefaultBlock(int index, byte type, byte faceMask, byte metaData)
	{
		index *= BLOCK_DATA_SIZE;
		_data[index++] = type;
		_data[index++] = faceMask;
		_data[index++] = metaData;
	}

	public void setSpecialBlock(int index, byte type, int position)
	{
		index *= BLOCK_DATA_SIZE;
		_data[index++] = type;
		_data[index++] = (byte) (SPECIAL_BIT + (position >> 8));
		_data[index++] = (byte) (position & 0xFF);
	}

	public void setBlockType(int index, byte type)
	{
		_data[BLOCK_DATA_SIZE * index] = type;
	}

	public void setSpecial(int index, boolean special)
	{
		if (special)
		{
			_data[BLOCK_DATA_SIZE * index + 1] |= SPECIAL_BIT;
		} else
		{
			_data[BLOCK_DATA_SIZE * index + 1] &= (~SPECIAL_BIT);
		}
	}

	public void setFaceMask(int index, byte faceMask)
	{
		index *= BLOCK_DATA_SIZE;
		++index;
		_data[index] = (byte) ((_data[index] & SPECIAL_BIT) | faceMask);
	}

	public void setMetaData(int index, byte metadata)
	{
		_data[BLOCK_DATA_SIZE * index + 2] = metadata;
	}

	public void setPosition(int index, int position)
	{
		index *= BLOCK_DATA_SIZE;
		++index;
		_data[index++] = (byte) (SPECIAL_BIT + (position >> 8));
		_data[index++] = (byte) (position & 0xFF);
	}

	public void clear(int index)
	{
		index *= BLOCK_DATA_SIZE;
		_data[index++] = 0;
		_data[index++] = 0;
		_data[index++] = 0;
	}

	/* Helper methods */

	public static int positionToIndex(int x, int y, int z)
	{
		return x * 128 * 16 + y * 16 + z;
	}

	public static void indexToPosition(int index, Vec3i output)
	{
		output.setX(index / (16 * 128));
		output.setY((index / 16) % 128);
		output.setZ(index % 16);
	}
}
