package org.craftmania.world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.craftmania.blocks.Block;
import org.craftmania.datastructures.Fast3DArray;
import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3i;

public class ChunkLoader
{

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

	private File getChunkFile(Chunk ch)
	{
		return getChunkFile(ch.getX(), ch.getZ());
	}

	public void loadChunk(Chunk chunk) throws IOException
	{
		File file = getChunkFile(chunk);
		if (!file.exists())
		{
			/* The chunk is totally new, so set it as "loaded" */
			chunk.setLoaded(true);
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
				chunk.setBlockTypeRelative(bx, by, bz, b, false, false, false);
			}
		}

		dis.close();
		
		chunk.cache();
		
		chunk.setGenerated(generated);
		chunk.setLoading(false);
		chunk.setLoaded(true);
	}

	protected void saveChunk(Chunk blockChunk) throws Exception
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
//			if (i % 512 == 0)
//			{
//				try
//				{
//					Thread.sleep(1);
//				} catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

		}

		dos.flush();
		dos.close();
	}
}
