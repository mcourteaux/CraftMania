package org.craftmania.world;

import org.craftmania.GameObject;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.lwjgl.opengl.GL11;

public class Sky extends GameObject
{

	private Vec3f innerColor;
	private Vec3f outerColor;
	private float height;
	private float radius;
	private float bend;
	private int vertices;

	public Sky()
	{
		innerColor = new Vec3f(0.5f, 0.7f, 1.0f);
		outerColor = innerColor;
		height = 128.0f;
		radius = Game.getInstance().getConfiguration().getViewingDistance() + 20.0f;
		bend = 15.0f;
		vertices = 32;
	}

	@Override
	public void update()
	{

	}

	@Override
	public void render()
	{
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		Vec3f pos = Game.getInstance().getWorld().getPlayer().getPosition();


		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glColor3f(innerColor.x(), innerColor.y(), innerColor.z());
		GL11.glVertex3f(pos.x(), height, pos.z());

		GL11.glColor3f(outerColor.x(), outerColor.y(), outerColor.z());

		for (int i = 0; i <= vertices; ++i)
		{
			float angle = MathHelper.f_2PI / vertices * i;
			float x = MathHelper.cos(angle) * radius;
			float z = MathHelper.sin(angle) * radius;
			GL11.glVertex3f(x + pos.x(), height - bend, z + pos.z());
		}
		GL11.glEnd();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

}
