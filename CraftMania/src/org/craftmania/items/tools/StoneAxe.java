package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class StoneAxe extends Tool
{
    public StoneAxe()
    {
        super("stone_axe", BlockClass.WOOD, Material.STONE, new Vec2i(1, 7), 7.2f);
    }
}
