package org.craftmania.blocks;

import java.util.Iterator;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.game.Game;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.world.BlockChunk;

public class DefaultBlock extends Block
{
	private static final Vec3f HALF_BLOCK_SIZE = new Vec3f(0.5f, 0.5f, 0.5f);
	@SuppressWarnings("unused")
	private static final Vec3f BLOCK_SIZE = new Vec3f(1.0f, 1.0f, 1.0f);

	public static final byte ALL_FACES = 0x3f; // 0011 1111

	private boolean _needVisibilityCheck;
	private byte _faceMask;

	private BlockMovementPlugin _movement;

	public DefaultBlock(BlockType type, BlockChunk chunk, Vec3i pos)
	{
		super(type, chunk, pos);
		// _faceMask = ALL_FACES;
		setFaceVisible(Side.TOP, true);
		setFaceVisible(Side.BOTTOM, true);
		_aabb = new AABB(new Vec3f(pos).add(HALF_BLOCK_SIZE), HALF_BLOCK_SIZE);
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
	public void update(Iterator<Block> updateIterator)
	{
		if (!_blockType.isFixed())
		{
			Block supportingBlock = _blockChunk.getBlockAbsolute(getX(), getY() - 1, getZ());
			if (supportingBlock == null)
			{
				if (!isFalling())
				{
					/* Start falling */
					if (!hasMovementPlugin())
						createMovementPlugin();
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
				/* Destroy the plugin */
				_movement = null;
			}
		} else
		{
			/* Remove this block from the update list */
			updateIterator.remove();
			_updating = false;
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
		return _faceMask != 0;
	}

	private synchronized void checkVisibility()
	{
		boolean preVisibility = _faceMask != 0;
		if (isMoving())
		{
			_faceMask = ALL_FACES;
		} else
		{
			if (_blockChunk != null)
			{
				for (int i = 0; i < 6; ++i)
				{
					Side side = Side.values()[i];
					Vec3i normal = side.getNormal();
					Block block = _blockChunk.getBlockAbsolute(getX() + normal.x(), getY() + normal.y(), getZ() + normal.z());
					if (block != null)
					{
						if (block.isMoving() || !block.getBlockType().hasNormalAABB())
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
			/* Make the bottom layer invisible */
			if (getY() == 0)
			{
				setFaceVisible(Side.BOTTOM, false);
			}
		}
		_needVisibilityCheck = false;

		boolean newVisibility = _faceMask != 0;

		if (newVisibility != preVisibility)
		{
			if (newVisibility)
			{
				addToVisibilityList();
			} else
			{
				removeFromVisibilityList();
			}
		}
	}

	@Override
	public AABB getAABB()
	{
		return _aabb;
	}

	@Override
	public boolean smash(InventoryItem item)
	{
		float damage;
		if (item == null)
		{
			damage = 0.9f;
		} else
		{
			damage = item.calcDamageFactorToBlock(this);
		}
		damage *= 9.0f;

		return inflictDamage(damage * Game.getInstance().getStep());
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
//			System.out.println("Support changed for " + _blockType.getName() + ", add to update list");
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

}
