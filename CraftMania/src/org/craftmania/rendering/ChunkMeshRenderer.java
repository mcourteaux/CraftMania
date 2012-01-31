package org.craftmania.rendering;

import org.craftmania.game.TextureStorage;
import org.craftmania.world.BlockChunk;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class ChunkMeshRenderer
{
	public static int STRIDE = 8;
	public static int POSITION_SIZE = 3;
	public static int POSITION_OFFSET = 0;
	public static int COLOR_SIZE = 3;
	public static int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE;
	public static int TEX_COORD_SIZE = 2;
	public static int TEX_COORD_OFFSET = COLOR_OFFSET + COLOR_SIZE;
	public static int FLOAT_SIZE = 4;

	public static void renderChunkMesh(BlockChunk chunk)
	{
		if (chunk.getMesh().getVBO() <= 0)
		{
			return;
		}

		synchronized (GLUtils.getOpenGLLock())
		{

			/* Bind the correct texture */
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			TextureStorage.getTexture("terrain").bind();

			ChunkMesh mesh = chunk.getMesh();

			/* Bind the buffer */
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, mesh.getVBO());

			/* Enable the different kinds of data in the buffer */
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			// System.out.println("Chunk Vertices = " + mesh.getVertexCount());

			/* Define the starting positions */
			GL11.glVertexPointer(POSITION_SIZE, GL11.GL_FLOAT, STRIDE * FLOAT_SIZE, POSITION_OFFSET * FLOAT_SIZE);
			GL11.glTexCoordPointer(TEX_COORD_SIZE, GL11.GL_FLOAT, STRIDE * FLOAT_SIZE, TEX_COORD_OFFSET * FLOAT_SIZE);
			GL11.glColorPointer(COLOR_SIZE, GL11.GL_FLOAT, STRIDE * FLOAT_SIZE, COLOR_OFFSET * FLOAT_SIZE);

			/* Draw the buffer */
			GL11.glDrawArrays(GL11.GL_QUADS, 0, mesh.getVertexCount());

			/* Unbind the buffer */
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);

			/* Disable the different kindds of data */
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		}
	}
}
