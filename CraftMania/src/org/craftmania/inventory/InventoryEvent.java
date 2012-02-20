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
