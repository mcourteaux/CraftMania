package org.craftmania.blocks;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec3f;
import org.newdawn.slick.opengl.Texture;

public final class BlockType extends InventoryItem
{

	private String type;
	private byte id;
	private DefaultBlockBrush brush;
	private Texture inventoryTexture;
	private Vec3f dimensions;
	private boolean solid;
	private boolean fixed;
	private boolean transculent;
	private int resistance;
	private boolean normalAABB;
	private int mineResult;
	private int mineResultCount;
	private String customClass;
	private boolean hasSpecialAction;
	private byte luminosity;

	public enum BlockClass
	{

		WOOD, STONE, SAND
	}

	private BlockClass blockClass;

	public BlockType(int id, String name)
	{
		super(name, 8.0f);

		this.type = name;
		this.id = (byte) id;
		this.brush = BlockBrushStorage.get(name);
		dimensions = new Vec3f(0.5f, 0.5f, 0.5f);
		solid = true;
		fixed = true;
		transculent = false;
		normalAABB = true;
		mineResult = -1;
		mineResultCount = 1;
		customClass = null;
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
	public void renderHoldableObject()
	{
		float scale = 0.1f;
		glScalef(scale, scale, scale);
		glRotatef(-40, 0, 0, 1);
		brush.renderAt(0, 0, 0);
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

	public DefaultBlockBrush getBrush()
	{
		return brush;
	}

	public Vec3f getDimensions()
	{
		return dimensions;
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

	public boolean isTransculent()
	{
		return transculent;
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

	public byte getLuminosity()
	{
		return luminosity;
	}

}
