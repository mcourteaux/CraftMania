package org.craftmania.inventory;



import org.lwjgl.util.ReadablePoint;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author martijncourteaux
 */
public interface InventoryRaster
{
    
    public boolean isInsideRasterAABB(int x, int y);
    public int getCellAt(int x, int y);
    public ReadablePoint getCenterOfCell(int index);
    
    public int getCellWidth();
    public int getCellHeight();
        
    public int getCellCount();
    
    public Rectangle getCellAABB(int x, int y);
    
}
