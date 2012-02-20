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

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.craftmania.Side;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

/**
 * 
 * @author martijncourteaux
 */
public final class DefaultBlockBrush
{

	private float x, y, z;
	private float hw, hh, hd;
	private Vec2f texturePositions[];
	private int displayList = -1;
	private int displayListBase = -1;
	private Vec3f[] colors;
	private boolean alphaBlending;
	private float[] insets;

	public DefaultBlockBrush()
	{
		this(1, 1, 1);
	}

	public DefaultBlockBrush(float w, float h, float d)
	{
		this.hw = w / 2.0f;
		this.hh = h / 2.0f;
		this.hd = d / 2.0f;
		texturePositions = new Vec2f[6];
		colors = new Vec3f[6];
		insets = new float[6];
		setGlobalColor(new Vec3f(1, 1, 1));
	}

	public Vec2f calcTextureOffsetFor(Side side)
	{
		Vec2f pos = new Vec2f(texturePositions[side.ordinal()]);
		pos.scale(16.0f);
		Texture terrain = TextureStorage.getTexture("terrain");
		pos.x(pos.x() / (float) terrain.getImageWidth());
		pos.y(pos.y() / (float) terrain.getImageHeight());
		return pos;
	}

	public Vec2f getTexturePositionInGridFor(Side side)
	{
		return texturePositions[side.ordinal()];
	}

	public Vec3f getColorFor(Side side)
	{
		return colors[side.ordinal()];
	}

	public void releaseDisplayList()
	{
		glDeleteLists(displayList, 1);
		glDeleteLists(displayListBase, 6);
	}

	public float getD()
	{
		return hd * 2.0f;
	}

	public void setH(float h)
	{
		this.hh = h / 2.0f;
	}

	public float getH()
	{
		return hh * 2.0f;
	}

	public void setD(float d)
	{
		this.hd = d / 2.0f;
	}

	public float getW()
	{
		return hw * 2.0f;
	}

