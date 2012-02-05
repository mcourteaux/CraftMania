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
	private Vec3f _innerColor;
	private Vec3f _outerColor;
	private float _height;
	private float _radius;
	private float _bend;
	private int _vertices;
	
	/* Clouds */
	private Texture _clouds;
	private float _cloudsX;
	private float _cloudsZ;
	private float _cloudsHeight;
	private float _cloudsAlpha;
	private float _cloudsScale;
	private float _cloudsTexWidth;
	private float _cloudsTexHeight;

	public Sky()
	{
		_innerColor = new Vec3f(0.5f, 0.7f, 1.0f);
		_outerColor = _innerColor;
		_height = 160.0f;
		_radius = Game.getInstance().getConfiguration().getViewingDistance() + 20.0f;
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
	}

	@Override
	public void render()
	{
		Vec3f playerPos = Game.getInstance().getWorld().getPlayer().getPosition();
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		/* Sphere */
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glColor3f(_innerColor.x(), _innerColor.y(), _innerColor.z());
		GL11.glVertex3f(playerPos.x(), _height, playerPos.z());
		GL11.glColor3f(_outerColor.x(), _outerColor.y(), _outerColor.z());
		for (int i = 0; i <= _vertices; ++i)
		{
			float angle = MathHelper.f_2PI / _vertices * i;
			float x = MathHelper.cos(angle) * _radius;
			float z = MathHelper.sin(angle) * _radius;
			GL11.glVertex3f(x + playerPos.x(), _height - _bend, z + playerPos.z());
		}
		GL11.glEnd();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		/* Clouds */
		GL11.glEnable(GL11.GL_BLEND);
		
		_clouds.bind();
		
		int playerCloudGridX = MathHelper.floorDivision(MathHelper.floor(playerPos.x()), MathHelper.floor(_cloudsTexWidth * _cloudsScale));
		int playerCloudGridZ = MathHelper.floorDivision(MathHelper.floor(playerPos.z()), MathHelper.floor(_cloudsHeight * _cloudsScale));
		
		GL11.glColor4f(1.0f, 1.0f, 1.0f, _cloudsAlpha);
		
		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
			{
				drawClouds((x + playerCloudGridX) * _cloudsTexWidth * _cloudsScale + _cloudsX, _cloudsHeight, (z + playerCloudGridZ) * _cloudsTexHeight * _cloudsScale + _cloudsZ);
			}
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void drawClouds(float x, float y, float z)
	{
		float hw = _cloudsTexWidth / 2.0f;
		float hh = _cloudsTexHeight / 2.0f;
		
		hw *= _cloudsScale;
		hh *= _cloudsScale;
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(x - hw, y, z - hh);
		
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(x + hw, y, z - hh);
		
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(x + hw, y, z + hh);
		
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(x - hw, y, z + hh);
		
		GL11.glEnd();
	}

}
