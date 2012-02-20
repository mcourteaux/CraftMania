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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * 
 * @author martijncourteaux
 */
public class ReverseIterator<T> implements Iterable<T>, Iterator<T>
{
	private final List<T> original;
	private final ListIterator<T> i;

	public ReverseIterator(List<T> original)
	{
		this.original = original;
		this.i = original.listIterator(original.size());
	}

	public Iterator<T> iterator()
	{
		return this;
	}
	
	public boolean hasNext()
	{
		return i.hasPrevious();
	}

	public T next()
	{
		return i.previous();
	}

	public void remove()
	{
		i.remove();
	}
}
