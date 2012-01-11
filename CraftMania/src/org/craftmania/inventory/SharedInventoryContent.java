package org.craftmania.inventory;



import org.craftmania.inventory.Inventory.InventoryPlace;

/**
 *
 * @author martijncourteaux
 */
public class SharedInventoryContent
{
    private int _size;
    private Inventory.InventoryPlace[] _content;

    public SharedInventoryContent(int size)
    {
        _size = size;
        _content = new Inventory.InventoryPlace[size];
    }

    public InventoryPlace getContentAt(int index)
    {
        return _content[index];
    }
    
    public void setContentAt(int index, InventoryPlace content)
    {
        _content[index] = content;
    }
    
    public int size()
    {
        return _size;
    }
}
