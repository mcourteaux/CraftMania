package org.craftmania.world;

import org.craftmania.datastructures.Fast3DByteArray;
import org.craftmania.math.MathHelper;
import org.craftmania.world.Chunk.LightType;

public class LightBuffer
{

	private int _x, _z;
	private int _refX, _refY, _refZ;
	private Fast3DByteArray _buffer;

	public LightBuffer()
	{
		_buffer = new Fast3DByteArray(Chunk.CHUNK_SIZE_HORIZONTAL + 2, Chunk.CHUNK_SIZE_VERTICAL, Chunk.CHUNK_SIZE_HORIZONTAL + 2);
	}

	public void buffer(Chunk chunk)
	{
		_x = chunk.getAbsoluteX();
		_z = chunk.getAbsoluteZ();

		ChunkData data = chunk.getChunkData();
		byte rawlight, blockLight, sunlight;
		float sun = chunk.getWorld().getSunlight();

		int minY = MathHelper.clamp(MathHelper.floor(chunk.getVisibleContentAABB().minY() - 2), 0, Chunk.CHUNK_SIZE_VERTICAL);
		int maxY = MathHelper.clamp(MathHelper.ceil(chunk.getVisibleContentAABB().maxY() + 2), 0, Chunk.CHUNK_SIZE_VERTICAL);

		int i = 0;
		for (int x = -1; x <= Chunk.CHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int z = -1; z <= Chunk.CHUNK_SIZE_HORIZONTAL; ++z)
			{
				if (x != -1 && x != Chunk.CHUNK_SIZE_HORIZONTAL && z != -1 && z != Chunk.CHUNK_SIZE_HORIZONTAL)
				{
					for (int y = minY; y <= maxY; ++y)
					{
						i = ChunkData.positionToIndex(x, y, z);
						rawlight = data.getLight(i, LightType.RAW);
						blockLight = (byte) (rawlight & 0xF);
						sunlight = (byte) ((rawlight & 0xF0) >>> 4);

						sunlight *= sun * 2.0f;

						_buffer.set(x + 1, y, z + 1, (byte) Math.max(blockLight * 2, sunlight));
					}
				} else
				{
					Chunk c = chunk.getChunkContaining(_x + x, maxY, _z + z, false, false, false);
					if (c == null) continue;
					ChunkData neighborData = c.getChunkData();
					
					int xx = MathHelper.simplify(x, Chunk.CHUNK_SIZE_HORIZONTAL);
					int zz = MathHelper.simplify(z, Chunk.CHUNK_SIZE_HORIZONTAL);
					
					for (int y = minY; y <= maxY; ++y)
					{
						i = ChunkData.positionToIndex(xx, y, zz);
						rawlight = neighborData.getLight(i, LightType.RAW);
						blockLight = (byte) (rawlight & 0xF);
						sunlight = (byte) ((rawlight & 0xF0) >>> 4);

						sunlight *= sun * 2.0f;

						_buffer.set(x + 1, y, z + 1, (byte) Math.max(blockLight * 2, sunlight));
					}
				}
			}
		}
	}

	public void setReferencePoint(int x, int y, int z)
	{
		_refX = x - _x;
		_refY = y - 1;
		_refZ = z - _z;
	}

	public byte get(int x, int y, int z)
	{
		return _buffer.get(x + _refX, y + _refY, z + _refZ);
	}
}
