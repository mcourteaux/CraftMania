package org.craftmania.world;

import org.craftmania.blocks.Block;
import org.craftmania.math.Vec3i;
import org.craftmania.utilities.IntegerPool;
import org.craftmania.world.Chunk.LightType;

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
	private static final byte SPECIAL_BIT = (byte) 128; // 128

	private SpecialBlockPool _blockPool;
	private byte[] _data;
	private byte[] _light;

	public ChunkData()
	{
		_data = new byte[BLOCK_DATA_SIZE * Chunk.BLOCK_COUNT];
		_light = new byte[Chunk.BLOCK_COUNT];
		_blockPool = new SpecialBlockPool();
	}

	public byte getLight(int index, LightType type)
	{
		if (type == LightType.SUN)
		{
			return getSunlight(index);
		}
		return getBlockLight(index);
	}

	public void setLight(int index, byte light, LightType type)
	{
		if (type == LightType.SUN)
		{
			setSunlight(index, light);
		} else
		{
			setBlockLight(index, light);
		}
	}

	public void clearLight(int index)
	{
		_light[index] = (byte) 0;
	}

	public byte getBlockLight(int index)
	{
		return (byte) (_light[index] & 0xF);
	}

	public void setBlockLight(int index, byte light)
	{
		byte l = _light[index];
		_light[index] = (byte) ((l & 0xF0) + (light & 0xF));
	}

	public void setSunlight(int index, byte sunlight)
	{
		byte l = _light[index];
		_light[index] = (byte) (((sunlight & 0xF) << 4) + (l & 0xF));
	}

	public byte getSunlight(int index)
	{
		return (byte) ((_light[index] & 0xF0) >>> 4);
	}

	public byte getTotalLight(int index)
	{
		return (byte) Math.max(getSunlight(index), getBlockLight(index));
	}

	public int getBlockData(int index)
	{
		index *= BLOCK_DATA_SIZE;
		byte b1 = _data[index];
		byte b2 = _data[index + 1];
		byte b3 = _data[index + 2];

		return ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
	}

	public byte getBlockType(int index)
	{
		return _data[BLOCK_DATA_SIZE * index];
	}

	public boolean isSpecial(int index)
	{
		return 1 == ((_data[BLOCK_DATA_SIZE * index + 1] & 0xFF) >> 7);
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
		if (isSpecial(index))
		{
			System.out.println("Overwriting special block!");
		}
		index *= BLOCK_DATA_SIZE;
		_data[index + 0] = type;
		_data[index + 1] = faceMask;
		_data[index + 2] = metaData;
	}

	public void setSpecialBlock(int index, byte type, int position)
	{
		index *= BLOCK_DATA_SIZE;
		_data[index + 0] = type;
		_data[index + 1] = (byte) (SPECIAL_BIT | ((position >>> 8) & 0x7F));
		_data[index + 2] = (byte) (position & 0xFF);
		System.out.println(Integer.toHexString(getBlockData(index / BLOCK_DATA_SIZE)));
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
		if (isSpecial(index))
		{
			System.out.println("Setting facemask for special block!!!");
		}
		index *= BLOCK_DATA_SIZE;
		++index;
		_data[index] = (byte) (((_data[index] & 0xFF) & SPECIAL_BIT) | faceMask);
	}

	public void setMetaData(int index, byte metadata)
	{
		if (isSpecial(index))
		{
			System.out.println("Setting metadata for special block!!!");
		}
		_data[BLOCK_DATA_SIZE * index + 2] = metadata;
	}

	public void setPosition(int index, int position)
	{
		if (isSpecial(index))
		{
			System.out.println("Updating position of special block");
		}
		index *= BLOCK_DATA_SIZE;
		++index;
		_data[index++] = (byte) (SPECIAL_BIT + (position >>> 8));
		_data[index++] = (byte) (position & 0xFF);
	}

	public int getPosition(int index)
	{
		index *= BLOCK_DATA_SIZE;
		++index;
		return ((_data[index] & (~SPECIAL_BIT)) << 8) | (_data[index + 1]);
	}

	public void clearBlock(int index)
	{
		boolean special = isSpecial(index);
		if (special)
		{
			System.out.println("Removing special block!");
			int position = getPosition(index);
			_blockPool.releaseBlock(position);
		}
		index *= BLOCK_DATA_SIZE;
		_data[index++] = 0;
		_data[index++] = 0;
		_data[index++] = 0;
	}

	public Block getSpecialBlock(int index)
	{
		int position = getPosition(index);
		System.out.println(index + " -> " + position);
		return _blockPool.getBlock(position);
	}

	public void setSpecialBlock(int index, Block block)
	{
		int position = _blockPool.allocateBlock(block);
		block.setSpecialBlockPoolIndex(position);
		setSpecialBlock(index, block.getBlockType().getID(), position);
		System.out.println(index + " " + block.getBlockType().getID() + "->" + getBlockType(index) + " -> SetPosition: " + position + " GetPosition: " + getPosition(index) + " ("
				+ Integer.toHexString(getBlockData(index)) + ")");
	}

	public class SpecialBlockPool
	{
		public static final int POOL_SIZE = 4024;

		private IntegerPool _integerPool;
		private Block[] _blocks;

		public SpecialBlockPool()
		{
			_integerPool = new IntegerPool(POOL_SIZE);
			_blocks = new Block[POOL_SIZE];
		}

		public int allocateBlock(Block block)
		{
			int index = _integerPool.newInteger();
			_blocks[index] = block;
			return index;
		}

		public void releaseBlock(int index)
		{
			_integerPool.releaseInteger(index);
			_blocks[index] = null;
		}

		public Block getBlock(int index)
		{
			return _blocks[index];
		}
	}

	/* Helper methods */

	public static int positionToIndex(int x, int y, int z)
	{
		return x * Chunk.CHUNK_SIZE_VERTICAL * Chunk.CHUNK_SIZE_HORIZONTAL + y * Chunk.CHUNK_SIZE_HORIZONTAL + z;
	}

	public static void indexToPosition(int index, Vec3i output)
	{
		output.setX(index / (Chunk.CHUNK_SIZE_HORIZONTAL * Chunk.CHUNK_SIZE_VERTICAL));
		output.setY((index / Chunk.CHUNK_SIZE_HORIZONTAL) % Chunk.CHUNK_SIZE_VERTICAL);
		output.setZ(index % Chunk.CHUNK_SIZE_HORIZONTAL);
	}

	public static boolean dataIsSpecial(int data)
	{
		return (data & 0x8000) == 0x8000;
	}

	public static byte dataGetFaceMask(int data)
	{
		return (byte) ((data >>> 8) & 0x3F);
	}

	public static byte dataGetMetadata(int data)
	{
		return (byte) (data & 0xFF);
	}

}
