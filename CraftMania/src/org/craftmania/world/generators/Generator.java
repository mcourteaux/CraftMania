package org.craftmania.world.generators;

import org.craftmania.blocks.BlockManager;
import org.craftmania.game.Game;
import org.craftmania.world.ChunkManager;

public class Generator
{
	
	protected ChunkManager _chunkManager;
	protected BlockManager _blockManager;
	protected final long _worldSeed;

	public Generator()
	{
		_chunkManager = Game.getInstance().getWorld().getChunkManager();
		_blockManager = BlockManager.getInstance();
		_worldSeed = Game.getInstance().getWorld().getWorldSeed();
	}
}
