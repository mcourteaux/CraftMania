package org.craftmania.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;

public class ChunkManager
{
	@SuppressWarnings("unused")
	private World _world;
	private BlockManager _blockManager;
	private Map<Integer, AbstractChunk<Chunk>> _superChunks;
	private List<BlockMovement> _blocksToMove;
	private ChunkLoader _blockChunkLoader;
	private ChunkThreading _blockChunkThreading;

	public ChunkManager(World world)
	{
		_world = world;
		_superChunks = new HashMap<Integer, AbstractChunk<Chunk>>();
		_blocksToMove = new ArrayList<ChunkManager.BlockMovement>();
		_blockChunkLoader = new ChunkLoader();
		_blockChunkThreading = new ChunkThreading(this);
		_blockManager = BlockManager.getInstance();
	}

	public Collection<AbstractChunk<Chunk>> getAllSuperChunks()
	{
		return _superChunks.values();
	}

	public AbstractChunk<Chunk> getSuperChunk(int x, int z)
	{
		/* Map to positive integers */
		int posX = MathHelper.mapToPositive(x);
		int posZ = MathHelper.mapToPositive(z);

		/* Apply Cantor's method */
		Integer cantorize = Integer.valueOf(MathHelper.cantorize(posX, posZ));

		AbstractChunk<Chunk> superChunk = _superChunks.get(cantorize);
		if (superChunk == null)
		{
			superChunk = new AbstractChunk<Chunk>(x, z);
			_superChunks.put(cantorize, superChunk);
		}
		return superChunk;
	}

