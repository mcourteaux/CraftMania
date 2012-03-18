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
import org.craftmania.world.characters.Player;

public class ChunkManager
{
	private World _world;
	private BlockManager _blockManager;
	private Map<Integer, AbstractChunk<Chunk>> _superChunks;
	private List<BlockMovement> _blocksToMove;
	private ChunkIO _chunkLoader;
	private ChunkThreading _blockChunkThreading;

	public ChunkManager(World world)
	{
		_world = world;
		_superChunks = new HashMap<Integer, AbstractChunk<Chunk>>();
		_blocksToMove = new ArrayList<ChunkManager.BlockMovement>();
		_chunkLoader = new ChunkIO();
		_blockChunkThreading = new ChunkThreading(this);
		_blockManager = BlockManager.getInstance();
	}

	public World getWorld()
	{
		return _world;
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

		synchronized (_superChunks)
		{
			AbstractChunk<Chunk> superChunk = getSuperChunk(superX, superZ);

			Chunk chunk = superChunk.get(xInChunk, zInChunk);
			if (chunk == null && createIfNecessary)
			{
				chunk = new Chunk(x, z);
				assignNeighbors(chunk);
				superChunk.set(xInChunk, zInChunk, chunk);
			}
			if (chunk != null && !chunk.isLoaded() && loadIfNecessary && !chunk.isLoading() && !chunk.isDestroying())
			{
				try
				{
					_chunkLoader.loadChunk(chunk);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (chunk != null && generateIfNecessary && !chunk.isLoading() && !chunk.isGenerated() && !chunk.isDestroying())
			{
				chunk.generate();
			}
			return chunk;
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
		int index = ChunkData.positionToIndex(x - c.getAbsoluteX(), y, z - c.getAbsoluteZ());
		return c.getChunkData().getSpecialBlock(index);
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
					if (chunk != null && !chunk.isDestroying())
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
		return getChunk(MathHelper.floorDivision(x, Chunk.CHUNK_SIZE_HORIZONTAL), MathHelper.floorDivision(z, Chunk.CHUNK_SIZE_HORIZONTAL), createIfNecessary, loadIfNeccessary, generateIfNecessary);
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

			/* Remember in which lists the block was present */
			boolean updating = block.isUpdating();
			boolean visible = block.isRendering();
			boolean renderMan = block.isRenderingManually();

			oldChunk.removeBlockAbsolute(srcX, srcY, srcZ);
			newChunk.setSpecialBlockAbsolute(dstX, dstY, dstZ, block, false, false, false);

			/* Put it again in the lists it was before the movement */
			if (updating)
				block.addToUpdateList();
			if (visible)
				block.addToVisibilityList();
			if (renderMan)
				block.addToManualRenderList();

			/* Check the visibility of neighbors again */
			newChunk.updateVisibilityForNeigborsOf(dstX, dstY, dstZ);

		} else
		{
			byte metadata = ChunkData.dataGetMetadata(blockData);
			BlockType type = _blockManager.getBlockType(oldChunk.getChunkData().getBlockType(oldIndex));
			oldChunk.removeBlockAbsolute(srcX, srcY, srcZ);
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

	private static class BlockMovement
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

	public int getTotalChunkCount()
	{
		int count = 0;
		synchronized (_superChunks)
		{
			for (AbstractChunk<Chunk> ch : getAllSuperChunks())
			{
				count += ch.objectCount();
			}
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
				_chunkLoader.saveChunk(chunk);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			while (!chunk.destroy())
			{
				try
				{
					Thread.sleep(10);
				} catch (Exception e)
				{
				}
			}
		}
	}

	public void generateChunk(Chunk chunk, boolean seperateThread)
	{
		if (seperateThread)
		{
			Player p = _world.getActivePlayer();
			float xDiff = chunk.getAbsoluteX() - p.getPosition().x();
			float zDiff = chunk.getAbsoluteZ() - p.getPosition().z();

			_blockChunkThreading.generateChunk(chunk, (int) (xDiff * xDiff + zDiff * zDiff));
		} else
		{
			chunk.generate();
		}
	}

	public ChunkIO getBlockChunkLoader()
	{
		return _chunkLoader;
	}

	public boolean isBlockChunkThreadingBusy()
	{
		return _blockChunkThreading.isTreadingBusy();
	}

	public void loadAndGenerateChunk(Chunk chunk, boolean separateThread)
	{
		if (separateThread)
		{
			Player p = _world.getActivePlayer();
			float xDiff = chunk.getAbsoluteX() - p.getPosition().x();
			float zDiff = chunk.getAbsoluteZ() - p.getPosition().z();

			_blockChunkThreading.loadAndGenerateChunk(chunk, (int) (xDiff * xDiff + zDiff * zDiff));
		} else
		{
			try
			{
				_chunkLoader.loadChunk(chunk);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chunk.generate();
		}
	}

	public boolean isLoadingThreadPoolFull()
	{
		return _blockChunkThreading.isLoadingThreadPoolFull();
	}

}
