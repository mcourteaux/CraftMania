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
package org.craftmania.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadPool
{
	private int _maximumThreads;
	private volatile int _runningThreads;
	private List<WaitingRunnable> _waitingRunnables;

	public ThreadPool(int maximumThreads)
	{
		_maximumThreads = maximumThreads;
		_waitingRunnables = new ArrayList<WaitingRunnable>();
	}

	public void addThread(Runnable runnable, int priority)
	{
		synchronized (this)
		{
			_waitingRunnables.add(new WaitingRunnable(runnable, priority));
			Collections.sort(_waitingRunnables);
		}
		manage();
	}

	private synchronized void manage()
	{
		if (_runningThreads < _maximumThreads && !_waitingRunnables.isEmpty())
		{
			final WaitingRunnable logic = _waitingRunnables.remove(_waitingRunnables.size() - 1);
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					++_runningThreads;
					logic._runnable.run();
					--_runningThreads;
					manage();
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}
	
	private static class WaitingRunnable implements Comparable<WaitingRunnable>
	{
		private Runnable _runnable;
		private int _priority;
		
		public WaitingRunnable(Runnable runnable, int priority)
		{
			_runnable = runnable;
			_priority = priority;
		}
		
		@Override
		public int compareTo(WaitingRunnable o)
		{
			return o._priority - _priority;
		}
		
		
	}
}
