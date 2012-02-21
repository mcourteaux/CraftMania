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
package org.craftmania.inventory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.craftmania.inventory.Inventory.InventoryItemStack;
import org.craftmania.inventory.Inventory.InventoryPlace;
import org.craftmania.items.ItemManager;

public class InventoryIO
{

	public static void writeInventory(Inventory inv, DataOutputStream dos) throws IOException
	{
		dos.writeInt(inv.size());

		for (int i = 0; i < inv.size(); ++i)
		{
			InventoryPlace place = inv.getInventoryPlace(i);
			if (place == null)
			{
				dos.writeByte(0);
			} else
			{
				if (place.isStack())
				{
					dos.writeByte(2);
					InventoryItemStack stack = place.getStack();
					dos.writeShort(stack.getItemType());
					dos.writeInt(stack.getItemCount());
				} else
				{
					dos.writeByte(1);
					InventoryItem item = place.getItem();
					dos.writeShort(item.getInventoryTypeID());
					dos.writeFloat(item.getHealth());
				}
			}
		}
	}

	public static void readInventory(DataInputStream dis, Inventory inv) throws IOException
	{
		int length = dis.readInt();

		if (length != inv.size())
		{
			throw new IOException("Inventory size is invalid!");
		}

		for (int i = 0; i < inv.size(); ++i)
		{
			byte type = dis.readByte();
			if (type == 0)
			{
				inv.setContentAt(null, i);
			} else
			{
				if (type == 2)
				{
					short stackItemType = dis.readShort();
					int stackItemCount = dis.readInt();
					inv.setContentAt(new InventoryPlace(i, inv.new InventoryItemStack(stackItemType, stackItemCount)), i);
				} else if (type == 1)
				{
					short itemType = dis.readShort();
					float health = dis.readFloat();
					// TODO use the health
					inv.setContentAt(new InventoryPlace(i, ItemManager.getInstance().getInventoryItem(itemType)), i);
				} else
				{
					throw new IOException("Inventory data is invalid!");
				}
			}
		}
	}

}
