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

import static org.craftmania.rendering.ChunkMeshBuilder.*;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

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
public final class DefaultBlockBrush extends BlockBrush
{

	private float x, y, z;
	private float hw, hh, hd;
	private Vec2f texturePositions[];
	private int displayList = -1;
	private int displayListBase = -1;
	private Vec3f[] colors;
	private boolean alphaBlending;
	private float[] insets;
	private byte faceMask;

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

	@Override
	public void release()
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

	public void renderAt(float x, float y, float z, byte[][][] lightBuffer)
	{
		setPosition(x, y, z);
		render(lightBuffer);
	}

	public void renderAt(Vec3f pos, byte[][][] lightBuffer)
	{
		setPosition(pos.x(), pos.y(), pos.z());
		render(lightBuffer);
	}

	public void render(byte[][][] lightBuffer)
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

		renderFaces((byte) 0x3F, lightBuffer);
	}

	public void renderFaces(byte faceMask, byte[][][] lightBuffer)
	{
		if (alphaBlending)
		{
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		} else
		{
			glDisable(GL_BLEND);
		}

		float light = lightBuffer[1][1][1] / 30.0001f;
		
		glEnable(GL_TEXTURE_2D);
		TextureStorage.getTexture("terrain").bind();
		glPushMatrix();
		glTranslatef(x, y, z);
		for (int i = 0, bit = 1; i < 6; ++i, bit <<= 1)
		{
			if ((faceMask & bit) == bit)
			{
				glSetColor(colors[i], light);
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

	/**
	 * This display lists draws the whole cube at once. This isn't used anywhere. I guess I will delete this code soon.
	 */
	public void generateDisplayList()
	{
		displayList = glGenLists(1);

		glNewList(displayList, GL_COMPILE);
		glBegin(GL_QUADS);

		// TOP
		glSetColor(colors[Side.TOP.ordinal()], 1.0f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x() + 0.0624f, calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.TOP).x(), calcTextureOffsetFor(Side.TOP).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

		// LEFT
		glSetColor(colors[Side.LEFT.ordinal()], 1.0f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x() + 0.0624f, calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.LEFT).x(), calcTextureOffsetFor(Side.LEFT).y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

		// BACK
		glSetColor(colors[Side.BACK.ordinal()], 1.0f);

		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x() + 0.0624f, calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.BACK).x(), calcTextureOffsetFor(Side.BACK).y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

		// RIGHT
		glSetColor(colors[Side.RIGHT.ordinal()], 1.0f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x() + 0.0624f, calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.RIGHT).x(), calcTextureOffsetFor(Side.RIGHT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);

		// FRONT
		glSetColor(colors[Side.FRONT.ordinal()], 1.0f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x() + 0.0624f, calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glTexCoord2f(calcTextureOffsetFor(Side.FRONT).x(), calcTextureOffsetFor(Side.FRONT).y() + 0.0624f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

		// BOTTOM
		glSetColor(colors[Side.BOTTOM.ordinal()], 1.0f);
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

	private void glSetColor(Vec3f c, float light)
	{
		glColor3f(c.x() * light, c.y() * light, c.z() * light);
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
	
	@Override
	public void create()
	{
		generateDisplayListForEachFace();
		generateDisplayList();
	}
	
	@Override
	@Deprecated
	public int getVertexCount()
	{
		return 24;
	}
	
	public void setFaceMask(byte faceMask)
	{
		this.faceMask = faceMask;
	}
	
	@Override
	public void storeInVBO(FloatBuffer vertexBuffer, float x, float y, float z, byte[][][] lightBuffer)
	{
		float tileSize = 0.0624f;
		for (int i = 0, bit = 1; i < 6; ++i, bit <<= 1)
		{
			if ((bit & faceMask) == bit)
			{
				Side side = Side.values()[i];
				Vec3f color = getColorFor(side);
				Vec2f uv = calcTextureOffsetFor(side);
				float inset = getInset(side);

				if (side == Side.TOP)
				{
					put3f(vertexBuffer, x - 0.5f, y + 0.5f - inset, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[0][2][1], lightBuffer[0][2][2], lightBuffer[1][2][2]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, x + 0.5f, y + 0.5f - inset, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[2][2][1], lightBuffer[1][2][2], lightBuffer[2][2][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x + 0.5f, y + 0.5f - inset, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[1][2][0], lightBuffer[2][2][0], lightBuffer[2][2][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x - 0.5f, y + 0.5f - inset, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[0][2][1], lightBuffer[1][2][0], lightBuffer[0][2][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.LEFT)
				{
					put3f(vertexBuffer, x - 0.5f + inset, y - 0.5f, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][0][1], lightBuffer[0][1][0], lightBuffer[0][0][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, x - 0.5f + inset, y - 0.5f, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][0][1], lightBuffer[0][1][2], lightBuffer[0][0][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x - 0.5f + inset, y + 0.5f, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][1][2], lightBuffer[0][2][1], lightBuffer[0][2][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x - 0.5f + inset, y + 0.5f, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][1][0], lightBuffer[0][2][0], lightBuffer[0][2][1]);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.FRONT)
				{
					put3f(vertexBuffer, x - 0.5f, y - 0.5f, z + 0.5f - inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[0][0][2], lightBuffer[0][1][2], lightBuffer[1][0][2]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, x + 0.5f, y - 0.5f, z + 0.5f - inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][0][2], lightBuffer[2][1][2], lightBuffer[2][0][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x + 0.5f, y + 0.5f, z + 0.5f - inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][2][2], lightBuffer[2][2][2], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x - 0.5f, y + 0.5f, z + 0.5f - inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][2][2], lightBuffer[0][1][2], lightBuffer[0][2][2]);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.RIGHT)
				{
					put3f(vertexBuffer, x + 0.5f - inset, y + 0.5f, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][2][1], lightBuffer[2][1][0], lightBuffer[2][2][0]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, x + 0.5f - inset, y + 0.5f, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][2][2], lightBuffer[2][2][1], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x + 0.5f - inset, y - 0.5f, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][0][2], lightBuffer[2][0][1], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x + 0.5f - inset, y - 0.5f, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][0][0], lightBuffer[2][0][1], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BACK)
				{
					put3f(vertexBuffer, x - 0.5f, y + 0.5f, z - 0.5f + inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][2][0], lightBuffer[0][2][0], lightBuffer[0][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, x + 0.5f, y + 0.5f, z - 0.5f + inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][2][0], lightBuffer[2][2][0], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x + 0.5f, y - 0.5f, z - 0.5f + inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][0][0], lightBuffer[2][0][0], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x - 0.5f, y - 0.5f, z - 0.5f + inset);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][0][0], lightBuffer[0][0][0], lightBuffer[0][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BOTTOM)
				{
					put3f(vertexBuffer, x - 0.5f, y - 0.5f + inset, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][0], lightBuffer[0][0][0], lightBuffer[0][0][1]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, x + 0.5f, y - 0.5f + inset, z - 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][0], lightBuffer[2][0][0], lightBuffer[2][0][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, x + 0.5f, y - 0.5f + inset, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][2], lightBuffer[2][0][2], lightBuffer[2][0][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, x - 0.5f, y - 0.5f + inset, z + 0.5f);
					putColorWithLight4(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][2], lightBuffer[0][0][2], lightBuffer[0][0][1]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				}
			}
		}
	}
}
