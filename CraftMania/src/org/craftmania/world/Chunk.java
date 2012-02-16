package org.craftmania.world;

import java.util.List;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec2i;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.rendering.ChunkMesh;
import org.craftmania.rendering.ChunkMeshBuilder;
import org.craftmania.rendering.ChunkMeshRenderer;
import org.craftmania.utilities.FastArrayList;
import org.craftmania.utilities.IntList;
import org.craftmania.world.generators.ChunkGenerator;

public class Chunk implements AABBObject
{
	public static final int CHUNK_SIZE_HORIZONTAL = 16;
	public static final int CHUNK_SIZE_VERTICAL = 128;
	public static final Vec3i BLOCKCHUNK_SIZE = new Vec3i(CHUNK_SIZE_HORIZONTAL, CHUNK_SIZE_VERTICAL, CHUNK_SIZE_HORIZONTAL);
	public static final Vec3i HALF_BLOCKCHUNK_SIZE = new Vec3i(BLOCKCHUNK_SIZE).scale(0.5f);
	public static final int BLOCK_COUNT = CHUNK_SIZE_HORIZONTAL * CHUNK_SIZE_HORIZONTAL * CHUNK_SIZE_VERTICAL;

	private static final BlockManager _blockManager = BlockManager.getInstance();

	private Vec2i _position;
	private AABB _aabb;
	private int _blockCount;
	private boolean _loaded;
	private boolean _generated;
	private boolean _destroying;
	private boolean _loading;
	private long _creationTime;
	private boolean _sunlightDirty;
	private World _world;

	private ChunkMesh _mesh;
	private boolean _newVboNeeded;

	/* Blocks */
	private ChunkData _chunkData;
	private IntList _visibleBlocks;
	private IntList _updatingBlocks;
	private IntList _manualRenderingBlocks;

	/* Neighbors */
	private Chunk[] _neighbors;

	public static enum LightType
	{
		SUN, BLOCK
	}

	public Chunk(int x, int z)
	{
		/* General */
		_position = new Vec2i(x, z);
		_aabb = createAABBForBlockChunkAt(x, z, null);
		_neighbors = new Chunk[4];

		_chunkData = new ChunkData();

		/* Integer Lists */
		_visibleBlocks = new IntList(512);
		_updatingBlocks = new IntList();
		_manualRenderingBlocks = new IntList();

		/* Initial chunk states */
		_newVboNeeded = true;
		_generated = false;
		_loaded = false;
		_loading = false;
		_world = Game.getInstance().getWorld();

		// System.out.println("BlockChunk constructed at: " + x + " " + z +
		// "  \t (AABB = " + _aabb.toString() + ")");
		_creationTime = System.currentTimeMillis();

	}

