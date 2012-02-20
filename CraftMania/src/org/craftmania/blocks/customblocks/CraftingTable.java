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
package org.craftmania.blocks.customblocks;

import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.game.Game;
import org.craftmania.inventory.CraftingTableInventory;
import org.craftmania.math.Vec3i;
import org.craftmania.world.Chunk;

public class CraftingTable extends DefaultBlock
{

	private CraftingTableInventory _inventory;
	
	public CraftingTable(Chunk chunk, Vec3i pos)
	{
		super(BlockManager.getInstance().getBlockType(BlockManager.getInstance().blockID("crafting_table")), chunk, pos);
		_inventory = new CraftingTableInventory();
		_inventory.setSharedContent(Game.getInstance().getWorld().getPlayer().getSharedInventoryContent());
	}
	
	@Override
	public void performSpecialAction()
	{
		Game.getInstance().getWorld().setActivatedInventory(_inventory);
	}

}
