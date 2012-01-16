package org.craftmania.world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.craftmania.blocks.Block;
import org.craftmania.datastructures.Fast3DArray;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec2i;
import org.craftmania.math.Vec3i;
import org.craftmania.world.generators.ChunkGenerator;

public class BlockChunkLoader implements Runnable
{
	private World _world;
	@SuppressWarnings("unused")
	private ChunkManager _chunkManager;
	@SuppressWarnings("unused")
	private WorldProvider _worldProvier;

	private List<Vec2i> _chunksToLoad;
	private volatile boolean _loading;
	private volatile int _threadsBusy;

	public BlockChunkLoader(World world)
	{
		_world = world;
		_chunkManager = world.getChunkManager();
		_worldProvier = world.getWorldProvider();
		_chunksToLoad = new ArrayList<Vec2i>();

		Thread t = new Thread(this, "BlockChunkLoader");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run()
	{
		while (true)
		{
			if (!_loading)
			{
				if (!_chunksToLoad.isEmpty())
				{
					_loading = true;
					final Vec2i pos = _chunksToLoad.remove(0);
					_threadsBusy++;
					Thread t = new Thread(new Runnable()
					{

						@Override
						public void run()
						{
							try
							{
								loadOrGenerateChunk(pos.x(), pos.y());
								_threadsBusy--;
							} catch (Exception e)
							{
								System.err.println("Couldn't load chunk!");
								e.printStackTrace();
							}
							_loading = false;
						}
					}, "Chunk Loader Thread (" + pos.x() + ", " + pos.y() + ")");
					t.setPriority(2);
					t.start();
				}
			}

			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void generateOrLoadChunk(final int x, final int z)
	{
		_chunksToLoad.add(new Vec2i(x, z));
	}

	public void deleteChunk(final BlockChunk blockChunk)
	{
		Thread t = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (blockChunk)
				{
					_threadsBusy++;
					try
					{
						saveChunk(blockChunk);
					} catch (Exception e)
					{
						System.err.println("Couldn't save the BlockChunk");
						e.printStackTrace();
					}
					blockChunk.destroy();
					_threadsBusy--;
				}
			}
		}, "Chunk Delete Thread (" + blockChunk.getX() + ", " + blockChunk.getZ() + ")");
		t.setPriority(2);
		t.start();
	}
	
	private long getUniquePositionID(int x, int z)
	{
		return MathHelper.cantorize(MathHelper.mapToPositive(x), MathHelper.mapToPositive(z));
	}
	
	private File getChunkFile(int x, int z)
	{
		long uniquePositionID = getUniquePositionID(x, z);
		File f = Game.getInstance().getRelativeFile(Game.FILE_BASE_USER_DATA, "${world}/chunks/" + Long.toHexString(uniquePositionID) + ".chunk");
		f.getParentFile().mkdirs();
		f.getParentFile().mkdir();
		return f;
	}
	
	protected void loadOrGenerateChunk(int x, int z) throws Exception
	{
		File file = getChunkFile(x, z);
		BlockChunk chunk = _world.getChunkManager().getBlockChunk(x, z, true, false);
		if (!file.exists())
		{
			ChunkGenerator gen = new ChunkGenerator(_world);
			chunk = gen.generateChunk(x, z);
			chunk.setLoading(false);
			return;
		}
		chunk.setLoading(true);
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		
		boolean generated = dis.readBoolean();
		System.out.println("Load Chunk (" + chunk.getX() + ", " + chunk.getZ() + "): generated = " + generated); 

		
		
		Fast3DArray<Block> blocks = chunk.getBlocks();
		int size = chunk.getBlocks().size();
		
		Vec3i blockPos = new Vec3i();
		for (int i = 0; i < size; ++i)
		{
			byte b = dis.readByte();
			if (b != 0)
			{
				blocks.rawIndexToVec3i(i, blockPos);
				int bx = blockPos.x();
				int by = blockPos.y();
				int bz = blockPos.z();
				chunk.setBlockTypeRelative(bx, by, bz, b, false, false);
			}
		}
		
		dis.close();
		chunk.setGenerated(generated);
		
		if (!generated)
		{
			ChunkGenerator gen = new ChunkGenerator(_world);
			gen.generateChunk(x, z);
		}
		
		chunk.setLoading(false);
	}

	protected void saveChunk(BlockChunk blockChunk) throws Exception
	{		
		File file = getChunkFile(blockChunk.getX(), blockChunk.getZ());
		
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		
		/* Store if the chunk was generated or only created */
		dos.writeBoolean(blockChunk.isGenerated());
		System.out.println("Save Chunk (" + blockChunk.getX() + ", " + blockChunk.getZ() + "): generated = " + blockChunk.isGenerated()); 
		
		Fast3DArray<Block> blocks = blockChunk.getBlocks();
		int size = blocks.size();
		
		for (int i = 0; i < size; ++i)
		{
			Block b = blocks.getRawObject(i);
			if (b == null)
			{
				dos.writeByte(0);
			} else
			{
				dos.writeByte(b.getBlockType().getID());
			}
		}
		
		dos.flush();
		dos.close();
	}

	public boolean isBusy()
	{
		return _threadsBusy > 0 || _chunksToLoad.size() > 0;
	}
}
