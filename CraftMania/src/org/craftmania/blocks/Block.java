package org.craftmania.blocks;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.game.Game;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;

public abstract class Block implements AABBObject
{
	protected BlockType _blockType;
	protected Vec3i _postion;
	protected Chunk _blockChunk;
	protected AABB _aabb;
	protected float _health;
	
	public int _distanceID;
	public float _distance;
	
	/* List facts */
	protected boolean _updating;
	protected boolean _rendering;
	protected boolean _renderManually;
	protected int _specialBlockPoolIndex;

	public Block(BlockType type, Chunk blockChunk, Vec3i pos)
	{
		_postion = pos;
		_blockType = type;
		_blockChunk = blockChunk;
		_health = type.getResistance();
	}
	
	public void setSpecialBlockPoolIndex(int specialBlockPoolIndex)
	{
		this._specialBlockPoolIndex = specialBlockPoolIndex;
	}
	
	public int getSpecialBlockPoolIndex()
	{
		return _specialBlockPoolIndex;
	}
	
	public Vec3i getPosition()
	{
		return _postion;
	}
	
	public BlockType getBlockType()
	{
		return _blockType;
	}
	
	public Chunk getBlockChunk()
	{
		return _blockChunk;
	}
	
	public int getX()
	{
		return _postion.x();
	}
	
	public int getY()
	{
		return _postion.y();
	}
	
	public int getZ()
	{
		return _postion.z();
	}
	
	public boolean isMoving()
	{
		return false;
	}
	
	public boolean inflictDamage(float damage)
	{
		_health -= damage;
		if (_health <= 0)
		{
			destory();
			return true;
		}
		return false;
	}
	
	public void destory()
	{
		Game.getInstance().getWorld().getChunkManager().removeBlock(getX(), getY(), getZ());
		
		_blockChunk.needsNewVBO();
	}
	
	public void removeFromVisibilityList()
	{
		if (_rendering)
		{
			_blockChunk.getVisibleBlocks().bufferRemove(_specialBlockPoolIndex);
			_rendering = false;
//			removeFromManualRenderList();
		}
	}
	
	public synchronized void addToVisibilityList()
	{
		if (!_rendering)
		{
			_blockChunk.getVisibleBlocks().bufferAdd(_specialBlockPoolIndex);
			_rendering = true;
		}
	}
	
	public synchronized void addToUpdateList()
	{
		if (!_updating)
		{
			_blockChunk.getUpdatingBlocks().bufferAdd(_specialBlockPoolIndex);
			_updating = true;
		}
	}
	
	public synchronized void removeFromUpdateList()
	{
		if (_updating)
		{
			_blockChunk.getUpdatingBlocks().bufferRemove(_specialBlockPoolIndex);
			_updating = false;
		}
	}
	
	public synchronized void addToManualRenderList()
	{
		System.out.print("Add to list... ");
//		if (!_renderManually)
		{
			_blockChunk.getManualRenderingBlocks().bufferAdd(_specialBlockPoolIndex);
			_renderManually = true;
			System.out.println("Done");
//		} else
//		{
//			System.out.println("Refused");
		}
	}
	
	public synchronized void removeFromManualRenderList()
	{
		System.out.print("Remove from list... ");
//		if (_renderManually)
		{
			_blockChunk.getManualRenderingBlocks().bufferRemove(_specialBlockPoolIndex);
			_renderManually = false;
			System.out.println("Done");
//		} else
//		{
//			System.out.println("Refused");
		}
	}

	public abstract void update();
	public abstract void render();
	public abstract void forceVisiblilityCheck();
	public abstract boolean isVisible();
	public abstract AABB getAABB();
	public abstract void smash(InventoryItem item);
	public abstract void neighborChanged(Side side);
	public abstract void checkVisibility();

	public void performSpecialAction()
	{
		
	}

	public synchronized void setUpdatingFlag(boolean u)
	{
		_updating = u;
		
	}

	public synchronized void setRenderingFlag(boolean v)
	{
		_rendering = v;
	}

	public void setBlockChunk(Chunk chunk)
	{
		_blockChunk = chunk;	
	}

	public boolean isRenderingManually()
	{
		return _renderManually;
	}


}
