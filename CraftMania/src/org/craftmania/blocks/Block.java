package org.craftmania.blocks;

import java.util.Iterator;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.game.Game;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3i;
import org.craftmania.world.BlockChunk;

public abstract class Block implements AABBObject
{
	protected BlockType _blockType;
	protected Vec3i _postion;
	protected BlockChunk _blockChunk;
	protected AABB _aabb;
	protected float _health;
	
	public int _distanceID;
	public float _distance;
	
	/* List facts */
	protected boolean _updating;
	protected boolean _rendering;
	protected boolean _renderManually;

	public Block(BlockType type, BlockChunk blockChunk, Vec3i pos)
	{
		_postion = pos;
		_blockType = type;
		_blockChunk = blockChunk;
		_health = type.getResistance();
	}
	
	public Vec3i getPosition()
	{
		return _postion;
	}
	
	public BlockType getBlockType()
	{
		return _blockType;
	}
	
	public BlockChunk getBlockChunk()
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
		Game.getInstance().getWorld().getChunkManager().destroyBlock(this);
		
		_blockChunk.needsNewVBO();
	}
	
	public void removeFromVisibilityList()
	{
		if (_rendering)
		{
			_blockChunk.getVisibleBlocks().rememberToRemoveBlock(this);
			_rendering = false;
//			removeFromManualRenderList();
		}
	}
	
	public synchronized void addToVisibilityList()
	{
		if (!_rendering)
		{
			_blockChunk.getVisibleBlocks().rememberToAddBlock(this);
			_rendering = true;
		}
	}
	
	public synchronized void addToUpdateList()
	{
		if (!_updating)
		{
			_blockChunk.getUpdatingBlocks().rememberToAddBlock(this);
			_updating = true;
		}
	}
	
	public synchronized void addToManualRenderList()
	{
		System.out.print("Add to list... ");
//		if (!_renderManually)
		{
			_blockChunk.getManualRenderBlocks().rememberToAddBlock(this);
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
			_blockChunk.getManualRenderBlocks().rememberToRemoveBlock(this);
			_renderManually = false;
			System.out.println("Done");
//		} else
//		{
//			System.out.println("Refused");
		}
	}

	public abstract void update(Iterator<Block> updatingIterator);
	public abstract void render();
	public abstract void forceVisiblilityCheck();
	public abstract boolean isVisible();
	public abstract AABB getAABB();
	public abstract boolean smash(InventoryItem item);
	public abstract void neighborChanged(Side side);
	
	public boolean hasSpecialAction()
	{
		return false;
	}
	
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

	public void setBlockChunk(BlockChunk chunk)
	{
		_blockChunk = chunk;	
	}

	public boolean isRenderingManually()
	{
		return _renderManually;
	}

}
