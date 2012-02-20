package org.craftmania.datastructures;

import java.util.ArrayList;
import java.util.List;

public class DataNode<Data>
{
	private Data _data;
	private List<DataNode<Data>> _childs;
	
	public DataNode(Data d)
	{
		_data = d;
		_childs = new ArrayList<DataNode<Data>>();
	}
	
	public Data getData()
	{
		return _data;
	}
	
	public void addChild(DataNode<Data> node)
	{
		_childs.add(node);
	}
	
	public int childCount()
	{
		return _childs.size();
	}
	
	public DataNode<Data> getChild(int i)
	{
		return _childs.get(i);
	}
	
}
