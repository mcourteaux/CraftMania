package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class StonePickaxe extends Tool
{
    public StonePickaxe()
    {
        super("stone_pickaxe", BlockClass.STONE, Material.STONE, new Vec2i(1, 6), 7.0f);
    }
}
