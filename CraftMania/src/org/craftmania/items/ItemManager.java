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
package org.craftmania.items;

import org.craftmania.blocks.BlockManager;
import org.craftmania.inventory.InventoryItem;

/**
 * 
 * @author martijncourteaux
 */
public class ItemManager
{

	private static ItemManager __instance;
	public static int ITEM_OFFSET = 128;

	public static ItemManager getInstance()
	{
		if (__instance == null)
		{
			__instance = new ItemManager();
		}
		return __instance;
	}

	private InventoryItem[] _items;

	private ItemManager()
	{
		_items = new InventoryItem[Short.MAX_VALUE - ITEM_OFFSET];
	}

	public InventoryItem getInventoryItem(short type)
	{
		if (type < ITEM_OFFSET)
		{
			return BlockManager.getInstance().getBlockType((byte) type);
		}
		return _items[type - ITEM_OFFSET];
	}

	public void putInventoryItem(short type, InventoryItem item)
	{
		if (type < ITEM_OFFSET)
		{
			return;
		}
		System.out.println("Put Item: " + type + " (" + item.getName() + " <" + item.getClass().getName() + ">)");
		_items[type - ITEM_OFFSET] = item;
	}

	public short getItemID(String name)
	{
		byte blockType = BlockManager.getInstance().blockID(name);
		if (blockType != -1)
		{
			return blockType;
		}

		for (int i = 0; i < _items.length; ++i)
		{
			InventoryItem item = _items[i];
			if (item != null && item.getName().equalsIgnoreCase(name))
			{
				return (short) (i + ITEM_OFFSET);
			}
		}
		return -1;
	}

}
