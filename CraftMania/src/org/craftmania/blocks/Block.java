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
package org.craftmania.blocks;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.AABBObject;
import org.craftmania.game.Game;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;
import org.craftmania.world.ChunkData;

public abstract class Block implements AABBObject
{
	protected BlockType _blockType;
	protected Vec3i _postion;
	protected Chunk _chunk;
	protected AABB _aabb;
	protected float _health;
	
	public int _distanceID;
	public float _distance;
	
	/* List facts */
	protected boolean _updating;
	protected boolean _rendering;
	protected boolean _renderManually;
	protected int _specialBlockPoolIndex;

	public Block(BlockType type, Chunk chunk, Vec3i pos)
	{
		_postion = pos;
		_blockType = type;
		_chunk = chunk;
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
		return _chunk;
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
	
	public int getChunkDataIndex()
	{
		return ChunkData.positionToIndex(getX() - _chunk.getAbsoluteX(), getY(), getZ() - _chunk.getAbsoluteZ());
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
		
		_chunk.needsNewVBO();
	}
	
	public void removeFromVisibilityList()
	{
		if (_rendering)
		{
			_chunk.getVisibleBlocks().bufferRemove(getChunkDataIndex());
			_rendering = false;
			removeFromManualRenderList();
		}
	}
	
	public synchronized void addToVisibilityList()
	{
		if (!_rendering)
		{
			_chunk.getVisibleBlocks().bufferAdd(getChunkDataIndex());
			_rendering = true;
		}
	}
	
	public synchronized void addToUpdateList()
	{
		if (!_updating)
		{
			_chunk.getUpdatingBlocks().bufferAdd(getChunkDataIndex());
			_updating = true;
		}
	}
	
	public synchronized void removeFromUpdateList()
	{
		if (_updating)
		{
			_chunk.getUpdatingBlocks().bufferRemove(getChunkDataIndex());
			_updating = false;
		}
	}
	
	public synchronized void addToManualRenderList()
	{
		System.out.print("Add to list (" + getChunkDataIndex() + ") ... ");
//		if (!_renderManually)
		{
			_chunk.getManualRenderingBlocks().bufferAdd(getChunkDataIndex());
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
			_chunk.getManualRenderingBlocks().bufferRemove(getChunkDataIndex());
			_renderManually = false;
			System.out.println("Done");
//		} else
//		{
//			System.out.println("Refused");
		}
	}

	public abstract void update();
	public abstract void render(byte[][][] lightBuffer);
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

	public void setChunk(Chunk chunk)
	{
		_chunk = chunk;	
	}

	public boolean isRenderingManually()
	{
		return _renderManually;
	}


}
