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

import java.nio.FloatBuffer;

import org.craftmania.Side;
import org.craftmania.datastructures.AABB;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;
import org.craftmania.world.LightBuffer;

public class CrossedBlock extends Block
{

	public CrossedBlock(BlockType type, Chunk chunk, Vec3i pos)
	{
		super(type, chunk, pos);
		addToVisibilityList();
	}

	@Override
	public void update()
	{
	}

	@Override
	public void render(LightBuffer lightBuffer)
	{
		_blockType.getCrossedBlockBrush().setPosition(_postion.x() + 0.5f, _postion.y() + 0.5f, _postion.z() + 0.5f);
		_blockType.getCrossedBlockBrush().render(lightBuffer);
	}
	
	@Override
	public void storeInVBO(FloatBuffer vbo, LightBuffer lightBuffer)
	{
		_blockType.getCrossedBlockBrush().storeInVBO(vbo, _postion.x() + 0.5f, _postion.y() + 0.5f, _postion.z() + 0.5f, lightBuffer);
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public synchronized AABB getAABB()
	{
		if (_aabb == null)
		{
			_aabb = new AABB(new Vec3f(getPosition()).add(DefaultBlock.HALF_BLOCK_SIZE), new Vec3f(DefaultBlock.HALF_BLOCK_SIZE));
		}
		return _aabb;
	}

	@Override
	public void smash(InventoryItem item)
	{
		destroy();
	}

	@Override
	public void neighborChanged(Side side)
	{
	}

	@Override
	public void checkVisibility()
	{
		// TODO: Check if it is surrounded by four non-translucent blocks */
	}

	
	@Override
	public int getVertexCount()
	{
		return 8;
	}
	
}
