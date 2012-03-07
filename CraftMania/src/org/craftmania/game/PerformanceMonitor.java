package org.craftmania.game;

import org.craftmania.utilities.MultiTimer;

public class PerformanceMonitor
{
	
	private static final PerformanceMonitor _monitor = new PerformanceMonitor();
	public static PerformanceMonitor getInstance()
	{
		return _monitor;
	}
	
	
	public static enum Operation
	{
		RENDER_ALL, RENDER_OPAQUE, RENDER_TRANSLUCENT, RENDER_MANUAL, RENDER_SKY, RENDER_OVERLAY,  UPDATE
	}
	
	private MultiTimer _timer;
	
	public PerformanceMonitor()
	{
		_timer = new MultiTimer(Operation.values().length);
	}
	
	public void start(Operation op)
	{
		_timer.start(op.ordinal());
	}
	
	public void stop(Operation op)
	{
		_timer.stop(op.ordinal());
	}
	
	/**
	 * Returns the time in milliseconds
	 */
	public float get(Operation op)
	{
		return _timer.get(op.ordinal()) / 1000000.0f;
	}
	
	
}
