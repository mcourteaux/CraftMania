package org.craftmania.blocks.customblocks;

import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.game.Game;
import org.craftmania.inventory.CraftingTableInventory;
import org.craftmania.math.Vec3i;
import org.craftmania.world.BlockChunk;

public class CraftingTable extends DefaultBlock
{

	private CraftingTableInventory _inventory;
	
	public CraftingTable(BlockChunk chunk, Vec3i pos)
	{
		super(BlockManager.getInstance().getBlockType(BlockManager.getInstance().blockID("crafting_table")), chunk, pos);
		_inventory = new CraftingTableInventory();
		_inventory.setSharedContent(Game.getInstance().getWorld().getPlayer().getSharedInventoryContent());
	}
	
	@Override
	public boolean hasSpecialAction()
	{
		return true;
	}
	
	@Override
	public void performSpecialAction()
	{
		Game.getInstance().getWorld().setActivatedInventory(_inventory);
	}

}
