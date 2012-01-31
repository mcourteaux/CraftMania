package org.craftmania.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.blocks.DefaultBlockBrush;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.craftmania.world.BlockChunk;
import org.craftmania.world.BlockList;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

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
	
	
	private static int USED_SIZE = 0;

	public static void generateChunkMesh(BlockChunk chunk)
	{
		synchronized (GLUtils.getOpenGLLock())
		{
			
			/* Make sure there are no list edits anymore */
			chunk.performListChanges();

			ChunkMesh mesh = chunk.getMesh();
			mesh.destroy();

			/* Compute vertex count */
			int vertexCount = chunk.getNumberOfVisibleFacesForVBO() * 4;
			
			/* If there are no faces visible yet (because of generating busy), don't create a buffer */
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
			System.out.println("Create VBO: " + vbo + " with size = " + size);

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
			BlockList blockList = chunk.getVisibleBlocks();
			Block block = null;
			DefaultBlock defaultBlock = null;
			for (int i = 0; i < blockList.size(); ++i)
			{
				block = blockList.getBlockAtIndex(i);
				if (block.isRenderingManually())
				{
					// TODO: Do not include moving blocks, they should be rendered afterwards.
				} else if (block instanceof DefaultBlock)
				{
					defaultBlock = (DefaultBlock) block;

					storeVertexData(vertexBuffer, defaultBlock);
				}
			}
			System.out.println("Used size = " + USED_SIZE);

			byteBuffer.flip();

			ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		}
	}

	public static void storeVertexData(FloatBuffer vertexBuffer, DefaultBlock block)
	{
		byte faceMask = block.getFaceMask();
		if (faceMask == 0)
		{
			return;
		}
		DefaultBlockBrush dbb = block.getBlockType().getBrush();

		float tileSize = 0.0624f;
		Vec3f p = new Vec3f(block.getPosition());
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
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.LEFT)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.FRONT) // TODO: Check this! Changed
												// Front and Back because of
												// CULL_FACE is working
												// incorrectly for these two
												// faces
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());
				} else if (side == Side.RIGHT)
				{
					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BACK)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() + 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				} else if (side == Side.BOTTOM)
				{
					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() - 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y());

					put3f(vertexBuffer, p.x() + 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x() + tileSize, uv.y() + tileSize);

					put3f(vertexBuffer, p.x() - 0.5f, p.y() - 0.5f, p.z() + 0.5f);
					putVec3f(vertexBuffer, color);
					put2f(vertexBuffer, uv.x(), uv.y() + tileSize);
				}
			}
		}

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