	public Chunk getChunk(int x, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		int superX = MathHelper.floorDivision(x, AbstractChunk.CHUNK_SIZE_X);
		int superZ = MathHelper.floorDivision(z, AbstractChunk.CHUNK_SIZE_Z);

		int xInChunk = x - superX * AbstractChunk.CHUNK_SIZE_X;
		int zInChunk = z - superZ * AbstractChunk.CHUNK_SIZE_X;

		AbstractChunk<Chunk> superChunk = getSuperChunk(superX, superZ);
		// synchronized (superChunk)
		{

			Chunk blockChunk = superChunk.get(xInChunk, zInChunk);
			if (blockChunk == null && createIfNecessary)
			{
				blockChunk = new Chunk(x, z);
				assignNeighbors(blockChunk);
				superChunk.set(xInChunk, zInChunk, blockChunk);
			}
			if (blockChunk != null && !blockChunk.isLoaded() && loadIfNecessary && !blockChunk.isLoading() && !blockChunk.isDestroying())
			{
				try
				{
					_blockChunkLoader.loadChunk(blockChunk);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (blockChunk != null && generateIfNecessary && !blockChunk.isLoading() && !blockChunk.isGenerated() && !blockChunk.isDestroying())
			{
				blockChunk.generate();
			}
			return blockChunk;
		}
	}

	public void assignNeighbors(Chunk blockChunk)
	{
		int x = blockChunk.getX();
		int z = blockChunk.getZ();
		blockChunk.setNeighborBlockChunk(Side.BACK, getChunk(x, z - 1, false, false, false));
		blockChunk.setNeighborBlockChunk(Side.FRONT, getChunk(x, z + 1, false, false, false));
		blockChunk.setNeighborBlockChunk(Side.LEFT, getChunk(x - 1, z, false, false, false));
		blockChunk.setNeighborBlockChunk(Side.RIGHT, getChunk(x + 1, z, false, false, false));
	}

	public byte getBlock(int x, int y, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		Chunk blockChunk = getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (blockChunk == null)
			return -1;
		return blockChunk.getBlockTypeAbsolute(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public void setDefaultBlock(int x, int y, int z, BlockType type, byte metadata, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		Chunk blockChunk = getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (blockChunk == null)
		{
			System.out.println("BlockChunk not found for " + x + " " + z);
			return;
		}
		blockChunk.setDefaultBlockAbsolute(x, y, z, type, metadata, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public Block getSpecialBlock(int x, int y, int z)
	{
		Chunk c = getChunkContaining(x, y, z, false, false, false);
		return c.getChunkData().getSpecialBlock(ChunkData.positionToIndex(x - c.getAbsoluteX(), y, z - c.getAbsoluteZ()));
	}
	

	public void removeBlock(int x, int y, int z)
	{
		Chunk blockChunk = getChunkContaining(x, y, z, false, false, false);
		if (blockChunk != null)
		{
			blockChunk.removeBlockAbsolute(x, y, z);
		}
	}

	public List<Chunk> getApproximateChunks(Vec3f position, float viewingDistance, List<Chunk> chunks)
	{
		viewingDistance /= Chunk.CHUNK_SIZE_HORIZONTAL;
		viewingDistance += 1.1f;

		int distance = MathHelper.ceil(viewingDistance);
		int distanceSq = distance * distance;

		int centerX = MathHelper.floor(position.x() / Chunk.CHUNK_SIZE_HORIZONTAL);
		int centerZ = MathHelper.floor(position.z() / Chunk.CHUNK_SIZE_HORIZONTAL);

		chunks.clear();


		
		for (int x = -distance; x <= distance; ++x)
		{
			for (int z = -distance; z <= distance; ++z)
			{
				int distSq = x * x + z * z;
				if (distSq <= distanceSq)
				{
					Chunk chunk = getChunk(centerX + x, centerZ + z, false, false, false);
					if (chunk != null && !chunk.isDestroying() && !chunk.isLoading() && chunk.isLoaded())
					{
						chunks.add(chunk);
					}
				}
			}
		}

		return chunks;
	}

	public Chunk getChunkContaining(int x, int y, int z, boolean createIfNecessary, boolean loadIfNeccessary, boolean generateIfNecessary)
	{
		return getChunk(MathHelper.floorDivision(x, Chunk.CHUNK_SIZE_HORIZONTAL),
							 MathHelper.floorDivision(z, Chunk.CHUNK_SIZE_HORIZONTAL),
							 createIfNecessary,	loadIfNeccessary, generateIfNecessary);
	}

	/**
	 * Moves a {@code Block} to a given point, this point might be in another
	 * chunk.
	 * 
	 * @param block
	 * @param newChunk
	 */
	public void moveBlockTo(int srcX, int srcY, int srcZ, int dstX, int dstY, int dstZ)
	{
		Chunk oldChunk = getChunkContaining(srcX, srcY, srcZ, false, false, false);
		Chunk newChunk = oldChunk.getChunkContaining(dstX, dstY, dstZ, true, true, false);

		int oldIndex = ChunkData.positionToIndex(srcX - oldChunk.getAbsoluteX(), srcY, srcZ - oldChunk.getAbsoluteZ());
		int newIndex = ChunkData.positionToIndex(dstX - newChunk.getAbsoluteX(), dstY, dstZ - newChunk.getAbsoluteZ());

		int blockData = oldChunk.getChunkData().getBlockData(oldIndex);
		boolean special = ChunkData.dataIsSpecial(blockData);

		if (special)
		{
			Block block = oldChunk.getChunkData().getSpecialBlock(oldIndex);
			oldChunk.removeBlockAbsolute(srcX, srcY, srcZ);
			newChunk.setSpecialBlockAbsolute(dstX, dstY, dstZ, block, false, false, false);
		} else
		{
			byte metadata = ChunkData.dataGetMetadata(blockData);
			BlockType type = _blockManager.getBlockType(oldChunk.getChunkData().getBlockType(oldIndex));
			oldChunk.setDefaultBlockAbsolute(srcX, srcY, srcZ, null, (byte) 0, true, true, false);
			newChunk.setDefaultBlockAbsolute(dstX, dstY, dstZ, type, metadata, true, true, false);
		}
	}

	public void rememberBlockMovement(int srcX, int srcY, int srcZ, int dstX, int dstY, int dstZ)
	{
		_blocksToMove.add(new BlockMovement(srcX, srcY, srcZ, dstX, dstY, dstZ));
	}

	public void performRememberedBlockChanges()
	{
		for (BlockMovement bm : _blocksToMove)
		{
			// System.out.println(bm);
			moveBlockTo(bm.srcX, bm.srcY, bm.srcZ, bm.dstX, bm.dstY, bm.dstZ);
		}
		_blocksToMove.clear();
	}

	private class BlockMovement
	{

		public int srcX, srcY, srcZ;
		public int dstX, dstY, dstZ;

		public BlockMovement(int srcX, int srcY, int srcZ, int dstX, int dstY, int dstZ)
		{
			this.srcX = srcX;
			this.srcY = srcY;
			this.srcZ = srcZ;
			this.dstX = dstX;
			this.dstY = dstY;
			this.dstZ = dstZ;
		}

		@Override
		public String toString()
		{
			return "BlockMovement{" + "srcX=" + srcX + ", srcY=" + srcY + ", srcZ=" + srcZ + ", dstX=" + dstX + ", dstY=" + dstY + ", dstZ=" + dstZ + '}';
		}
	}

	public int getTotalBlockChunkCount()
	{
		int count = 0;
		for (AbstractChunk<Chunk> ch : getAllSuperChunks())
		{
			count += ch.objectCount();
		}
		return count;
	}

	public void saveAndUnloadChunk(Chunk chunk, boolean seperateThread)
	{
		if (seperateThread)
		{
			_blockChunkThreading.saveAndUnloadChunk(chunk);
		} else
		{
			try
			{
				_blockChunkLoader.saveChunk(chunk);
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chunk.destroy();
		}
	}

	public void generateChunk(Chunk chunk, boolean seperateThread)
	{
		if (seperateThread)
		{
			_blockChunkThreading.generateChunk(chunk);
		} else
		{
			chunk.generate();
		}
	}

	public ChunkLoader getBlockChunkLoader()
	{
		return _blockChunkLoader;
	}

	public boolean isBlockChunkThreadingBusy()
	{
		return _blockChunkThreading.isTreadingBusy();
	}

	public void loadAndGenerateChunk(Chunk chunk, boolean separateThread)
	{
		if (separateThread)
		{
			_blockChunkThreading.loadAndGenerateChunk(chunk);
		} else
		{
			try
			{
				_blockChunkLoader.loadChunk(chunk);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chunk.generate();
		}
	}


}
