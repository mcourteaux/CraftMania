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
package org.craftmania.world;

import org.craftmania.GameObject;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class Sky extends GameObject
{

	/* Sphere */
	private static Vec3f COLOR = new Vec3f(0.5f, 0.7f, 1.0f);
	private Vec3f _color;
	private float _height;
	private float _radius;
	private float _bend;
	private int _vertices;
	private int _sphereCallList;

	/* Clouds */
	private Texture _clouds;
	private float _cloudsX;
	private float _cloudsZ;
	private float _cloudsHeight;
	private float _cloudsAlpha;
	private float _cloudsScale;
	private float _cloudsTexWidth;
	private float _cloudsTexHeight;
	private int _cloudsCallList;

	public Sky()
	{
		_color = new Vec3f(COLOR);
		_height = 128.0f;
		_radius = Game.getInstance().getConfiguration().getViewingDistance() * 4.0f;
		_bend = 15.0f;
		_vertices = 32;
		_clouds = TextureStorage.getTexture("environment.clouds");
		_cloudsTexWidth = _clouds.getImageWidth();
		_cloudsTexHeight = _clouds.getImageHeight();
		_cloudsHeight = 128.0f;
		_cloudsAlpha = 0.8f;
		_cloudsScale = 2.0f;
	}

	@Override
	public void update()
	{
		float step = Game.getInstance().getStep();
		_cloudsX += step * 1.0f;
		_cloudsZ += step * 0.6f;

		_cloudsX = MathHelper.simplify(_cloudsX, _cloudsTexWidth * _cloudsScale);
		_cloudsZ = MathHelper.simplify(_cloudsZ, _cloudsTexHeight * _cloudsScale);

		_color.set(COLOR);
		_color.scale(Game.getInstance().getWorld().getSunlight() - 0.15f);
	}

	@Override
	public void render()
	{
		Vec3f playerPos = Game.getInstance().getWorld().getActivePlayer().getPosition();

		GL11.glDisable(GL11.GL_CULL_FACE);

		/* Sphere */
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		drawShpere(playerPos.x(), Math.max(_cloudsHeight + _bend + 10.0f, playerPos.y() + _height), playerPos.z());
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		/* Clouds */
		GL11.glEnable(GL11.GL_BLEND);
		_clouds.bind();

		int playerCloudGridX = MathHelper.round(playerPos.x() / (_cloudsTexWidth * _cloudsScale));
		int playerCloudGridZ = MathHelper.round(playerPos.z() / (_cloudsHeight * _cloudsScale));

		GL11.glColor4f(1.0f, 1.0f, 1.0f, _cloudsAlpha);

		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
			{
				drawClouds((x + playerCloudGridX) * _cloudsTexWidth * _cloudsScale + _cloudsX, _cloudsHeight, (z + playerCloudGridZ) * _cloudsTexHeight * _cloudsScale + _cloudsZ);
			}
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawShpere(float x, float y, float z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		GL11.glColor3f(_color.x(), _color.y(), _color.z());
		if (_sphereCallList == 0)
		{
			_sphereCallList = GL11.glGenLists(1);
			GL11.glNewList(_sphereCallList, GL11.GL_COMPILE_AND_EXECUTE);
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex3f(0, 0, 0);
			for (int i = 0; i <= _vertices; ++i)
			{
				float angle = MathHelper.f_2PI / _vertices * i;
				float xx = MathHelper.cos(angle) * _radius;
				float zz = MathHelper.sin(angle) * _radius;
				GL11.glVertex3f(xx, -_bend, zz);
			}
			GL11.glEnd();
			GL11.glEndList();
		} else
		{
			GL11.glCallList(_sphereCallList);
		}
		GL11.glPopMatrix();
	}

	private void drawClouds(float x, float y, float z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		if (_cloudsCallList == 0)
		{
			_cloudsCallList = GL11.glGenLists(1);
			GL11.glNewList(_cloudsCallList, GL11.GL_COMPILE_AND_EXECUTE);
			float hw = _cloudsTexWidth / 2.0f;
			float hh = _cloudsTexHeight / 2.0f;

			hw *= _cloudsScale;
			hh *= _cloudsScale;

			GL11.glBegin(GL11.GL_QUADS);

			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(-hw, 0, -hh);

			GL11.glTexCoord2f(1, 0);
			GL11.glVertex3f(+hw, 0, -hh);

			GL11.glTexCoord2f(1, 1);
			GL11.glVertex3f(+hw, 0, +hh);

			GL11.glTexCoord2f(0, 1);
			GL11.glVertex3f(-hw, 0, +hh);

			GL11.glEnd();

			GL11.glEndList();
		} else
		{
			GL11.glCallList(_cloudsCallList);
		}
		GL11.glPopMatrix();
	}

	public float getCloudsHeight()
	{
		return _cloudsHeight;
	}

}
