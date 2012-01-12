package org.craftmania.items.tools;

import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.items.Tool;
import org.craftmania.math.Vec2i;

public class StoneShovel extends Tool
{
    public StoneShovel()
    {
        super("stone_shovel", BlockClass.SAND, Material.STONE, new Vec2i(1, 5), 8.0f);
    }
}
