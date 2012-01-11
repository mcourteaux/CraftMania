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
