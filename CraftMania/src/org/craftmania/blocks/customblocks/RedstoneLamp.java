package org.craftmania.blocks.customblocks;

import java.nio.FloatBuffer;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.blocks.DefaultBlockBrush;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;
import org.craftmania.world.LightBuffer;
import org.craftmania.world.Chunk.LightType;

public class RedstoneLamp extends DefaultBlock implements RedstoneLogic
{

	private final static DefaultBlockBrush BRUSH_POWERED;

	static
	{
		BRUSH_POWERED = new DefaultBlockBrush();
		BRUSH_POWERED.setTexture(new Vec2f(4, 13));
	}

	private boolean _powered;

	public RedstoneLamp(Chunk chunk, Vec3i pos)
	{
		super(BlockManager.getInstance().getBlockType("redstone_lamp"), chunk, pos);
	}

	@Override
	public void feed(int power)
	{
		System.out.println("RedstoneLamp power = " + power);
		if (!_powered)
		{
			_powered = true;
			_chunk.spreadLight(getX(), getY(), getZ(), (byte) 15, LightType.BLOCK);
		}
	}

	@Override
	public void unfeed(int power)
	{
		if (_powered)
		{
			_powered = false;
			_chunk.unspreadLight(getX(), getY(), getZ(), (byte) 15, LightType.BLOCK);
		}
	}

	@Override
	public boolean isPowered()
	{
		return _powered;
	}

	@Override
	public void connect(Side side)
	{

	}

	@Override
	public void disconnect(Side side)
	{
	}
	
	@Override
	public synchronized void neighborChanged(Side side)
	{
		super.neighborChanged(side);
		
		Vec3i n = side.getNormal();
		byte bType = _chunk.getBlockTypeAbsolute(getX() + n.x(), getY() + n.y(), getZ() + n.z(), false, false, false);
		if (bType <= 0)
		{
			disconnect(side);
			return;
		}
		BlockType type = BlockManager.getInstance().getBlockType(bType);
		if (type.hasRedstoneLogic())
		{
			Block block = _chunk.getSpecialBlockAbsolute(getX() + n.x(), getY() + n.y(), getZ() + n.z());
			RedstoneLogic rl = (RedstoneLogic) block;
			rl.connect(Side.getOppositeSide(side));
			connect(side);
		} else
		{
			disconnect(side);
		}
	}

	@Override
	public void destruct()
	{
		super.destruct();

		if (_powered)
		{
			_chunk.unspreadLight(getX(), getY(), getZ(), (byte) 15, LightType.BLOCK);
		}
	}

	@Override
	public void storeInVBO(FloatBuffer vbo, LightBuffer lightBuffer)
	{
		if (!_powered)
		{
			super.storeInVBO(vbo, lightBuffer);
		} else
		{
			BRUSH_POWERED.setFaceMask(getFaceMask());
			BRUSH_POWERED.storeInVBO(vbo, getX() + 0.5f, getY() + 0.5f, getZ() + 0.5f, lightBuffer);
		}
	}

}
