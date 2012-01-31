package org.craftmania.rendering;

import org.lwjgl.opengl.ARBVertexBufferObject;

public class ChunkMesh
{
	private volatile int _vertexCount;
	private volatile int _vbo;

	public ChunkMesh(int vertexCount, int vbo)
	{
		super();
		this._vertexCount = vertexCount;
		this._vbo = vbo;
	}

	public ChunkMesh()
	{
		_vbo = 0;
		_vertexCount = 0;
	}

	public void setVBO(int vbo)
	{
		this._vbo = vbo;
	}

	public void setVertexCount(int vertexCount)
	{
		this._vertexCount = vertexCount;
	}

	public int getVBO()
	{
		return _vbo;
	}

	public int getVertexCount()
	{
		return _vertexCount;
	}

	public synchronized void destroy()
	{
		if (_vbo != 0 && _vbo != -1)
		{
			synchronized (GLUtils.getOpenGLLock())
			{
				ARBVertexBufferObject.glDeleteBuffersARB(_vbo);
			}
			System.out.println("Delete VBO: " + _vbo);
			_vbo = 0;
		}
	}
}
