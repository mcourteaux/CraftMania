package org.craftmania.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author martijncourteaux
 */
public class BlockBrushStorage
{
    private static Map<String, DefaultBlockBrush> brushes = new HashMap<String, DefaultBlockBrush>();

    private BlockBrushStorage()
    {
    }
    
    public static void releaseBrushes()
    {
        for (Entry<String, DefaultBlockBrush> entry : brushes.entrySet())
        {
            entry.getValue().releaseDisplayList();
        }
    }
    
    public static void loadBrush(String id, DefaultBlockBrush bb)
    {
        brushes.put(id, bb);
    }
    
    public static DefaultBlockBrush get(String id)
    {
        return brushes.get(id);
    }
}
