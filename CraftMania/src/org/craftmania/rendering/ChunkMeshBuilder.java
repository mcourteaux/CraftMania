/*******************************************************************************
 * Copyright 2012 Martijn Courteaux <martijn.courteaux@skynet.be>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.craftmania.rendering;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.craftmania.Side;
import org.craftmania.blocks.Block;
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
import org.craftmania.world.LightBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class ChunkMeshBuilder
{
	private static final boolean DEBUG = false;
	
	public static final Vec3f COLOR_WHITE = new Vec3f(1, 1, 1);

	public static final int STRIDE = 8;
	public static final int POSITION_SIZE = 3;
	public static final int POSITION_OFFSET = 0;
	public static final int COLOR_SIZE = 3;
	public static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE;
	public static final int TEX_COORD_SIZE = 2;
	public static final int TEX_COORD_OFFSET = COLOR_OFFSET + COLOR_SIZE;
	public static final int FLOAT_SIZE = 4;

	public static enum MeshType
	{
		OPAQUE, TRANSLUCENT
	}

	private static BlockManager _blockManager = BlockManager.getInstance();

	private static int USED_SIZE = 0;
	private static boolean SMOOTH_LIGHTING = true;

	public static void generateChunkMesh(Chunk chunk, MeshType meshType)
	{
		if (DEBUG)
		System.out.println("Building " + meshType.name() + " Mesh for " + chunk.toString() + "...");

		/* Make sure there are no list edits anymore */
		chunk.performListChanges();

		ChunkMesh mesh = chunk.getMesh();
		mesh.destroy(meshType);

		/* Compute vertex count */
		int vertexCount = chunk.getVertexCount(meshType);
		if (DEBUG)
		System.out.println("\tVertex Count = " + vertexCount);
		/*
		 * If there are no faces visible yet (because of generating busy), don't
		 * create a buffer
		 */
		if (vertexCount == 0)
		{
			return;
		}
		mesh.setVertexCount(meshType, vertexCount);

		/* Create a buffer */
		int vbo = BufferManager.getInstance().createBuffer();
		mesh.setVBO(meshType, vbo);

		/* Bind the buffer */
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

		/* Allocate size for the buffer */
		int size = vertexCount * STRIDE * FLOAT_SIZE;
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, GL15.GL_STATIC_DRAW);
		
		if (DEBUG)
		System.out.println("\tCreate VBO: " + vbo + " with size = " + size + " (ERROR: " + GL11.glGetError() + ")");

		/* Get the native buffer to write to */
		ByteBuffer byteBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY, size, null);
		if (byteBuffer == null)
		{
			System.out.println("\tCouldn't create a native VBO!: GL Error Code = " + GL11.glGetError());

			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			mesh.destroy(meshType);
			Thread.dumpStack();
			mesh.setVBO(meshType, -1);
			mesh.setVertexCount(meshType, 0);

			return;
		}

		FloatBuffer vertexBuffer = byteBuffer.asFloatBuffer();

		/* Store all vertices in the buffer */
		USED_SIZE = 0;

		/* Local temporary variables, used to speed up */
		IntList blockList = chunk.getVisibleBlocks();
		int blockIndex = -1;
		byte blockType = 0;
		boolean special = false;
		Vec3i vec = new Vec3i();
		BlockType type;
		Block block = null;
		LightBuffer lightBuffer = chunk.getLightBuffer();
		byte faceMask = 0;

		/* Iterate over the blocks */
		for (int i = 0; i < blockList.size(); ++i)
		{
			blockIndex = blockList.get(i);
			blockType = chunk.getChunkData().getBlockType(blockIndex);
			if (blockType == 0)
				continue;
			special = chunk.getChunkData().isSpecial(blockIndex);
			type = _blockManager.getBlockType(blockType);

			if ((meshType == MeshType.OPAQUE && !type.isTranslucent() && type.hasNormalAABB()) || (meshType == MeshType.TRANSLUCENT && (type.isTranslucent() || !type.hasNormalAABB())))
			{

				ChunkData.indexToPosition(blockIndex, vec);

				/* Build the light buffer */

				vec.setX(vec.x() + chunk.getAbsoluteX());
				vec.setZ(vec.z() + chunk.getAbsoluteZ());

				lightBuffer.setReferencePoint(vec.x(), vec.y(), vec.z());

				if (special)
				{
					block = chunk.getChunkData().getSpecialBlock(blockIndex);
					if (block.isVisible())
					{
						block.storeInVBO(vertexBuffer, lightBuffer);
					}
				} else
				{
					if (type.isCrossed())
					{
						type.getCrossedBlockBrush().storeInVBO(vertexBuffer, vec.x() + 0.5f, vec.y() + 0.5f, vec.z() + 0.5f, lightBuffer);
					} else
					{
						faceMask = chunk.getChunkData().getFaceMask(blockIndex);
						type.getDefaultBlockBrush().setFaceMask(faceMask);
						type.getBrush().storeInVBO(vertexBuffer, vec.x() + 0.5f, vec.y() + 0.5f, vec.z() + 0.5f, lightBuffer);
					}
				}
			}
		}

		/* Perform a check */
		if (USED_SIZE != STRIDE * FLOAT_SIZE * mesh.getVertexCount(meshType))
		{
			System.out.println("\t[WARNING!]: Used size = " + USED_SIZE);
			System.out.println("\t[WARNING!]: Vertex count = " + USED_SIZE / STRIDE / FLOAT_SIZE);
			mesh.setVertexCount(meshType, USED_SIZE / STRIDE / FLOAT_SIZE);
		}

		byteBuffer.flip();

		GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

	}

	public static void putColorWithLight4(FloatBuffer vertexBuffer, Vec3f vec, byte light, byte light1, byte light2, byte light3)
	{
		float value;

		if (SMOOTH_LIGHTING)
		{
			value = light + light1 + light2 + light3;
			value /= 120.0001f;
		} else
		{
			value = light / 30.001f;
		}
		vertexBuffer.put(vec.x() * value);
		vertexBuffer.put(vec.y() * value);
		vertexBuffer.put(vec.z() * value);
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void putColorWithLight3(FloatBuffer vertexBuffer, Vec3f vec, byte light, byte light1, byte light2)
	{
		float value;

		if (SMOOTH_LIGHTING)
		{
			value = light + light1 + light2;
			value /= 90.0001f;
		} else
		{
			value = light / 30.001f;
		}
		vertexBuffer.put(vec.x() * value);
		vertexBuffer.put(vec.y() * value);
		vertexBuffer.put(vec.z() * value);
		USED_SIZE += 3 * FLOAT_SIZE;
	}

	public static void putColorWithLight(FloatBuffer vertexBuffer, Vec3f vec, byte light)
	{
		float value;
		value = light / 30.001f;
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

	public static void generateChunkMeshes(Chunk chunk)
	{
		chunk.getLightBuffer().buffer(chunk);
		generateChunkMesh(chunk, MeshType.OPAQUE);
		generateChunkMesh(chunk, MeshType.TRANSLUCENT);
	}
}
