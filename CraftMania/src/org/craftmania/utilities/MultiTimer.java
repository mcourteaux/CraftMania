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
