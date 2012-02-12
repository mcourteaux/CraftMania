package org.craftmania.blocks;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.game.Game;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;

public class DefaultBlock extends Block
{
	public static final Vec3f HALF_BLOCK_SIZE = new Vec3f(0.5f, 0.5f, 0.5f);
	public static final Vec3f BLOCK_SIZE = new Vec3f(1.0f, 1.0f, 1.0f);

	public static final byte ALL_FACES = 0x3f; // 0011 1111

	private boolean _needVisibilityCheck;
	private byte _faceMask;
	private boolean _visible;

	private BlockMovementPlugin _movement;

	public DefaultBlock(BlockType type, Chunk chunk, Vec3i pos)
	{
		super(type, chunk, pos);
		_aabb = null;
		_needVisibilityCheck = true;
	}

	private void createMovementPlugin()
	{
		_movement = new BlockMovementPlugin(this);
	}

	public boolean hasMovementPlugin()
	{
		return _movement != null;
	}

	@Override
	public void update()
	{
		if (!_blockType.isFixed())
		{
			byte supportingBlock = _blockChunk.getBlockTypeAbsolute(getX(), getY() - 1, getZ(), false, false, false);
			if (supportingBlock == 0)
			{
				if (!isFalling())
				{
					/* Start falling */
					if (!hasMovementPlugin())
					{
						createMovementPlugin();
						addToManualRenderList();
						_needVisibilityCheck = true;
						_blockChunk.needsNewVBO();
					}
					_movement.setFalling(true);
					_blockChunk.notifyNeighborsOf(getX(), getY(), getZ());
				}
			} else
			{
				if (isFalling())
				{
					/* Stop falling */
					_movement.setFalling(false);
					_blockChunk.notifyNeighborsOf(getX(), getY(), getZ());
					removeFromManualRenderList();
					_needVisibilityCheck = true;
					_blockChunk.needsNewVBO();
				}
			}

			if (isFalling())
			{
				float step = Game.getInstance().getStep();
				_movement.getMotion().setY(_movement.getMotion().y() - (9.81f * step));
			} else if (hasMovementPlugin())
			{
				_movement.getAdditionCoordinates().setY(0.0f);
				_movement.getMotion().setY(0.0f);
				/* Update pos */
				_aabb.getPosition().set(_postion).add(HALF_BLOCK_SIZE).add(_movement.getAdditionCoordinates());
				_aabb.recalcVertices();
			}
		}

		if (isMoving())
		{
			_movement.solveMotion();
			if (!_movement.isMoving())
			{
				/* Destroy the plug-in */
				_movement = null;
			}
		} else
		{
			/* Remove this block from the update list */
			removeFromUpdateList();
		}
	}

	private boolean isFalling()
	{
		return hasMovementPlugin() && _movement.isFalling();
	}

	/**
	 * Sets the bit in the face mask which represents the visibility of the
	 * face.
	 * 
	 * @param face
	 * @param flag
	 */
	public void setFaceVisible(Side face, boolean flag)
	{
		if (flag)
		{
			_faceMask |= 1 << face.ordinal();
		} else
		{
			_faceMask &= ~(1 << face.ordinal());
		}
	}

	@Override
	public void render()
	{
		_renderManually = true;
		_rendering = true;
		if (isVisible())
		{
			_blockType.getBrush().setPosition(_aabb.getPosition());
			_blockType.getBrush().renderFaces(_faceMask);
		}
	}

	@Override
	public boolean isVisible()
	{
		if (_needVisibilityCheck)
		{
			checkVisibility();
		}
		return _visible;
	}

	private synchronized void checkVisibility()
	{
		boolean preVisibility = _faceMask != 0;
		byte preMask = _faceMask;
		if (isMoving())
		{
			_faceMask = ALL_FACES;
		} else
		{
			if (_blockChunk != null)
			{
				for (int i = 0; i < 6; ++i)
				{
					Side side = Side.getSide(i);
					Vec3i normal = side.getNormal();
					Chunk chunk = _blockChunk.getChunkContaining(getX() + normal.x(), getY() + normal.y(), getZ() + normal.z(), false, false, false);
					if (chunk == null)
					{
						/* TODO Solve */
						setFaceVisible(side, true);
					} else
					{
						byte block = chunk.getBlockTypeAbsolute(getX() + normal.x(), getY() + normal.y(), getZ() + normal.z(), false, false, false);
						if (block != 0)
						{
							if (false || !BlockManager.getInstance().getBlockType(block).hasNormalAABB())
							{
								setFaceVisible(side, true);
							} else
							{
								setFaceVisible(side, false);
							}
						} else
						{
							setFaceVisible(side, true);
						}
					}
				}
			}
			/* Make the bottom layer invisible */
			if (getY() == 0)
			{
				setFaceVisible(Side.BOTTOM, false);
			}
		}
		_needVisibilityCheck = false;

		_visible = _faceMask != 0;

		if (_visible != preVisibility)
		{
			if (_visible)
			{
				addToVisibilityList();
			} else
			{
				removeFromVisibilityList();
			}
			
			_blockChunk.needsNewVBO();
		} else if (preMask != _faceMask)
		{
			_blockChunk.needsNewVBO();
		}
	}

	@Override
	public synchronized AABB getAABB()
	{
		if (_aabb == null)
		{
			_aabb = new AABB(new Vec3f(getPosition()).add(HALF_BLOCK_SIZE), HALF_BLOCK_SIZE);
		}
		return _aabb;
	}

	@Override
	public void smash(InventoryItem item)
	{
	}

	@Override
	public synchronized void neighborChanged(Side side)
	{
		_needVisibilityCheck = true;
		if (!_rendering)
		{
			checkVisibility();
		}
		if (side == Side.BOTTOM && !_blockType.isFixed())
		{
			addToUpdateList();
		}
	}

	@Override
	public synchronized void forceVisiblilityCheck()
	{
		_needVisibilityCheck = true;
		if (!_rendering)
		{
			checkVisibility();
		}
	}

	@Override
	public boolean isMoving()
	{
		return _movement != null && _movement.isFalling();
	}

	public byte getFaceMask()
	{
		return _faceMask;
	}

	@Override
	public String toString()
	{
		return _blockType.getName() + " " + _postion.toString();
	}

}
