package org.craftmania.rendering;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.craftmania.Side;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.blocks.DefaultBlockBrush;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.utilities.IntList;
import org.craftmania.world.Chunk;
import org.craftmania.world.ChunkData;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

public class ChunkMeshBuilder
{

	public static int STRIDE = 8;
	public static int POSITION_SIZE = 3;
	public static int POSITION_OFFSET = 0;
	public static int COLOR_SIZE = 3;
	public static int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE;
	public static int TEX_COORD_SIZE = 2;
	public static int TEX_COORD_OFFSET = COLOR_OFFSET + COLOR_SIZE;
	public static int FLOAT_SIZE = 4;

	private static BlockManager _blockManager = BlockManager.getInstance();

	private static int USED_SIZE = 0;
	private static boolean SMOOTH_LIGHTING = true;

	public static void generateChunkMesh(Chunk chunk)
	{
		synchronized (GLUtils.getOpenGLLock())
		{

			/* Make sure there are no list edits anymore */
			chunk.performListChanges();

			ChunkMesh mesh = chunk.getMesh();
			mesh.destroy();

			/* Compute vertex count */
			int vertexCount = chunk.getNumberOfVisibleFacesForVBO() * 4;

			/*
			 * If there are no faces visible yet (because of generating busy),
			 * don't create a buffer
			 */
			if (vertexCount == 0)
			{
				return;
			}
			mesh.setVertexCount(vertexCount);

			/* Create a buffer */
			int vbo = ARBVertexBufferObject.glGenBuffersARB();
			mesh.setVBO(vbo);

			/* Bind the buffer */
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vbo);

			/* Allocate size for the buffer */
			int size = vertexCount * STRIDE * FLOAT_SIZE;
			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, size, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
			System.out.println("Create VBO: " + vbo + " with size = " + size + " for chunk (" + chunk.getX() + ", " + chunk.getZ() + ") (ERROR: " + GL11.glGetError() + ")");

			/* Get the native buffer to write to */
			ByteBuffer byteBuffer = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, size, null);
			if (byteBuffer == null)
			{
				System.out.println("Couldn't create a native VBO!: GL Error Code = " + GL11.glGetError());

				ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
				ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);

				mesh.destroy();
				Thread.dumpStack();
				mesh.setVBO(-1);
				mesh.setVertexCount(0);

