package org.craftmania.rendering;

import org.craftmania.rendering.ChunkMeshBuilder.MeshType;
import org.lwjgl.opengl.ARBVertexBufferObject;

public class ChunkMesh
{
	private volatile int _vertexCount[];
	private volatile int[] _vbos;

	public ChunkMesh(int[] vertexCount, int[] vbos)
	{
		super();
		this._vertexCount = vertexCount;
		this._vbos = vbos;
	}

	public ChunkMesh()
	{
		_vbos = new int[MeshType.values().length];
		_vertexCount = new int[MeshType.values().length];
	}

	public void setVBO(MeshType meshType, int vbo)
	{
		this._vbos[meshType.ordinal()] = vbo;
	}

	public void setVertexCount(MeshType meshType, int vertexCount)
	{
		this._vertexCount[meshType.ordinal()] = vertexCount;
	}

	public int getVBO(MeshType meshType)
	{
		return _vbos[meshType.ordinal()];
	}

	public int getVertexCount(MeshType meshType)
	{
		return _vertexCount[meshType.ordinal()];
	}

	public synchronized void destroy(MeshType meshType)
	{
		if (_vbos[meshType.ordinal()] != 0 && _vbos[meshType.ordinal()] != -1)
		{
			synchronized (GLUtils.getOpenGLLock())
			{
				ARBVertexBufferObject.glDeleteBuffersARB(_vbos[meshType.ordinal()]);
			}
//			System.out.println("Delete VBO: " + _vbo);
			_vbos[meshType.ordinal()] = 0;
		}
	}

	public void destroyAllMeshes()
	{
		destroy(MeshType.SOLID);
		destroy(MeshType.TRANSCULENT);
	}
}
