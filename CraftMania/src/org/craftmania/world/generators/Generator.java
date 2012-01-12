package org.craftmania.world.generators;

import org.craftmania.game.Game;
import org.craftmania.world.ChunkManager;

public class Generator
{
	
	protected ChunkManager _chunkManager;
	protected final long _worldSeed;

	public Generator()
	{
		_chunkManager = Game.getInstance().getWorld().getChunkManager();
		_worldSeed = Game.getInstance().getWorld().getWorldSeed();
	}
}