				return;
			}

			FloatBuffer vertexBuffer = byteBuffer.asFloatBuffer();

			/* Store all vertices in the buffer */
			USED_SIZE = 0;
			IntList blockList = chunk.getVisibleBlocks();
			int blockIndex = -1;
			byte blockType = 0;
			boolean special;
			Vec3i vec = new Vec3i();
			byte[][][] lightBuffer = new byte[3][3][3];
			byte faceMask;
			for (int i = 0; i < blockList.size(); ++i)
			{
				blockIndex = blockList.get(i);
				blockType = chunk.getChunkData().getBlockType(blockIndex);
				special = chunk.getChunkData().isSpecial(blockIndex);
				if (blockType == 0)
					continue;
				if (special)
				{
					// TODO: Do not include moving blocks, they should be
					// rendered afterwards.
				} else
				{
					faceMask = chunk.getChunkData().getFaceMask(blockIndex);
					ChunkData.indexToPosition(blockIndex, vec);

					/* Build the light buffer */
					for (int x = -1; x <= 1; ++x)
					{
						for (int y = -1; y <= 1; ++y)
						{
							for (int z = -1; z <= 1; ++z)
							{
								int xx = x + vec.x();
								int yy = y + vec.y();
								int zz = z + vec.z();

								lightBuffer[x + 1][y + 1][z + 1] = chunk.getTotalLightRelative(xx, yy, zz);
							}
						}
					}

					vec.setX(vec.x() + chunk.getAbsoluteX());
					vec.setZ(vec.z() + chunk.getAbsoluteZ());

					storeVertexData(vertexBuffer, vec.x(), vec.y(), vec.z(), faceMask, _blockManager.getBlockType(blockType), lightBuffer);
				}
			}
			mesh.setVertexCount(USED_SIZE);
			if (USED_SIZE != mesh.getVertexCount())
			{
				System.out.println("[WARNING!]: Used size = " + USED_SIZE);
			}

			byteBuffer.flip();

			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		}
	}

	public static void storeVertexData(FloatBuffer vertexBuffer, int x, int y, int z, byte faceMask, BlockType blockType, byte[][][] lightBuffer)
	{
		if (faceMask == 0)
		{
			return;
		}
		DefaultBlockBrush dbb = blockType.getBrush();

		float tileSize = 0.0624f;
		Vec3f p = new Vec3f(x, y, z);
		p.add(DefaultBlock.HALF_BLOCK_SIZE);
		for (int i = 0, bit = 1; i < 6; ++i, bit <<= 1)
		{
			if ((bit & faceMask) == bit)
			{
				Side side = Side.values()[i];
				Vec3f color = dbb.getColorFor(side);
				Vec2f uv = dbb.calcTextureOffsetFor(side);

				if (side == Side.TOP)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[0][2][1], lightBuffer[0][2][2], lightBuffer[1][2][2]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[2][2][1], lightBuffer[1][2][2], lightBuffer[2][2][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[1][2][0], lightBuffer[2][2][0], lightBuffer[2][2][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][2][1], lightBuffer[0][2][1], lightBuffer[1][2][0], lightBuffer[0][2][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.LEFT)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][0][1], lightBuffer[0][1][0], lightBuffer[0][0][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][0][1], lightBuffer[0][1][2], lightBuffer[0][0][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][1][2], lightBuffer[0][2][1], lightBuffer[0][2][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[0][1][1], lightBuffer[0][1][0], lightBuffer[0][2][0], lightBuffer[0][2][1]);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.FRONT)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[0][0][2], lightBuffer[0][1][2], lightBuffer[1][0][2]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][0][2], lightBuffer[2][1][2], lightBuffer[2][0][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][2][2], lightBuffer[2][2][2], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][2], lightBuffer[1][2][2], lightBuffer[0][1][2], lightBuffer[0][2][2]);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.RIGHT)
				{
					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][2][1], lightBuffer[2][1][0], lightBuffer[2][2][0]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][2][2], lightBuffer[2][2][1], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][0][2], lightBuffer[2][0][1], lightBuffer[2][1][2]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[2][1][1], lightBuffer[2][0][0], lightBuffer[2][0][1], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BACK)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][2][0], lightBuffer[0][2][0], lightBuffer[0][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][2][0], lightBuffer[2][2][0], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][0][0], lightBuffer[2][0][0], lightBuffer[2][1][0]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][1][0], lightBuffer[1][0][0], lightBuffer[0][0][0], lightBuffer[0][1][0]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BOTTOM)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][0], lightBuffer[0][0][0], lightBuffer[0][0][1]);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][0], lightBuffer[2][0][0], lightBuffer[2][0][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][2], lightBuffer[2][0][2], lightBuffer[2][0][1]);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putColorWithLight(vertexBuffer, color, lightBuffer[1][0][1], lightBuffer[1][0][2], lightBuffer[0][0][2], lightBuffer[0][0][1]);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				}
			}
		}

	}

	public static void putColorWithLight(FloatBuffer vertexBuffer, Vec3f vec, byte light, byte light1, byte light2, byte light3)
	{
		float value;

		if (SMOOTH_LIGHTING)
		{
			value = light + light1 + light2 + light3;
			value /= 60.0001f;
		} else
		{
			value = light / 15.001f;
		}
		vertexBuffer.put(vec.x() * value);
		vertexBuffer.put(vec.y() * value);
		vertexBuffer.put(vec.z() * value);
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void putColorWithLight(FloatBuffer vertexBuffer, Vec3f vec, byte light)
	{
		float value;
		value = light / 15.001f;
		vertexBuffer.put(vec.x() * value);
		vertexBuffer.put(vec.y() * value);
		vertexBuffer.put(vec.z() * value);
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void putVec3f(FloatBuffer vertexBuffer, Vec3f vec)
	{
		vertexBuffer.put(vec.x());
		vertexBuffer.put(vec.y());
		vertexBuffer.put(vec.z());
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void put3f(FloatBuffer vertexBuffer, float f0, float f1, float f2)
	{
		vertexBuffer.put(f0);
		vertexBuffer.put(f1);
		vertexBuffer.put(f2);
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void put2f(FloatBuffer vertexBuffer, float f0, float f1)
	{
		vertexBuffer.put(f0);
		vertexBuffer.put(f1);
		USED_SIZE += 2 * FLOAT_SIZE;

	}
}
