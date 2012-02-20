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
package org.craftmania.blocks.customblocks;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.datastructures.AABB;
import org.craftmania.game.TextureStorage;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class RedFlower extends Block
{

	private static int CALL_LIST;

	public RedFlower(Chunk chunk, Vec3i pos)
	{
		super(BlockManager.getInstance().getBlockType("redflower"), chunk, pos);
		addToManualRenderList();
	}

	@Override
	public void update()
	{
	}

	@Override
	public void render(byte[][][] lightBuffer)
	{
		Texture terrain = TextureStorage.getTexture("terrain");
		terrain.bind();

		float light = lightBuffer[1][1][1] / 29.99f;

		/* Texture 11,10 -> 11,15 */
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		// GL11.glDepthFunc(GL11.GL_ALWAYS);
		GL11.glPushMatrix();
		GL11.glTranslatef(getX() + 0.5f, getY() + 0.5f, getZ() + 0.5f);
		GL11.glColor3f(light, light, light);

		if (CALL_LIST == 0)
		{
			CALL_LIST = GL11.glGenLists(1);

			GL11.glNewList(CALL_LIST, GL11.GL_COMPILE_AND_EXECUTE);
			GL11.glBegin(GL11.GL_QUADS);

			GL11.glTexCoord2f(12f * 16 / terrain.getImageWidth(), 0f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
			GL11.glTexCoord2f(13f * 16 / terrain.getImageWidth(), 0f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(0.5f, 0.5f, 0.5f);
			GL11.glTexCoord2f(13f * 16 / terrain.getImageWidth(), 1f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(0.5f, -0.5f, 0.5f);
			GL11.glTexCoord2f(12f * 16 / terrain.getImageWidth(), 1f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

			GL11.glTexCoord2f(12f * 16 / terrain.getImageWidth(), 0f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(0.5f, 0.5f, -0.5f);
			GL11.glTexCoord2f(13f * 16 / terrain.getImageWidth(), 0f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
			GL11.glTexCoord2f(13f * 16 / terrain.getImageWidth(), 1f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
			GL11.glTexCoord2f(12f * 16 / terrain.getImageWidth(), 1f * 16 / terrain.getImageHeight());
			GL11.glVertex3f(0.5f, -0.5f, -0.5f);
			GL11.glEnd();
			GL11.glEndList();

		} else
		{
			GL11.glCallList(CALL_LIST);
		}
		GL11.glPopMatrix();
		// GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
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
			_aabb = new AABB(new Vec3f(getPosition()).add(DefaultBlock.HALF_BLOCK_SIZE), DefaultBlock.HALF_BLOCK_SIZE);
		}
		return _aabb;
	}

	@Override
	public void smash(InventoryItem item)
	{
		destory();
	}

	@Override
	public void neighborChanged(Side side)
	{
		if (side == Side.BOTTOM)
		{
			if (_chunk.getBlockTypeAbsolute(getX(), getY() - 1, getZ(), false, false, false) <= 0)
			{
				destory();
			}
		}
	}

	@Override
	public void checkVisibility()
	{
	}

}
