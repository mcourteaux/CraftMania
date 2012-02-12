package org.craftmania.utilities;

public class MultiTimer
{
	private int _timerCount;
	private long[] _startTimes;
	private long[] _stopTimes;
	
	public MultiTimer(int timerCount)
	{
		_timerCount = timerCount;
		_startTimes = new long[timerCount];
		_stopTimes = new long[timerCount];
	}
	
	public void start(int i)
	{
		_startTimes[i] = System.nanoTime();
	}
	
	public void stop(int i)
	{
		_stopTimes[i] = System.nanoTime();
	}
	
	public long get(int i)
	{
		return _stopTimes[i] - _startTimes[i];
	}
	
	public int getTimerCount()
	{
		return _timerCount;
	}
	
}
