package org.craftmania.blocks.customblocks;

import org.craftmania.Side;

public interface RedstoneLogic
{
	public void feed();
	public void unfeed();
	public boolean isPowered();
	public void connect(Side side);
	public void disconnect(Side side);
}
