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
package org.craftmania.world;

import org.craftmania.utilities.ThreadPool;

public class ChunkThreading
{

	private ChunkManager _chunkManager;
	private ThreadPool _generatePool;
	private ThreadPool _savePool;
	private ThreadPool _deletePool;
	private volatile int _threads;

	public ChunkThreading(ChunkManager chman)
	{
		_chunkManager = chman;
		int generatePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		System.out.println("Generate Pool Size: " + generatePoolSize);
		_generatePool = new ThreadPool(generatePoolSize);
		_savePool = new ThreadPool(2);
		_deletePool = new ThreadPool(1);
	}

	public void saveChunk(final Chunk chunk)
	{
		_savePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().saveChunk(chunk);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		}, 0);
	}

	public void deleteChunk(final Chunk chunk)
	{
		chunk.setDestroying(true);
		/* Mesh has to be deleted in the main thread, because of OpenGL */
		chunk.destroyMesh();

		/* Add a runnable to the pool */
		_deletePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						chunk.destroy();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		}, 0);
	}

	public void loadChunk(final Chunk chunk, int priority)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().loadChunk(chunk);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					_chunkManager.getWorld().requestCheckForNewVisibleChunks();

				}
				--_threads;
			}
		}, priority);
	}

	public void generateChunk(final Chunk chunk, int priority)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						chunk.generate();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					_chunkManager.getWorld().requestCheckForNewVisibleChunks();

				}
				--_threads;
			}
		}, priority);
	}

	public void saveAndUnloadChunk(final Chunk chunk)
	{
		/* Mesh has to be deleted in the main thread, because of OpenGL */
		chunk.destroyMesh();

		/* Add a runnable to the pool */
		_savePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().saveChunk(chunk);
						chunk.destroy();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				--_threads;
			}
		}, 0);
	}

	public boolean isTreadingBusy()
	{
		return _threads > 0;
	}

	public void loadAndGenerateChunk(final Chunk chunk, int priority)
	{
		chunk.setLoading(true);
		_generatePool.addThread(new Runnable()
		{

			@Override
			public void run()
			{
				++_threads;
				synchronized (chunk)
				{
					try
					{
						_chunkManager.getBlockChunkLoader().loadChunk(chunk);
						chunk.generate();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					_chunkManager.getWorld().requestCheckForNewVisibleChunks();
				}
				--_threads;
			}
		}, priority);
	}

	public boolean isLoadingThreadPoolFull()
	{
		return _generatePool.isFull();
	}
}
