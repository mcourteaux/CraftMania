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

import java.nio.FloatBuffer;

import org.craftmania.game.TextureStorage;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.craftmania.world.LightBuffer;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class CrossedBlockBrush extends BlockBrush
{
	private Vec2f _texturePosition;
	private Vec2f _textureSize;
	private Vec3f _position;
	private int _callList;
	private Texture _terrain;

	public CrossedBlockBrush()
	{
		_texturePosition = new Vec2f();
		_textureSize = new Vec2f();
		_position = new Vec3f();
		_terrain = TextureStorage.getTexture("terrain");
	}

	@Override
	public void setPosition(float x, float y, float z)
	{
		_position.set(x, y, z);
	}

	public void setTexturePosition(int x, int y)
	{
		_texturePosition.set(x * 16f / _terrain.getImageWidth(), y * 16f / _terrain.getImageHeight());
		_textureSize.set(1.0f / 16.0f, 1.0f / 16.0f);
	}

	@Override
	public void render(LightBuffer lightBuffer)
	{
		if (_callList != 0)
		{
			_terrain.bind();

			float light = lightBuffer.get(1, 1, 1) / 30.001f;

			/* Texture 11,10 -> 11,15 */
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			// GL11.glDepthFunc(GL11.GL_ALWAYS);
			GL11.glPushMatrix();
			GL11.glTranslatef(_position.x() + 0.5f, _position.y() + 0.5f, _position.z() + 0.5f);
			GL11.glColor3f(light, light, light);

			GL11.glCallList(_callList);

			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}

	@Override
	public void create()
	{
		_callList = GL11.glGenLists(1);

		GL11.glNewList(_callList, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glTexCoord2f(_texturePosition.x(), _texturePosition.y());
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(_texturePosition.x() + _textureSize.x(), _texturePosition.y());
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(_texturePosition.x() + _textureSize.x(), _texturePosition.y() + _textureSize.y());
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(_texturePosition.x(), _texturePosition.y() + _textureSize.y());
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

		GL11.glTexCoord2f(_texturePosition.x(), _texturePosition.y());
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord2f(_texturePosition.x() + _textureSize.x(), _texturePosition.y());
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glTexCoord2f(_texturePosition.x() + _textureSize.x(), _texturePosition.y() + _textureSize.y());
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glTexCoord2f(_texturePosition.x(), _texturePosition.y() + _textureSize.y());
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glEnd();
		GL11.glEndList();
	}

	@Override
	public void release()
	{
		GL11.glDeleteLists(_callList, 1);
	}
	
	@Override
	public int getVertexCount()
	{
		return 8;
	}

	@Override
	public void storeInVBO(FloatBuffer vbo, float x, float y, float z, LightBuffer lightBuffer)
	{
		byte light = lightBuffer.get(1, 1, 1);

		/* Blade 0 */
		put3f(vbo, x - 0.5f, y + 0.5f, z - 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x(), _texturePosition.y());

		put3f(vbo, x + 0.5f, y + 0.5f, z + 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x() + _textureSize.x(), _texturePosition.y());

		put3f(vbo, x + 0.5f, y - 0.5f, z + 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x() + _textureSize.x(), _texturePosition.y() + _textureSize.y());

		put3f(vbo, x - 0.5f, y - 0.5f, z - 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x(), _texturePosition.y() + _textureSize.y());

		/* Blade 1 */
		put3f(vbo, x + 0.5f, y + 0.5f, z - 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x(), _texturePosition.y());

		put3f(vbo, x - 0.5f, y + 0.5f, z + 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x() + _textureSize.x(), _texturePosition.y());

		put3f(vbo, x - 0.5f, y - 0.5f, z + 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x() + _textureSize.x(), _texturePosition.y() + _textureSize.y());

		put3f(vbo, x + 0.5f, y - 0.5f, z - 0.5f);
		putColorWithLight(vbo, COLOR_WHITE, light);
		put2f(vbo, _texturePosition.x(), _texturePosition.y() + _textureSize.y());

	}

	public Vec2f getTexturePosition()
	{
		return _texturePosition;
	}

}
