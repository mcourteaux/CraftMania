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

import static org.lwjgl.opengl.GL11.*;

import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.craftmania.world.LightBuffer;
import org.newdawn.slick.opengl.Texture;

public final class BlockType extends InventoryItem
{

	private String type;
	private byte id;
	private BlockBrush brush;
	private Texture inventoryTexture;
	private Vec3f dimensions;
	private Vec3f center;
	private boolean solid;
	private boolean fixed;
	private boolean translucent;
	private int resistance;
	private boolean normalAABB;
	private int mineResult;
	private int mineResultCount;
	private String customClass;
	private boolean hasSpecialAction;
	private boolean crossed;
	private byte luminosity;
	private boolean supportNeeded;

	public enum BlockClass
	{

		WOOD, STONE, SAND
	}

	private BlockClass blockClass;
	private boolean hasSpecialSaveData;

	public BlockType(int id, String name)
	{
		super(name, 8.0f);

		this.type = name;
		this.id = (byte) id;
		this.brush = BlockBrushStorage.get(name);
		dimensions = new Vec3f(0.5f, 0.5f, 0.5f);
		center = new Vec3f(0.5f, 0.5f, 0.5f);
		solid = true;
		fixed = true;
		translucent = false;
		normalAABB = true;
		mineResult = -1;
		mineResultCount = 1;
		customClass = null;
		crossed = false;
		luminosity = 0;
	}

	@Override
	public void renderInventoryItem()
	{
		glEnable(GL_TEXTURE_2D);
		float hw = 15f;
		float hh = 15f;

		inventoryTexture.bind();

		glBegin(GL_QUADS);
		glTexCoord2f(0, 0);
		glVertex2f(-hw, hh);
		glTexCoord2f(1, 0);
		glVertex2f(hw, hh);
		glTexCoord2f(1, 1);
		glVertex2f(hw, -hh);
		glTexCoord2f(0, 1);
		glVertex2f(-hw, -hh);
		glEnd();
	}

	@Override
	public float getHealth()
	{
		return 1.0f;
	}

	@Override
	public boolean isStackable()
	{
		return true;
	}

	@Override
	public short getInventoryTypeID()
	{
		return getID();
	}

	public int getMineResult()
	{
		return mineResult;
	}

	public int getMineResultCount()
	{
		return mineResultCount;
	}

	public String getCustomClass()
	{
		return customClass;
	}

	@Override
	public float calcDamageFactorToBlock(byte block)
	{
		return 1.0f;
	}

	@Override
	public float calcDamageInflictedByBlock(byte block)
	{
		return 0.0f;
	}

	@Override
	public void renderHoldableObject(LightBuffer lightBuffer)
	{

		/* Smoothen the light buffer */

//		int avg = 0;
//		for (int i0 = 0; i0 < lightBuffer.length; ++i0)
//		{
//			for (int i1 = 0; i1 < lightBuffer[i0].length; ++i1)
//			{
//				for (int i2 = 0; i2 < lightBuffer[i0][i1].length; ++i2)
//				{
//					avg += lightBuffer[i0][i1][i2];
//				}
//			}
//		}
//		avg /= 27.0f;
//		for (int i0 = 0; i0 < lightBuffer.length; ++i0)
//		{
//			for (int i1 = 0; i1 < lightBuffer[i0].length; ++i1)
//			{
//				for (int i2 = 0; i2 < lightBuffer[i0][i1].length; ++i2)
//				{
//					lightBuffer[i0][i1][i2] = (byte) avg;
//				}
//			}
//		}

		float scale = 0.1f;
		glScalef(scale, scale, scale);
		glRotatef(-40, 0, 0, 1);
		brush.setPosition(0, 0, 0);
		brush.render(lightBuffer);
	}

	@Override
	public void update()
	{
		// Do Nothing
	}

	@Override
	public void inflictDamage(float toolDamage)
	{
		// Do nothing, blocks can't be damaged
	}

	public boolean hasNormalAABB()
	{
		return normalAABB;
	}

	public BlockBrush getBrush()
	{
		return brush;
	}

	public Vec3f getDimensions()
	{
		return dimensions;
	}
	
	public Vec3f getCenter()
	{
		return center;
	}

	public boolean isFixed()
	{
		return fixed;
	}

	public byte getID()
	{
		return id;
	}

	public int getResistance()
	{
		return resistance;
	}

	public boolean isSolid()
	{
		return solid;
	}

	public boolean isTranslucent()
	{
		return translucent;
	}

	public String getType()
	{
		return type;
	}

	public BlockClass getBlockClass()
	{
		return blockClass;
	}

	public Texture getInventoryTexture()
	{
		return inventoryTexture;
	}

	public void setInventoryTexture(Texture inventoryTexture)
	{
		this.inventoryTexture = inventoryTexture;
	}

	public boolean wantsToBeUpdated()
	{
		return (!fixed) || (customClass != null);
	}

	public boolean hasSpecialAction()
	{
		return hasSpecialAction;
	}

	public boolean hasSpecialSaveData()
	{
		return hasSpecialSaveData;
	}

	public byte getLuminosity()
	{
		return luminosity;
	}

	public DefaultBlockBrush getDefaultBlockBrush()
	{
		return (DefaultBlockBrush) brush;
	}

	public CrossedBlockBrush getCrossedBlockBrush()
	{
		return (CrossedBlockBrush) brush;
	}

	public boolean isCrossed()
	{
		return crossed;
	}

	public boolean isSupportNeeded()
	{
		return supportNeeded;
	}

}
