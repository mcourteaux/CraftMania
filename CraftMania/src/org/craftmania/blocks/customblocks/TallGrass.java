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

public class TallGrass extends Block
{

	private static int CALL_LIST_BASE;
	private static int LENGHT_COUNT = 6;
	private int _length;

	public TallGrass(Chunk chunk, Vec3i pos, int length)
	{
		super(BlockManager.getInstance().getBlockType("tallgrass"), chunk, pos);
		_length = length;
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
		
		if (CALL_LIST_BASE == 0)
		{
			CALL_LIST_BASE = GL11.glGenLists(LENGHT_COUNT);

			for (int i = 0; i < LENGHT_COUNT; ++i)
			{
				GL11.glNewList(CALL_LIST_BASE + i, i == _length ? GL11.GL_COMPILE_AND_EXECUTE : GL11.GL_COMPILE);
				GL11.glBegin(GL11.GL_QUADS);

				GL11.glTexCoord2f((9f + i) * 16 / terrain.getImageWidth(), 11f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
				GL11.glTexCoord2f((10f + i) * 16 / terrain.getImageWidth(), 11f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(0.5f, 0.5f, 0.5f);
				GL11.glTexCoord2f((10f + i) * 16 / terrain.getImageWidth(), 12f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(0.5f, -0.5f, 0.5f);
				GL11.glTexCoord2f((9f + i) * 16 / terrain.getImageWidth(), 12f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

				GL11.glTexCoord2f((9f + i) * 16 / terrain.getImageWidth(), 11f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(0.5f, 0.5f, -0.5f);
				GL11.glTexCoord2f((10f + i) * 16 / terrain.getImageWidth(), 11f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
				GL11.glTexCoord2f((10f + i) * 16 / terrain.getImageWidth(), 12f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
				GL11.glTexCoord2f((9f + i) * 16 / terrain.getImageWidth(), 12f * 16 / terrain.getImageHeight());
				GL11.glVertex3f(0.5f, -0.5f, -0.5f);

				GL11.glEnd();
				GL11.glEndList();
			}
		} else
		{
			GL11.glCallList(CALL_LIST_BASE + _length);
		}
		GL11.glPopMatrix();
		// GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);

	}

	@Override
	public void forceVisiblilityCheck()
	{

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
