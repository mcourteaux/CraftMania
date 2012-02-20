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
