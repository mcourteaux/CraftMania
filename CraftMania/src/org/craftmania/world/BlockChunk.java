package org.craftmania.world;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockConstructor;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.datastructures.Fast3DArray;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec2i;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.rendering.ChunkMesh;
import org.craftmania.rendering.ChunkMeshBuilder;
import org.craftmania.rendering.ChunkMeshRenderer;
import org.craftmania.world.BlockList.BlockAcceptor;
import org.craftmania.world.generators.ChunkGenerator;

public class BlockChunk implements AABBObject
{
	public static final int BLOCKCHUNK_SIZE_HORIZONTAL = 16;
	public static final int BLOCKCHUNK_SIZE_VERTICAL = 128;
	public static final Vec3i BLOCKCHUNK_SIZE = new Vec3i(BLOCKCHUNK_SIZE_HORIZONTAL, BLOCKCHUNK_SIZE_VERTICAL, BLOCKCHUNK_SIZE_HORIZONTAL);
	public static final Vec3i HALF_BLOCKCHUNK_SIZE = new Vec3i(BLOCKCHUNK_SIZE).scale(0.5f);

	private Vec2i _position;
	private AABB _aabb;
	private int _blockCount;
	private boolean _loaded;
	private boolean _generated;
	private boolean _destroying;
	private boolean _loading;
	private boolean _cached;
	private long _creationTime;

	private ChunkMesh _mesh;
	private boolean _newVboNeeded;

	/* Blocks */
	private Fast3DArray<Block> _blocks;
	private BlockList _visibleBlocks;
	private BlockList _updatingBlocks;
	private BlockList _manualRenderBlocks;

	/* Neighbors */
	private BlockChunk[] _neighbors;

	public BlockChunk(int x, int z)
	{
		_position = new Vec2i(x, z);
		_blocks = new Fast3DArray<Block>(BLOCKCHUNK_SIZE_HORIZONTAL, BLOCKCHUNK_SIZE_VERTICAL, BLOCKCHUNK_SIZE_HORIZONTAL);
		_aabb = createAABBForBlockChunkAt(x, z);
		_visibleBlocks = new BlockList();
		_updatingBlocks = new BlockList();
		_manualRenderBlocks = new BlockList();
		_generated = false;
		_neighbors = new BlockChunk[4];

		System.out.println("BlockChunk constructed at: " + x + " " + z + "  \t (AABB = " + _aabb.toString() + ")");
		_creationTime = System.currentTimeMillis();

		_newVboNeeded = true;
	}

	public static AABB createAABBForBlockChunkAt(int x, int z)
	{
		return new AABB(new Vec3f(x * BLOCKCHUNK_SIZE_HORIZONTAL, 0, z * BLOCKCHUNK_SIZE_HORIZONTAL).add(new Vec3f(HALF_BLOCKCHUNK_SIZE)), new Vec3f(HALF_BLOCKCHUNK_SIZE));
	}

	public void needsNewVBO()
	{
		_newVboNeeded = true;
	}

	public ChunkMesh getMesh()
	{
		return _mesh;
	}

	public void setMesh(ChunkMesh mesh)
	{
		_mesh = mesh;
	}

	public void createVBO()
	{
		_newVboNeeded = false;
		performListChanges();
		_newVboNeeded = false;

		if (_mesh == null)
		{
			_mesh = new ChunkMesh();
		}

		ChunkMeshBuilder.generateChunkMesh(this);
		if (_mesh.getVBO() <= 0)
		{
			_newVboNeeded = true;
		}

	}

	public void cache()
	{
		if (_cached)
			return;
		_visibleBlocks.cache(this, BlockAcceptor.BLOCK_VISIBLE_ACCEPTOR);
		_updatingBlocks.cache(this, BlockAcceptor.BLOCK_UPDATE_ACCEPTOR);
		_manualRenderBlocks.cache(this, BlockAcceptor.NO_BLOCKS);
		_cached = true;
	}

	public void clearCache()
	{
		_visibleBlocks.clearCache();
		_updatingBlocks.clearCache();
		_manualRenderBlocks.clearCache();
		_cached = false;
	}

	public void generate()
	{
		if (!_generated)
		{
			_generated = true;
			_loading = true;
			ChunkGenerator gen = new ChunkGenerator(Game.getInstance().getWorld());
			gen.generateChunk(getX(), getZ());
			_loading = false;
		}
	}

	public boolean isGenerated()
	{
		return _generated;
	}