	public static AABB createAABBForBlockChunkAt(int x, int z, AABB output)
	{
		if (output == null)
		{
			return new AABB(new Vec3f(x * CHUNK_SIZE_HORIZONTAL, 0, z * CHUNK_SIZE_HORIZONTAL).add(HALF_BLOCKCHUNK_SIZE), new Vec3f(HALF_BLOCKCHUNK_SIZE));
		} else
		{
			output.getPosition().set(x * CHUNK_SIZE_HORIZONTAL, 0, z * CHUNK_SIZE_HORIZONTAL).add(HALF_BLOCKCHUNK_SIZE);
			output.getDimensions().set(HALF_BLOCKCHUNK_SIZE);
			return output;
		}
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
		} else
		{
			_newVboNeeded = false;
		}

	}

	public void generate()
	{
		_loading = true;
		if (!_generated)
		{
			_generated = true;
			ChunkGenerator gen = new ChunkGenerator(Game.getInstance().getWorld());
			gen.generateChunk(getX(), getZ());

			markNeighborsSunlightDirty();
		}
		_loading = false;
		/* Generate sunlight */
		 generateSunlight();
	}


	public int getBlockCount()
	{
		return _blockCount;
	}

	public Chunk getNeighborChunk(Side side)
	{
		return _neighbors[side.ordinal()];
	}

	public void setNeighborBlockChunk(Side side, Chunk chunk)
	{
		_neighbors[side.ordinal()] = chunk;
		if (chunk != null)
		{
			Side oppositeSide = Side.getOppositeSide(side);
			if (chunk.getNeighborChunk(oppositeSide) != this)
			{
				chunk.setNeighborBlockChunk(oppositeSide, this);
			}
			/*
			 * Now, we need to update the visibility of the blocks on the edge
			 * of the chunk
			 */
			updateVisiblityForAllBlocksOnSide(side);
		}
	}

	public void updateVisiblityForAllBlocksOnSide(Side side)
	{
		boolean varX;
		int j;

		if (side.getNormal().x() == 1)
		{
			j = CHUNK_SIZE_HORIZONTAL - 1;
			varX = false;
		} else if (side.getNormal().x() == -1)
		{
			j = 0;
			varX = false;
		} else if (side.getNormal().z() == 1)
		{
			j = CHUNK_SIZE_HORIZONTAL - 1;
			varX = true;
		} else if (side.getNormal().z() == -1)
		{
			j = 0;
			varX = true;
		} else
		{
			return;
		}

		for (int i = 0; i < CHUNK_SIZE_HORIZONTAL; ++i)
		{
			for (int y = 0; y < CHUNK_SIZE_VERTICAL; ++y)
			{
				if (varX)
				{
					updateVisibilityFor(getAbsoluteX() + i, y, getAbsoluteZ() + j);
				} else
				{
					updateVisibilityFor(getAbsoluteX() + j, y, getAbsoluteZ() + i);
				}
			}
		}
	}

	public void performListChanges()
	{
		// System.out.println("Changes!");
		_visibleBlocks.executeModificationBuffer();
		_updatingBlocks.executeModificationBuffer();
		_manualRenderingBlocks.executeModificationBuffer();
	}

	public Chunk getChunk(int x, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		if (x == getX() && z == getZ())
		{
			return this;
		} else
		{
			if (x < getX())
			{
				Chunk c = getNeighborChunk(Side.LEFT);
				if (c != null)
					return c.getChunk(x, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
			if (x > getX())
			{
				Chunk c = getNeighborChunk(Side.RIGHT);
				if (c != null)
					return c.getChunk(x, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
			if (z < getZ())
			{
				Chunk c = getNeighborChunk(Side.FRONT);
				if (c != null)
					return c.getChunk(x, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
			if (z > getZ())
			{
				Chunk c = getNeighborChunk(Side.BACK);
				if (c != null)
					return c.getChunk(x, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
		}
		return Game.getInstance().getWorld().getChunkManager().getChunk(x, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public Chunk getChunkContaining(int x, int y, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		int relativeX = x - getAbsoluteX();
		int relativeZ = z - getAbsoluteZ();
		/* Find out the neighboring chunk that contains our block */
		Side side = null;
		if (relativeX < 0)
		{
			side = Side.LEFT;
		} else if (relativeX >= CHUNK_SIZE_HORIZONTAL)
		{
			side = Side.RIGHT;
		} else if (relativeZ < 0)
		{
			side = Side.BACK;
		} else if (relativeZ >= CHUNK_SIZE_HORIZONTAL)
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
			Chunk neighbor = getNeighborChunk(side);
			if (neighbor == null)
			{
				// return null;
				return Game.getInstance().getWorld().getChunkManager().getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			} else
			{
				return getNeighborChunk(side).getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
			}
		}
	}

	public byte getBlockTypeAbsolute(int x, int y, int z, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		if (isInvalidHeight(y))
		{
			return -1;
		}
		Chunk c = getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (c != null)
		{
			return c._chunkData.getBlockType(ChunkData.positionToIndex(x - c.getAbsoluteX(), y, z - c.getAbsoluteZ()));
		}
		return -1;
	}

	private boolean isInvalidHeight(int y)
	{
		return y >= 128 || y < 0;
	}

	public void rebuildVisibilityBuffer()
	{
		_visibleBlocks.clear();
		Vec3i v = new Vec3i();
		for (int i = 0; i < Chunk.BLOCK_COUNT; ++i)
		{
			ChunkData.indexToPosition(i, v);
			boolean special = getChunkData().isSpecial(i);

			/* Clear the face mask */
			if (!special)
			{
				getChunkData().setFaceMask(i, (byte) 0);
			}
			updateVisibilityFor(getAbsoluteX() + v.x(), v.y(), getAbsoluteZ() + v.z());
		}
		performListChanges();
	}

	private void updateVisibilityForNeigborsOf(int x, int y, int z)
	{
		updateVisibilityFor(x - 1, y, z);
		updateVisibilityFor(x + 1, y, z);
		updateVisibilityFor(x, y - 1, z);
		updateVisibilityFor(x, y + 1, z);
		updateVisibilityFor(x, y, z - 1);
		updateVisibilityFor(x, y, z + 1);
	}

	public byte updateVisibilityFor(int x, int y, int z)
	{
		if (isInvalidHeight(y))
			return -1;
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk != null)
		{
			int index = ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());
			byte type = chunk.getChunkData().getBlockType(index);
			if (type <= 0)
			{
				return 0;
			}

			boolean special = chunk.getChunkData().isSpecial(index);

			/* Build the face mask */
			if (!special)
			{
				byte oldFaceMask = chunk.getChunkData().getFaceMask(index);
				byte faceMask = 0;
				for (int i = 0; i < 6; ++i)
				{
					Side s = Side.getSide(i);
					Vec3i normal = s.getNormal();
					byte block = chunk.getBlockTypeAbsolute(x + normal.x(), y + normal.y(), z + normal.z(), false, false, false);

					if (block == -1)
					{
						/*
						 * If the block should be in a neighboring chunk, but
						 * the neighbor doesn't exist, make it invisible
						 */
						faceMask = MathHelper.setBit(faceMask, i, false);
					} else
					{
						if (block != 0)
						{
							BlockType blockType = _blockManager.getBlockType(block);
							if (blockType.hasNormalAABB())
							{
								faceMask = MathHelper.setBit(faceMask, i, false);
							} else
							{
								faceMask = MathHelper.setBit(faceMask, i, true);
							}
						} else
						{
							faceMask = MathHelper.setBit(faceMask, i, true);
						}
					}
				}
				chunk.getChunkData().setFaceMask(index, faceMask);

				if (oldFaceMask != faceMask)
				{
					if (oldFaceMask == 0)
					{
						chunk.getVisibleBlocks().bufferAdd(index);
					} else if (faceMask == 0)
					{
						chunk.getVisibleBlocks().bufferRemove(index);
					}
					chunk.needsNewVBO();
				}
				return faceMask;
			} else
			{
				Block block = chunk._chunkData.getSpecialBlock(index);
				block.checkVisibility();
				if (block instanceof DefaultBlock)
				{
					return ((DefaultBlock) block).getFaceMask();
				}
				return -2;
			}
		}
		return -1;
	}

	public void setDefaultBlockRelative(int x, int y, int z, BlockType type, byte metadata, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		setDefaultBlockAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ(), type, metadata, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public void setDefaultBlockAbsolute(int x, int y, int z, BlockType type, byte metadata, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		if (isInvalidHeight(y))
		{
			return;
		}
		Chunk chunk = getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (chunk != null)
		{
			int index = ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());

			byte blockType = chunk._chunkData.getBlockType(index);
			byte blocklight = chunk._chunkData.getBlockLight(index);
			byte sunlight = chunk._chunkData.getSunlight(index);
			if (blockType != 0)
			{
				removeBlockAbsolute(x, y, z);
			}

			if (type == null)
			{
				chunk._chunkData.clearBlock(index);
				chunk.needsNewVBO();
				return;
			}
			
			/* Unspread the sunlight */
			unspreadSunlight(x, z);

			/* Set it in the chunk data */
			chunk._chunkData.setDefaultBlock(index, type.getID(), (byte) 0, metadata);

			/* Build the face mask */
			chunk.updateVisibilityFor(x, y, z);
			chunk.updateVisibilityForNeigborsOf(x, y, z);

			/* Clear the light at this position if solid */
			if (!type.isTransculent())
			{
				chunk._chunkData.clearLight(index);
				/* Unspread light */
				unspreadLight(x, y, z, blocklight, LightType.BLOCK);
				unspreadLight(x, y, z, sunlight, LightType.SUN);
				/* Respread sunlight */
				spreadSunlight(x, z);
			}

		}
	}

	public void setSpecialBlockAbsolute(int x, int y, int z, Block block, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		if (block == null)
		{
			removeBlockAbsolute(x, y, z);
			return;
		}
		Chunk chunk = getChunkContaining(x, y, z, createIfNecessary, loadIfNecessary, generateIfNecessary);
		if (chunk != null)
		{
			chunk._chunkData.setSpecialBlock(ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ()), block);
			chunk.updateVisibilityFor(x - 1, y, z);
			chunk.updateVisibilityFor(x + 1, y, z);
			chunk.updateVisibilityFor(x, y - 1, z);
			chunk.updateVisibilityFor(x, y + 1, z);
			chunk.updateVisibilityFor(x, y, z - 1);
			chunk.updateVisibilityFor(x, y, z + 1);
		}
	}

	public void setSpecialBlockRelative(int x, int y, int z, Block block, boolean createIfNecessary, boolean loadIfNecessary, boolean generateIfNecessary)
	{
		setSpecialBlockAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ(), block, createIfNecessary, loadIfNecessary, generateIfNecessary);
	}

	public void removeBlockAbsolute(int x, int y, int z)
	{
		if (isInvalidHeight(y))
			return;
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk != null)
		{
			int index = ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());
			/*
			 * Remove from the old one from the visibility/update/render list
			 */
			boolean oldSpecial = chunk._chunkData.isSpecial(index);
			if (oldSpecial)
			{
				Block block = chunk._chunkData.getSpecialBlock(index);
				block.removeFromVisibilityList();
				block.removeFromManualRenderList();
				block.removeFromUpdateList();
			} else if (chunk._chunkData.getFaceMask(index) != 0)
			{
				chunk._visibleBlocks.bufferRemove(index);
			}

			chunk._chunkData.clearBlock(index);
			chunk.updateVisibilityForNeigborsOf(x, y, z);
			chunk.needsNewVBO();

			/* Get the highest neighboring light value */
			Side side;
			Vec3i vec = new Vec3i();
			byte blocklight = 0;
			byte sunlight = 0;
			for (int i = 0; i < 6; ++i)
			{
				side = Side.getSide(i);
				vec.set(x + side.getNormal().x(), y + side.getNormal().y(), z + side.getNormal().z());
				blocklight = (byte) Math.max(blocklight, getLightAbsolute(vec.x(), vec.y(), vec.z(), LightType.BLOCK));
				sunlight = (byte) Math.max(sunlight, getLightAbsolute(vec.x(), vec.y(), vec.z(), LightType.SUN));
			}

			chunk.spreadLight(x, y, z, (byte) (blocklight - 1), LightType.BLOCK);
			chunk.spreadLight(x, y, z, (byte) (sunlight - 1), LightType.SUN);
			chunk.spreadSunlight(x, z);
		}
	}

	public void removeBlockRelative(int x, int y, int z)
	{
		removeBlockAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ());
	}

	public void setGenerated(boolean b)
	{
		_generated = b;
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

		// long lifeTime = System.currentTimeMillis() - _creationTime;
		// System.out.println("Deleting chunk (" + getX() + ", " + getZ() +
		// ") with lifetime " + (lifeTime / 1000.0f));

		ChunkManager chman = Game.getInstance().getWorld().getChunkManager();

		/* Clear the neighbors references */
		for (int i = 0; i < 4; ++i)
		{
			Side side = Side.getSide(i);
			Chunk neighbor = this.getNeighborChunk(side);
			if (neighbor != null)
			{
				neighbor.setNeighborBlockChunk(Side.getOppositeSide(side), null);
			}
			setNeighborBlockChunk(side, null);
		}

		/* TODO: Destroy all special blocks */

		/* Delete this chunk from the superchunk */
		int superChunkX = MathHelper.floorDivision(this.getX(), AbstractChunk.CHUNK_SIZE_X);
		int superChunkZ = MathHelper.floorDivision(this.getZ(), AbstractChunk.CHUNK_SIZE_Z);

		int xInChunk = getX() - superChunkX * AbstractChunk.CHUNK_SIZE_X;
		int zInChunk = getZ() - superChunkZ * AbstractChunk.CHUNK_SIZE_X;

		AbstractChunk<Chunk> superChunk = chman.getSuperChunk(superChunkX, superChunkZ);
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

	public void render()
	{
		if (_sunlightDirty)
		{
			regenerateSunlight();
		}
		if (_newVboNeeded)
		{
			createVBO();
		}
		ChunkMeshRenderer.renderChunkMesh(this);

	}

	public int getNumberOfVisibleFacesForVBO()
	{
		int count = 0;
		int blockIndex = 0;
		int blockData = 0;
		boolean special;
		Block block = null;
		for (int i = 0; i < _visibleBlocks.size(); ++i)
		{
			blockIndex = _visibleBlocks.get(i);
			blockData = _chunkData.getBlockData(blockIndex);
			special = ChunkData.dataIsSpecial(blockData);
			if (!special)
			{
				byte faceMask = ChunkData.dataGetFaceMask(blockData);
				count += MathHelper.cardinality(faceMask);
			} else
			{
				System.out.println("Block Data = " + Integer.toHexString(blockData));
				block = _chunkData.getSpecialBlock(blockIndex);
				if (block instanceof DefaultBlock && !block.isRenderingManually())
				{
					System.out.println(block);
					byte faceMask = ((DefaultBlock) block).getFaceMask();
					count += MathHelper.cardinality(faceMask);
				}
			}
			
		}
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

	public IntList getVisibleBlocks()
	{
		return _visibleBlocks;
	}

	public IntList getUpdatingBlocks()
	{
		return _updatingBlocks;
	}

	public IntList getManualRenderingBlocks()
	{
		return _manualRenderingBlocks;
	}

	@Override
	public String toString()
	{
		return "Chunk(" + getX() + ", " + getZ() + ")";
	}

	/**
	 * ONLY FOR TESTING!!
	 */
	public void unsetNetVBONeeded()
	{
		_newVboNeeded = false;
	}
	
	public void markChunkForNewVBO(int x, int z)
	{
		Chunk c = getChunk(x, z, false, false, false);
		if (c != null)
		{
			c.needsNewVBO();
		}
	}

	public byte getLightAbsolute(int x, int y, int z, LightType type)
	{
		if (isInvalidHeight(y))
		{
			return 0;
		}
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk == null)
		{
			return 0;
		}
		int index = ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());
		return chunk._chunkData.getLight(index, type);
	}

	public byte getLightRelative(int x, int y, int z, LightType type)
	{
		return getLightAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ(), type);
	}

	public void setLightRelative(int x, int y, int z, byte light, LightType type)
	{
		setLightAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ(), light, type);
	}

	public void setLightAbsolute(int x, int y, int z, byte light, LightType type)
	{
		if (_loading)
		{
			/* Do not update light system when loading */
			return;
		}
		if (isInvalidHeight(y))
		{
			return;
		}
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk == null)
		{
			return;
		}
		int relX = x - chunk.getAbsoluteX();
		int relZ = z - chunk.getAbsoluteZ();
		int index = ChunkData.positionToIndex(relX, y, relZ);
		chunk._chunkData.setLight(index, light, type);
		chunk.needsNewVBO();
		
		if (relX == 0)
		{
			if (relZ == 0)
			{
				markChunkForNewVBO(chunk.getX() - 1, chunk.getZ() - 1);
			} else if (relZ == CHUNK_SIZE_HORIZONTAL - 1)
			{
				markChunkForNewVBO(chunk.getX() - 1, chunk.getZ() + 1);
			}
			markChunkForNewVBO(chunk.getX() - 1, chunk.getZ());
		}
		if (relX == CHUNK_SIZE_HORIZONTAL - 1)
		{
			if (relZ == 0)
			{
				markChunkForNewVBO(chunk.getX() + 1, chunk.getZ() - 1);
			} else if (relZ == CHUNK_SIZE_HORIZONTAL - 1)
			{
				markChunkForNewVBO(chunk.getX() + 1, chunk.getZ() + 1);
			}
			markChunkForNewVBO(chunk.getX() + 1, chunk.getZ());
		}
		if (relZ == 0)
		{
			markChunkForNewVBO(chunk.getX(), chunk.getZ() - 1);
		} else if (relZ == CHUNK_SIZE_HORIZONTAL - 1)
		{
			markChunkForNewVBO(chunk.getX(), chunk.getZ() + 1);
		}
	}

	public byte getTotalLightAbsolute(int x, int y, int z)
	{
		if (isInvalidHeight(y))
		{
			return 0;
		}
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk == null)
		{
			return 0;
		}
		int index = ChunkData.positionToIndex(x - chunk.getAbsoluteX(), y, z - chunk.getAbsoluteZ());
		return chunk._chunkData.getTotalLight(index);
	}

	public byte getTotalLightRelative(int x, int y, int z)
	{
		return getTotalLightAbsolute(x + getAbsoluteX(), y, z + getAbsoluteZ());
	}

	public void spreadSunlight(int x, int z)
	{
		Chunk chunk = getChunkContaining(x, 0, z, false, false, false);
		if (chunk == null || chunk._loading)
			return;

		boolean spreading = false;

		for (int y = CHUNK_SIZE_VERTICAL - 1; y > 0; --y)
		{
			byte type = chunk.getBlockTypeAbsolute(x, y, z, false, false, false);
			if (type < 0)
				return;
			if (type != 0)
			{
				BlockType btype = BlockManager.getInstance().getBlockType(type);
				if (!btype.isTransculent())
				{
					return;
				}
			}

			if (!spreading)
			{
				Side side;
				Vec3i vec = new Vec3i();
				inner: for (int i = 0; i < 4; ++i)
				{
					side = Side.getSide(i);
					vec.set(x + side.getNormal().x(), y + side.getNormal().y(), z + side.getNormal().z());
					byte btype = chunk.getBlockTypeAbsolute(vec.x(), vec.y(), vec.z(), false, false, false);
					if (btype > 0)
					{
						spreading = true;
						break inner;
					}
				}
			}

			if (spreading)
			{
				chunk.spreadLight(x, y, z, (byte) _world.getSunlight(), LightType.SUN);
			} else
			{
				chunk.setLightAbsolute(x, y, z, (byte) _world.getSunlight(), LightType.SUN);
			}
		}
	}

	public void unspreadSunlight(int x, int z)
	{
		Chunk chunk = getChunkContaining(x, 0, z, false, false, false);
		if (chunk._loading)
			return;

		boolean covered = false;
		for (int y = CHUNK_SIZE_VERTICAL - 1; y > 0; --y)
		{
			byte type = chunk.getBlockTypeAbsolute(x, y, z, false, false, false);
			if (type < 0)
				return;
			if (type != 0)
			{
				BlockType btype = BlockManager.getInstance().getBlockType(type);
				if (!btype.isTransculent())
				{
					continue;
				}
				covered = true;
				continue;
			}
			if (covered)
			{
				chunk.unspreadLight(x, y, z, (byte) 15, LightType.SUN);
			}
		}
	}

	public void spreadLight(int x, int y, int z, byte light, LightType lightType)
	{
		if (light == 0)
			return;

		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk._loading)
			return;

		/* This block */
		{
			byte oldLight = chunk.getLightAbsolute(x, y, z, lightType);
			if (oldLight < light)
			{
				chunk.setLightAbsolute(x, y, z, light, lightType);
			}
		}

		Side side;
		Vec3i vec = new Vec3i();
		for (int i = 0; i < 6; ++i)
		{
			side = Side.getSide(i);
			vec.set(x + side.getNormal().x(), y + side.getNormal().y(), z + side.getNormal().z());

			/* Conditions */
			if (isInvalidHeight(vec.y()))
				continue;
			byte type = chunk.getBlockTypeAbsolute(vec.x(), vec.y(), vec.z(), false, false, false);
			if (type < 0)
				continue;
			if (type != 0)
			{
				BlockType btype = BlockManager.getInstance().getBlockType(type);
				if (!btype.isTransculent())
				{
					continue;
				}
			}

			/* Recursive */
			byte nLight = chunk.getLightAbsolute(vec.x(), vec.y(), vec.z(), lightType);
			if (nLight < light - 1)
			{
				chunk.spreadLight(vec.x(), vec.y(), vec.z(), (byte) (light - 1), lightType);
			}
		}

	}

	public void unspreadLight(int x, int y, int z, byte light, LightType type)
	{
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk._loading)
			return;
		List<Vec3i> brightSpots = new FastArrayList<Vec3i>();
		chunk.unspreadLight(x, y, z, light, type, brightSpots);

		for (Vec3i v : brightSpots)
		{
			byte spot = getLightAbsolute(v.x(), v.y(), v.z(), type);
			chunk.spreadLight(v.x(), v.y(), v.z(), spot, type);
		}
	}

	private void unspreadLight(int x, int y, int z, byte light, LightType lightType, List<Vec3i> brightSpots)
	{
		Chunk chunk = getChunkContaining(x, y, z, false, false, false);
		if (chunk._loading)
			return;
		if (light == 0)
			return;

		/* This block */
		{
			byte oldLight = chunk.getLightAbsolute(x, y, z, lightType);
			if (oldLight == light)
			{
				chunk.setLightAbsolute(x, y, z, (byte) 0, lightType);
			}
		}

		Side side;
		Vec3i vec = new Vec3i();
		for (int i = 0; i < 6; ++i)
		{
			side = Side.getSide(i);
			vec.set(x + side.getNormal().x(), y + side.getNormal().y(), z + side.getNormal().z());
			if (isInvalidHeight(vec.y()))
				continue;
			byte type = chunk.getBlockTypeAbsolute(vec.x(), vec.y(), vec.z(), false, false, false);
			if (type < 0)
				continue;
			if (type != 0)
			{
				BlockType btype = BlockManager.getInstance().getBlockType(type);
				if (!btype.isTransculent())
				{
					continue;
				}
			}
			/* Recursive */
			byte nLight = chunk.getLightAbsolute(vec.x(), vec.y(), vec.z(), lightType);
			if (nLight == light - 1 && nLight > 0)
			{
				chunk.unspreadLight(vec.x(), vec.y(), vec.z(), nLight, lightType, brightSpots);
			} else if (nLight == 0)
			{
				chunk.setLightAbsolute(vec.x(), vec.y(), vec.z(), nLight, lightType);
			} else
			{
				/* Bright spots will be respreaded afterwards */
				brightSpots.add(new Vec3i(vec));
			}
		}
	}

	public void generateSunlight()
	{
		for (int x = 0; x < CHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int z = 0; z < CHUNK_SIZE_HORIZONTAL; ++z)
			{
				spreadSunlight(getAbsoluteX() + x, getAbsoluteZ() + z);
			}
		}
		_sunlightDirty = false;
	}
	
	public void regenerateSunlight()
	{
		for (int i = 0; i < BLOCK_COUNT; ++i)
		{
			_chunkData.setSunlight(i, (byte) 0);
		}
		generateSunlight();
	}
	

	public void setSunlightDirty(boolean b)
	{
		_sunlightDirty = b;
	}

	public void markNeighborsSunlightDirty()
	{
		Chunk c;

		c = getChunk(getX() - 1, getZ() - 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX(), getZ() - 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX() + 1, getZ() - 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX() - 1, getZ(), false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX() + 1, getZ(), false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX() - 1, getZ() + 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX(), getZ() + 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

		c = getChunk(getX() + 1, getZ() + 1, false, false, false);
		if (c != null)
			c._sunlightDirty = true;

	}
	
	public boolean isGenerated()
	{
		return _generated;
	}

	public ChunkData getChunkData()
	{
		return _chunkData;
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
		return getX() * CHUNK_SIZE_HORIZONTAL;
	}

	public int getAbsoluteZ()
	{
		return getZ() * CHUNK_SIZE_HORIZONTAL;
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

	public void notifyNeighborsOf(int x, int y, int z)
	{
		for (int i = 0; i < 6; ++i)
		{
			Side side = Side.getSide(i);
			Vec3i normal = side.getNormal();
			// TODO
		}
	}


}
