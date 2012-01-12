package org.craftmania.world;

import org.craftmania.world.generators.ChunkGenerator;

public class BlockChunkLoader
{
	private World _world;
	private ChunkManager _chunkManager;
	private WorldProvider _worldProvier;

	public BlockChunkLoader(World world)
	{
		_world = world;
		_chunkManager = world.getChunkManager();
		_worldProvier = world.getWorldProvider();
	}
	
	public void generateChunk(final int x, final int z)
	{
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				ChunkGenerator gen = new ChunkGenerator(_world);
				gen.generateChunk(x, z);
			}
		}, "Chunk Generate Thread (" + x + ", " + z + ")").start();
	}

	public void deleteChunk(final BlockChunk blockChunk)
	{
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				blockChunk.destroy();
			}
		}, "Chunk Delete Thread (" + blockChunk.getX() + ", " + blockChunk.getZ() + ")").start();
	}
}