	public void setW(float w)
	{
		this.hw = w / 2.0f;
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public float getZ()
	{
		return z;
	}

	public void setZ(float z)
	{
		this.z = z;
	}

	public void setPosition(float x, float y, float z)
	{
		setX(x);
		setY(y);
		setZ(z);
	}

	public void setSize(float w, float h, float d)
	{
		setW(w);
		setH(h);
		setD(d);
	}

	public void setMantleTopAndBottomTexture(Vec2f mantle, Vec2f top, Vec2f bottom)
	{
		setTextureForMantle(mantle);
		texturePositions[Side.TOP.ordinal()] = top;
		texturePositions[Side.BOTTOM.ordinal()] = bottom;
	}

	public void setTextureForMantle(Vec2f mantle)
	{
		texturePositions[Side.BACK.ordinal()] = mantle;
		texturePositions[Side.FRONT.ordinal()] = mantle;
		texturePositions[Side.LEFT.ordinal()] = mantle;
		texturePositions[Side.RIGHT.ordinal()] = mantle;
	}

	public void setTexture(Vec2f pos)
	{
		for (int i = 0; i < 6; ++i)
		{
			texturePositions[i] = pos;
		}
	}

	public void setInsetForMantle(float inset)
	{
		insets[Side.BACK.ordinal()] = inset;
		insets[Side.FRONT.ordinal()] = inset;
		insets[Side.LEFT.ordinal()] = inset;
		insets[Side.RIGHT.ordinal()] = inset;
	}

	public void setInset(float inset)
	{
		for (int i = 0; i < 6; ++i)
		{
			insets[i] = inset;
		}
	}

	public void setSideInset(Side s, float inset)
	{
		insets[s.ordinal()] = inset;
	}

	public void setTextureForSize(Vec2f pos, Side side)
	{
		texturePositions[side.ordinal()] = pos;
	}

	public void setAlphaBlending(boolean b)
	{
		alphaBlending = true;
	}

	public void renderAt(float x, float y, float z)
	{
		setPosition(x, y, z);
		render();
	}

	public void renderAt(Vec3f pos)
	{
		setPosition(pos.x(), pos.y(), pos.z());
		render();
	}

	public void render()
	{
		if (alphaBlending)
		{
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		} else
		{
			glDisable(GL_BLEND);
		}

		glEnable(GL_TEXTURE_2D);

		glPushMatrix();
		glTranslatef(x, y, z);
		TextureStorage.getTexture("terrain").bind();
		glColor3f(0.3f, 0.3f, 0.3f);
		glCallList(displayList);

		glPopMatrix();
	}

	public void renderFaces(byte faceMask)
	{
		if (alphaBlending)
		{
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		} else
		{
			glDisable(GL_BLEND);
		}

		glEnable(GL_TEXTURE_2D);
		TextureStorage.getTexture("terrain").bind();
		glPushMatrix();
		glTranslatef(x, y, z);
		for (int i = 0, bit = 1; i < 6; ++i, bit <<= 1)
		{
			if ((faceMask & bit) == bit)
			{
				glSetColor(colors[i]);
				glCallList(displayListBase + i);
			}
		}
		glPopMatrix();
	}

	public void generateDisplayListForEachFace()
	{
		displayListBase = glGenLists(6);

		glNewList(displayListBase + Side.TOP.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// TOP
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(-0.5f, 0.5f - insets[Side.TOP.ordinal()], 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(0.5f, 0.5f - insets[Side.TOP.ordinal()], 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(0.5f, 0.5f - insets[Side.TOP.ordinal()], -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, 0.5f - insets[Side.TOP.ordinal()], -0.5f);
		glEnd();
		glEndList();

		glNewList(displayListBase + Side.LEFT.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// LEFT
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f + insets[Side.LEFT.ordinal()], -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f + insets[Side.LEFT.ordinal()], -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f + insets[Side.LEFT.ordinal()], 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f + insets[Side.LEFT.ordinal()], 0.5f, -0.5f);
		glEnd();
		glEndList();

		glNewList(displayListBase + Side.FRONT.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// FRONT
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f - insets[Side.FRONT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f - insets[Side.FRONT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f - insets[Side.FRONT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f - insets[Side.FRONT.ordinal()]);
		glEnd();
		glEndList();

		glNewList(displayListBase + Side.RIGHT.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// RIGHT
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f - insets[Side.RIGHT.ordinal()], 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f - insets[Side.RIGHT.ordinal()], 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f - insets[Side.RIGHT.ordinal()], -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f - insets[Side.RIGHT.ordinal()], -0.5f, -0.5f);
		glEnd();
		glEndList();

		glNewList(displayListBase + Side.BACK.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// BACK
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f + insets[Side.BACK.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f + insets[Side.BACK.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f + insets[Side.BACK.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f + insets[Side.BACK.ordinal()]);
		glEnd();
		glEndList();

		glNewList(displayListBase + Side.BOTTOM.ordinal(), GL_COMPILE);
		glBegin(GL_QUADS);
		// BOTTOM
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x(), calcTextureOffsetFor(Side.BOTTOM).y());
		GL11.glVertex3f(-0.5f, -0.5f + insets[Side.BOTTOM.ordinal()], -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x() + 0.0624f, calcTextureOffsetFor(Side.BOTTOM).y());
		GL11.glVertex3f(0.5f, -0.5f + insets[Side.BOTTOM.ordinal()], -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x() + 0.0624f, calcTextureOffsetFor(Side.BOTTOM).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f + insets[Side.BOTTOM.ordinal()], 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x(), calcTextureOffsetFor(Side.BOTTOM).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f + insets[Side.BOTTOM.ordinal()], 0.5f);
		glEnd();
		glEndList();

	}

	public void generateDisplayList()
	{
		displayList = glGenLists(1);

		glNewList(displayList, GL_COMPILE);
		glBegin(GL_QUADS);

		// TOP
		glSetColor(colors[Side.TOP.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

		// LEFT
		glSetColor(colors[Side.LEFT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

		// BACK
		glSetColor(colors[Side.BACK.ordinal()]);

		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

		// RIGHT
		glSetColor(colors[Side.RIGHT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);

		// FRONT
		glSetColor(colors[Side.FRONT.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

		// BOTTOM
		glSetColor(colors[Side.BOTTOM.ordinal()]);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x(), calcTextureOffsetFor(Side.BOTTOM).y());
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x() + 0.0624f, calcTextureOffsetFor(Side.BOTTOM).y());
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x() + 0.0624f, calcTextureOffsetFor(Side.BOTTOM).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BOTTOM).x(), calcTextureOffsetFor(Side.BOTTOM).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

		GL11.glEnd();

		glEndList();
	}

	public void setSideColor(Side side, Vec3f color)
	{
		this.colors[side.ordinal()] = color;
	}

	private void glSetColor(Vec3f c)
	{
		glColor3f(c.x(), c.y(), c.z());
	}

	public void setGlobalColor(Vec3f c)
	{
		for (int i = 0; i < 6; ++i)
		{
			float factor = 0.76f;
			float intensity = i * 0.04f;
			colors[i] = new Vec3f(c.x() * factor + intensity, c.y() * factor + intensity, c.z() * factor + intensity);
		}
	}

	public void setMantleColor(Vec3f c)
	{
		for (int i = 0; i < 6; ++i)
		{
			if (i == Side.TOP.ordinal() || i == Side.BOTTOM.ordinal())
			{
				continue;
			}
			float factor = 0.76f;
			float intensity = i * 0.04f;
			colors[i] = new Vec3f(c.x() * factor + intensity, c.y() * factor + intensity, c.z() * factor + intensity);
		}
	}

	public void setPosition(Vec3f position)
	{
		x = position.x();
		y = position.y();
		z = position.z();
		
	}

	public float getInset(Side side)
	{
		return insets[side.ordinal()];
	}
}
