package org.craftmania.inventory;



/**
 *
 * @author martijncourteaux
 */
public class InventoryEvent
{
    public static final int TAKE = 1;
    public static final int DROP = 2;
    public static final int CANCELED = 3;
    
    private int _action;
    private int _index;
    private int _count;
    private int _type;

    public InventoryEvent(int _action, int _index, int _count, int _type)
    {
        this._action = _action;
        this._index = _index;
        this._count = _count;
        this._type = _type;
    }

    public int getAction()
    {
        return _action;
    }

    public int getCount()
    {
        return _count;
    }

    public int getIndex()
    {
        return _index;
    }

    public int getType()
    {
        return _type;
    }
    
    
}