	public Block getBlockRelative(int x, int y, int z)
	{
		if (isDestroying())
		{
			return null;
		}

		if (x < 0 && getNeighborBlockChunk(Side.LEFT) != null)
		{
			return getNeighborBlockChunk(Side.LEFT).getBlockRelative(x + BLOCKCHUNK_SIZE_HORIZONTAL, y, z);
		}
		if (x >= BLOCKCHUNK_SIZE_HORIZONTAL && getNeighborBlockChunk(Side.RIGHT) != null)
		{
			return getNeighborBlockChunk(Side.RIGHT).getBlockRelative(x - BLOCKCHUNK_SIZE_HORIZONTAL, y, z);
		}
		if (z < 0 && getNeighborBlockChunk(Side.BACK) != null)
		{
			return getNeighborBlockChunk(Side.BACK).getBlockRelative(x, y, z + BLOCKCHUNK_SIZE_HORIZONTAL);
		}
		if (z >= BLOCKCHUNK_SIZE_HORIZONTAL && getNeighborBlockChunk(Side.FRONT) != null)
		{
			return getNeighborBlockChunk(Side.FRONT).getBlockRelative(x, y, z - BLOCKCHUNK_SIZE_HORIZONTAL);
		}
		return _blocks.get(x, y, z);
	}

	public Block getBlockAbsolute(int x, int y, int z)
	{
		BlockChunk chunk = getBlockChunkContaining(x, y, z, false, false, false);
		if (chunk == null || chunk.isDestroying())
		{
			return null;
		}
		return chunk._blocks.get(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());
	}

	public boolean isDestroying()
	{
		return _destroying;
	}

	public int getX()
	{
		return _position.x();
	}

	public int getZ()
	{
		return _position.y();
	}

	public int getAbsoluteX()
	{
		return getX() * BLOCKCHUNK_SIZE_HORIZONTAL;
	}

	public int getAbsoluteZ()
	{
		return getZ() * BLOCKCHUNK_SIZE_HORIZONTAL;
	}

	@Override
	public AABB getAABB()
	{
		return _aabb;
	}

	public boolean isEmpty()
	{
		return _blockCount == 0;
	}

	public BlockList getVisibleBlocks()
	{
		return _visibleBlocks;
	}

	public BlockList getUpdatingBlocks()
	{
		return _updatingBlocks;
	}

	public BlockList getManualRenderBlocks()
	{
		return _manualRenderBlocks;
	}

	public void setBlockTypeAbsolute(int x, int y, int z, byte blockType, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		BlockChunk chunk = getBlockChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (chunk != null)
		{
			Block block = BlockConstructor.construct(x, y, z, chunk, blockType, (byte) 0);
			chunk.setBlockAbsolute(x, y, z, block, createIfNecessary, loadIfNecessary, generateIfNecessary);
		}
	}

	public void notifyNeighborsOf(int x, int y, int z)
	{
		for (int i = 0; i < 6; ++i)
		{
			Side side = Side.getSide(i);
			Vec3i normal = side.getNormal();
			Block block = getBlockAbsolute(x + normal.x(), y + normal.y(), z + normal.z());
			if (block != null)
			{
				block.neighborChanged(Side.getOppositeSide(side));
			}
		}
	}

	public int getBlockCount()
	{
		return _blockCount;
	}

	public BlockChunk getNeighborBlockChunk(Side side)
	{
		return _neighbors[side.ordinal()];
	}

	public void setNeighborBlockChunk(Side side, BlockChunk chunk)
	{
		_neighbors[side.ordinal()] = chunk;
		if (chunk != null)
		{
			Side oppositeSide = Side.getOppositeSide(side);
			if (chunk.getNeighborBlockChunk(oppositeSide) != this)
			{
				chunk.setNeighborBlockChunk(oppositeSide, this);
			}
		}
	}

	public void performListChanges()
	{
//		System.out.println("Changes!");
		_visibleBlocks.updateCacheManagment();
		_updatingBlocks.updateCacheManagment();
		_manualRenderBlocks.updateCacheManagment();
	}

