package org.craftmania.blocks.customblocks;

import org.craftmania.Side;

public interface RedstoneLogic
{
	
	public static final int MAXIMUM_REDSTONE_TRAVELING_DISTANCE = 32;

	
	public void feed(int power);
	public void unfeed(int power);
	public boolean isPowered();
	public void connect(Side side);
	public void disconnect(Side side);
}
