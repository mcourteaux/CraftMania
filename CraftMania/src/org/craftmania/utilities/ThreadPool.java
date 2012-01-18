package org.craftmania.utilities;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool
{
	private int _maximumThreads;
	private volatile int _runningThreads;
	private List<Runnable> _waitingRunnables;

	public ThreadPool(int maximumThreads)
	{
		_maximumThreads = maximumThreads;
		_waitingRunnables = new ArrayList<Runnable>();
	}

	public void addThread(Runnable runnable)
	{
		synchronized (this)
		{
			_waitingRunnables.add(runnable);
		}
		manage();
	}

	private synchronized void manage()
	{
		if (_runningThreads < _maximumThreads && !_waitingRunnables.isEmpty())
		{
			final Runnable logic = _waitingRunnables.remove(0);
			Thread t = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					++_runningThreads;
					logic.run();
					--_runningThreads;
					manage();
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}
}
