package org.craftmania.inventory;

import org.craftmania.game.Configuration;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.Vec2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;
import org.lwjgl.util.ReadablePoint;
import org.lwjgl.util.Rectangle;
import org.newdawn.slick.opengl.Texture;

public class DoubleContainerInventory extends Inventory
{
	
	/* STATIC OPENGL SHIT */
	private static final int _inventoryDrawList;
	private static final Vec2f _texPosUpLeft;
	private static final Vec2f _texPosDownRight;
	private static final Texture _tex;
	private static final int _hw = 176;
	private static final int _hh = 227;

	static
	{
		_texPosUpLeft = new Vec2f(0, 0);
		_texPosDownRight = new Vec2f(_texPosUpLeft.x() + _hw, _texPosUpLeft.y() + _hh);

		_tex = TextureStorage.getTexture("gui.container");
		_texPosUpLeft.x(_texPosUpLeft.x() / _tex.getTextureWidth());
		_texPosUpLeft.y(_texPosUpLeft.y() / _tex.getTextureHeight());
		_texPosDownRight.x(_texPosDownRight.x() / _tex.getTextureWidth());
		_texPosDownRight.y(_texPosDownRight.y() / _tex.getTextureHeight());

		
		_inventoryDrawList = GL11.glGenLists(1);
		GL11.glNewList(_inventoryDrawList, GL11.GL_COMPILE);

		GL11.glColor3f(1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(_texPosUpLeft.x(), _texPosUpLeft.y());
		GL11.glVertex2f(-_hw, _hh);
		GL11.glTexCoord2f(_texPosDownRight.x(), _texPosUpLeft.y());
		GL11.glVertex2f(_hw, _hh);
		GL11.glTexCoord2f(_texPosDownRight.x(), _texPosDownRight.y());
		GL11.glVertex2f(_hw, -_hh);
		GL11.glTexCoord2f(_texPosUpLeft.x(), _texPosDownRight.y());
		GL11.glVertex2f(-_hw, -_hh);
		GL11.glEnd();

		GL11.glEndList();


	}
	
	public static void UNLOAD_STATIC_CONTENT()
	{
		GL11.glDeleteLists(_inventoryDrawList, 1);
	}
	
	public DoubleContainerInventory()
	{
		super(90);
		_raster = new DoubleContainerInventoryRaster();
	}


	@Override
	public void renderInventory()
	{
		if (_inventoryDrawList == -1)
		{
			return;
		}
		
		Configuration conf = Game.getInstance().getConfiguration();

		GL11.glPushMatrix();
		GL11.glTranslatef(conf.getWidth() / 2.0f, conf.getHeight() / 2.0f, 0.0f);

		_tex.bind();

		GL11.glCallList(_inventoryDrawList);
		GL11.glPopMatrix();

		for (int i = 0; i < _raster.getCellCount(); ++i)
		{
			GL11.glPushMatrix();
			ReadablePoint r = _raster.getCenterOfCell(i);
			GL11.glTranslatef(r.getX(), r.getY(), 0);
			if (Mouse.getX() < r.getX() + 16 && Mouse.getX() > r.getX() - 16 && Mouse.getY() < r.getY() + 16 && Mouse.getY() > r.getY() - 16)
			{
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.2f);
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2i(-16, -16);
				GL11.glVertex2i(+16, -16);
				GL11.glVertex2i(+16, +16);
				GL11.glVertex2i(-16, +16);
				GL11.glEnd();
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

			}

			InventoryPlace place = getInventoryPlace(i);
			if (place != null)
			{
				place.render();
			}
			GL11.glPopMatrix();
		}

		/* Draggin item */
		if (_dragging && _draggingItem != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(Mouse.getX(), Mouse.getY(), 0);
			_draggingItem.render();
			GL11.glPopMatrix();
		}
	}


	@Override
	protected void inventoryEvent(InventoryEvent evt)
	{
		Game.getInstance().getWorld().getActivePlayer().inventoryContentChanged();
	}

	@Override
	public boolean acceptsToTakeItem(int index, InventoryItem item)
	{
		if (index < DoubleContainerInventoryRaster.CONTENT_OFFSET)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptsToPutItem(int index, InventoryItem item)
	{
		return true;
	}

	public void setSharedContent(SharedInventoryContent sharedInventoryContent)
	{
		_sharedContent = sharedInventoryContent;
	}

	public static final class DoubleContainerInventoryRaster implements InventoryRaster
	{

		public static final int CONTENT_OFFSET = 36;
		private Rectangle[] _cellAABBs;
		private ReadablePoint[] _cellCenters;
		private int _x, _y;

		public DoubleContainerInventoryRaster()
		{
			Configuration conf = Game.getInstance().getConfiguration();
			_x = conf.getWidth() / 2 - _hw;
			_y = conf.getHeight() / 2 - _hh;

			_cellAABBs = new Rectangle[getCellCount()];
			_cellCenters = new ReadablePoint[getCellCount()];

			/* Default Inventory */
			for (int y = 0; y < 10; y++)
			{
				for (int x = 0; x < 9; x++)
				{
					int add = 0;
					if (y > 0)
					{
						add = 8;
					}
					if (y > 3)
					{
						add = 36;
					}
					addCell(32 + x * 36, 44 + y * 36 + add);
				}
			}
		}

		private void addCell(int x, int y)
		{
			int index = 0;
			while (_cellCenters[index] != null)
			{
				++index;
			}
			_cellCenters[index] = new Point(_x + x, _y + y);
			_cellAABBs[index] = new Rectangle(_x + x - getCellWidth() / 2, _y + y - getCellHeight() / 2, getCellWidth(), getCellHeight());
		}

		@Override
		public boolean isInsideRasterAABB(int x, int y)
		{
			return x > _x && y > _y && x < _x + _hw + _hw && y < _y + _hh + _hh;
		}

		@Override
		public int getCellAt(int x, int y)
		{
			for (int i = 0; i < getCellCount(); ++i)
			{
				Rectangle r = _cellAABBs[i];
				if (r.contains(x, y))
				{
					return i;
				}
			}
			return -1;
		}

		@Override
		public ReadablePoint getCenterOfCell(int index)
		{
			return _cellCenters[index];
		}

		@Override
		public int getCellWidth()
		{
			return 32;
		}

		@Override
		public int getCellHeight()
		{
			return 32;
		}

		@Override
		public int getCellCount()
		{
			return 90;
		}

		@Override
		public Rectangle getCellAABB(int x, int y)
		{
			return _cellAABBs[getCellAt(x, y)];
		}
	}
}
