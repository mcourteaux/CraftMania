package org.craftmania.rendering;

import org.craftmania.utilities.IntList;
import org.lwjgl.opengl.GL15;

public class BufferManager
{

	private static BufferManager __instance;
	
	public static BufferManager getInstance()
	{
		if (__instance == null)
		{
			__instance = new BufferManager();
		}
		return __instance;
	}
	
	private IntList _buffersToDelete;
	private IntList _buffers;
	private Thread _mainThread;
	
	private BufferManager()
	{
		_mainThread = Thread.currentThread();
		_buffers = new IntList(512);
		_buffersToDelete = new IntList(32);
	}
	
	private void verifyThread()
	{
		if (_mainThread != Thread.currentThread())
		{
			throw new IllegalThreadStateException("Trying to access buffer operations from wrong thread");
		}
	}
	
	public int createBuffer()
	{
		verifyThread();
		int buffer = GL15.glGenBuffers();
		_buffers.add(buffer);
		return buffer;
	}
	
	public void deleteBuffer(int buffer)
	{
		if (_mainThread == Thread.currentThread())
		{
			deleteBufferDirect(buffer);
		} else
		{
			_buffersToDelete.add(buffer);
		}
	}

	private void deleteBufferDirect(int buffer)
	{
		GL15.glDeleteBuffers(buffer);
		_buffers.removeValue(buffer);
	}
	
	public int deleteQueuedBuffers()
	{
		int count = _buffersToDelete.size();
		
		if (count == 0)
			return 0;
		
		for (int i = 0; i < count; ++i)
		{
			deleteBufferDirect(_buffersToDelete.get(i));
		}
		_buffersToDelete.clear();
		
		return count;
	}
	
	public int getAliveBuffers()
	{
		return _buffers.size();
	}
	
}