	public BlockChunk getBlockChunkContaining(int x, int y, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		int relativeX = x - getAbsoluteX();
		int relativeZ = z - getAbsoluteZ();
		/* Find out the neighboring chunk that contains our block */
		Side side = null;
		if (relativeX < 0)
		{
			side = Side.LEFT;
		} else if (relativeX >= BLOCKCHUNK_SIZE_HORIZONTAL)
		{
			side = Side.RIGHT;
		} else if (relativeZ < 0)
		{
			side = Side.BACK;
		} else if (relativeZ >= BLOCKCHUNK_SIZE_HORIZONTAL)
		{
			side = Side.FRONT;
		}
		if (side == null)
		{
			if (!isDestroying())
			{
				return this;
			} else
			{
				return null;
			}
		} else
		{
			BlockChunk neighbor = getNeighborBlockChunk(side);
			if (neighbor == null)
			{
				// return null;
				return Game.getInstance().getWorld().getChunkManager().getBlockChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			} else
			{
				return getNeighborBlockChunk(side).getBlockChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
		}
	}

	public void setBlockAbsolute(int x, int y, int z, Block block, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		BlockChunk chunk = getBlockChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (chunk != null)
		{
			Block oldBlock = chunk._blocks.set(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ(), block);

			if (chunk._cached)
			{

				if (oldBlock != null)
				{
					/* Remove old Block from the lists */
					oldBlock.removeFromVisibilityList();
					oldBlock.setRenderingFlag(false);
					if (oldBlock.getBlockType().wantsToBeUpdated())
					{
						chunk._updatingBlocks.rememberToRemoveBlock(oldBlock);
						oldBlock.setUpdatingFlag(false);
					}
				}

				if (block != null)
				{
					/* Add Block to the lists */
					block.forceVisiblilityCheck();
					if (block.isVisible())
					{
						block.addToVisibilityList();
					}
					if (block.getBlockType().wantsToBeUpdated())
					{
						block.addToUpdateList();
					}
				}

			}
			if (block != null && oldBlock == null && y >= 0 && y < BLOCKCHUNK_SIZE_VERTICAL)
				chunk._blockCount++;
			if (block == null && oldBlock != null)
				chunk._blockCount--;

			/* Set the block its position */
			if (block != null)
			{
				block.getPosition().set(x, y, z);
				/*
				 * If the source chunk is different from the target chunk,
				 * update the parenting chunk for the block
				 */
				if (block.getBlockChunk() != chunk)
				{
					block.setBlockChunk(chunk);
				}
			}

			/* Notify neighbors */
			chunk.notifyNeighborsOf(x, y, z);
			needsNewVBO();
		}
	}

	public void setGenerated(boolean b)
	{
		_generated = b;
	}

	public void setBlockTypeRelative(int x, int y, int z, byte blockID, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		setBlockTypeAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ(), blockID, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public synchronized void destroy()
	{
		if (isDestroying())
		{
			return;
		}
		_destroying = true;

		/* Remove the VBO */
		if (_mesh != null)
		{
			_mesh.destroy();
		}

		long lifeTime = System.currentTimeMillis() - _creationTime;
		System.out.println("Deleting chunk (" + getX() + ", " + getZ() + ") with lifetime " + (lifeTime / 1000.0f));

		ChunkManager chman = Game.getInstance().getWorld().getChunkManager();

		/* Clear the neighbors references */
		for (int i = 0; i < 4; ++i)
		{
			Side side = Side.values()[i];
			BlockChunk neighbor = this.getNeighborBlockChunk(side);
			if (neighbor != null)
			{
				neighbor.setNeighborBlockChunk(Side.getOppositeSide(side), null);
			}
			setNeighborBlockChunk(side, null);
		}

		/* Destroy all the blocks */
		for (int count = 0, index = 0; count < _blockCount; ++index)
		{
			Block b = _blocks.getRawObject(index);
			if (b != null)
			{
				synchronized (b)
				{
					++count;
					_blocks.setRawObject(index, null);
					chman.forgetBlockMovementsForBlock(b);
					b.setBlockChunk(null);
				}
			}
		}
		clearCache();
		_blocks.clear();

		/* Delete this chunk from the superchunk */
		int superChunkX = MathHelper.floorDivision(this.getX(), Chunk.CHUNK_SIZE_X);
		int superChunkZ = MathHelper.floorDivision(this.getZ(), Chunk.CHUNK_SIZE_Z);

		int xInChunk = getX() - superChunkX * Chunk.CHUNK_SIZE_X;
		int zInChunk = getZ() - superChunkZ * Chunk.CHUNK_SIZE_X;

		Chunk<BlockChunk> superChunk = chman.getSuperChunk(superChunkX, superChunkZ);
		superChunk.set(xInChunk, zInChunk, null);

		setGenerated(false);

	}

	public void setLoading(boolean b)
	{
		_loading = b;
	}

	public boolean isLoading()
	{
		return _loading;
	}

	public int getUniquePositionID()
	{
		return MathHelper.cantorize(MathHelper.mapToPositive(getX()), MathHelper.mapToPositive(getZ()));
	}

	public Fast3DArray<Block> getBlocks()
	{
		return _blocks;
	}

	public void render()
	{
		if (_newVboNeeded)
		{
			createVBO();
		}
		ChunkMeshRenderer.renderChunkMesh(this);

		for (int i = 0; i < _manualRenderBlocks.size(); ++i)
		{
			System.out.println("Rendering " + _manualRenderBlocks.getBlockAtIndex(i).toString() + " block manually");
			_manualRenderBlocks.getBlockAtIndex(i).render();
		}

	}

	public int getNumberOfVisibleFacesForVBO()
	{
		int count = 0;
		Block block = null;
		DefaultBlock defaultBlock = null;
		for (int i = 0; i < getVisibleBlocks().size(); ++i)
		{
			block = getVisibleBlocks().getBlockAtIndex(i);
			if (!block.isRenderingManually())
			{
				if (block instanceof DefaultBlock)
				{
					defaultBlock = (DefaultBlock) block;
					count += MathHelper.cardinality(defaultBlock.getFaceMask());
				}
			}
			
		}
		System.out.println();
		return count;
	}

	public void destroyMesh()
	{
		if (_mesh != null)
		{
			_mesh.destroy();
			_mesh = null;
			_newVboNeeded = true;
		}
	}

	public void setLoaded(boolean b)
	{
		_loaded = b;
	}
	
	public boolean isLoaded()
	{
		return _loaded;
	}

	public void setDestroying(boolean b)
	{
		_destroying = b;
	}
}
