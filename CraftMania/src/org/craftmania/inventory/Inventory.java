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


import org.craftmania.game.FontStorage;
import org.craftmania.items.ItemManager;
import org.craftmania.math.MathHelper;
import org.craftmania.rendering.GLFont;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author martijncourteaux
 */
public abstract class Inventory
{

    private InventoryPlace[] _places;
    protected InventoryRaster _raster;
    protected InventoryPlace _draggingItem;
    protected SharedInventoryContent _sharedContent;
    protected boolean _dragging;

    public Inventory(int size)
    {
        _places = new InventoryPlace[size];
    }
    
    public int size()
    {
    	return _places.length;
    }

    public final void update()
    {
        while (Mouse.next())
        {
            int currentMouseIndex = _raster.getCellAt(Mouse.getEventX(), Mouse.getEventY());
            InventoryPlace draggingItemTemp = _draggingItem;
            if (currentMouseIndex == -1) // No current cell
            {
                if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0 && _dragging) // Cancel dragging
                {
                    if (acceptsToPutItem(_draggingItem._index, _draggingItem.getItemOrStackType()))
                    {
                        if (getInventoryPlace(_draggingItem._index) == null)
                        {
                            setContentAt(_draggingItem, _draggingItem._index);
                            _dragging = false;
                            _draggingItem = null;
                            inventoryEvent(new InventoryEvent(InventoryEvent.CANCELED, draggingItemTemp._index, draggingItemTemp.getItemCount(), draggingItemTemp.getItemTypeOrStackType()));
                        } else
                        {
                            InventoryPlace place = getInventoryPlace(_draggingItem._index);
                            if (place.isStack() && place.getItemTypeOrStackType() == draggingItemTemp.getItemTypeOrStackType())
                            {
                                place.getStack().addAmount(draggingItemTemp.getItemCount());
                                _dragging = false;
                                _draggingItem = null;
                                inventoryEvent(new InventoryEvent(InventoryEvent.CANCELED, draggingItemTemp._index, draggingItemTemp.getItemCount(), draggingItemTemp.getItemTypeOrStackType()));
                            }
                        }
                    }
                }
                continue;
            }
            InventoryPlace currentMousePlace = getInventoryPlace(currentMouseIndex);
            boolean currentMousePlaceContainsObject = currentMousePlace != null;
            int draggingItemType = _draggingItem == null ? -1 : _draggingItem.getItemTypeOrStackType();

            if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) // Select / Drop all
            {
                if (_dragging) // Drop ALL
                {
                    if (!acceptsToPutItem(currentMouseIndex, _draggingItem.getItemOrStackType())) // Cancel movement
                    {
                        // Check if it was taken from a forbidden place
                        if (!acceptsToPutItem(_draggingItem._index, _draggingItem.getItemOrStackType()))
                        {
                            // Can't cancel the movement because where it was taken from a place where you can't put items
                            // So, check for alternative actions
                            if (currentMousePlaceContainsObject)
                            {
                                if (_draggingItem.isStack() && currentMousePlace.isStack())
                                {
                                    if (currentMousePlace.getItemTypeOrStackType() == draggingItemType)
                                    {
                                        _draggingItem.getStack().addAmount(currentMousePlace.getItemCount());
                                        setContentAt(null, currentMouseIndex);
                                        inventoryEvent(new InventoryEvent(InventoryEvent.TAKE, currentMouseIndex, currentMousePlace.getItemCount(), draggingItemType));
                                    }
                                }
                            }
                            continue;
                        }
                        // Check if it came from a stack
                        InventoryPlace sourcePlace = getInventoryPlace(_draggingItem._index);
                        int count = _draggingItem.getItemCount();
                        if (sourcePlace == null)
                        {
                            setContentAt(_draggingItem, _draggingItem._index);
                        } else if (sourcePlace.isStack() && sourcePlace.getItemTypeOrStackType() == draggingItemType)
                        {
                            sourcePlace.getStack().addAmount(count);
                        }
                        _dragging = false;
                        _draggingItem = null;
                        inventoryEvent(new InventoryEvent(InventoryEvent.CANCELED, draggingItemTemp._index, draggingItemTemp.getItemCount(), draggingItemType));
                    } else // Accepts to drop item
                    {
                        if (currentMousePlaceContainsObject) // There is already something
                        {
                            if (currentMousePlace.isStack() && currentMousePlace.getItemTypeOrStackType() == draggingItemType) // Add it to the stack
                            {
                                currentMousePlace.getStack().addAmount(_draggingItem.getItemCount());
                                _dragging = false;
                                _draggingItem = null;
                                inventoryEvent(new InventoryEvent(InventoryEvent.DROP, currentMouseIndex, draggingItemTemp.getItemCount(), draggingItemType));
                            } else // Swap the dragging item with the cell content
                            {
                                setContentAt(_draggingItem, currentMouseIndex);
                                _draggingItem = currentMousePlace;
                                inventoryEvent(new InventoryEvent(InventoryEvent.TAKE, currentMouseIndex, currentMousePlace.getItemCount(), currentMousePlace.getItemTypeOrStackType()));
                                inventoryEvent(new InventoryEvent(InventoryEvent.DROP, currentMouseIndex, _draggingItem.getItemCount(), _draggingItem.getItemTypeOrStackType()));
                            }
                        } else
                        {
                            setContentAt(_draggingItem, currentMouseIndex);
                            _dragging = false;
                            _draggingItem = null;
                            inventoryEvent(new InventoryEvent(InventoryEvent.DROP, currentMouseIndex, draggingItemTemp.getItemCount(), draggingItemType));
                        }
                    }
                } else // Select ALL
                {
                    if (!currentMousePlaceContainsObject)
                    {
                        continue;
                    }
                    _draggingItem = currentMousePlace;
                    _dragging = true;
                    setContentAt(null, currentMouseIndex);
                    inventoryEvent(new InventoryEvent(InventoryEvent.TAKE, currentMouseIndex, _draggingItem.getItemCount(), _draggingItem.getItemTypeOrStackType()));
                }
            } else if (Mouse.getEventButtonState() && Mouse.getEventButton() == 1) // Drop ONE / Select half
            {
                if (_dragging) // Drop ONE
                {
                    boolean draggingItemIsStack = _draggingItem.isStack();

                    if (currentMousePlaceContainsObject) // Drop place contains already something
                    {
                        if (currentMousePlace.isStack() && currentMousePlace.getItemTypeOrStackType() == draggingItemType) // Same type
                        {
                            if ((_draggingItem.isStack() && --_draggingItem.getStack()._itemCount == 0) || !_draggingItem.isStack())
                            {
                                _draggingItem = null;
                                _dragging = false;
                            }

                            currentMousePlace.getStack().increaseItemCount();
                            inventoryEvent(new InventoryEvent(InventoryEvent.DROP, currentMouseIndex, 1, draggingItemType));
                        }
                    } else // Drop place is empty
                    {
                        if ((draggingItemIsStack && --_draggingItem.getStack()._itemCount == 0) || !_draggingItem.isStack())
                        {
                            _draggingItem = null;
                            _dragging = false;
                        }
                        if (draggingItemIsStack)
                        {
                            setContentAt(new InventoryPlace(currentMouseIndex, new InventoryItemStack((short) draggingItemType)), currentMouseIndex);
                        } else
                        {
                            setContentAt(new InventoryPlace(currentMouseIndex, draggingItemTemp.getItemOrStackType()), currentMouseIndex);
                        }
                        inventoryEvent(new InventoryEvent(InventoryEvent.DROP, currentMouseIndex, 1, draggingItemType));
                    }
                } else // Select half
                {
                    if (currentMousePlaceContainsObject && currentMousePlace.isStack())
                    {
                        int newStackSize = MathHelper.ceil(currentMousePlace.getItemCount() / 2.0f);
                        _dragging = true;
                        _draggingItem = new InventoryPlace(currentMouseIndex, new InventoryItemStack((short) currentMousePlace.getItemTypeOrStackType(), newStackSize));
                        currentMousePlace.getStack().subtractAmount(newStackSize);
                        inventoryEvent(new InventoryEvent(InventoryEvent.TAKE, currentMouseIndex, newStackSize, currentMousePlace.getItemTypeOrStackType()));
                    }
                }
            }
        }
    }

    public abstract void renderInventory();

    public abstract boolean acceptsToTakeItem(int index, InventoryItem item);

    public abstract boolean acceptsToPutItem(int index, InventoryItem item);

    protected abstract void inventoryEvent(final InventoryEvent evt);

    public boolean addToInventory(InventoryItem item)
    {
        short type = item.getInventoryTypeID();
        boolean stackable = item.isStackable();
        if (stackable)
        {
            int freeIndex = -1;
            for (int i = 0; i < _places.length; ++i)
            {
                if (!acceptsToTakeItem(i, item))
                {
                    continue;
                }
                if (getInventoryPlace(i) != null)
                {
                    if (getInventoryPlace(i).isStack() && getInventoryPlace(i)._stack._itemType == type)
                    {
                        getInventoryPlace(i)._stack.increaseItemCount();
                        inventoryEvent(new InventoryEvent(InventoryEvent.DROP, i, 1, item.getInventoryTypeID()));
                        return true;
                    }
                } else if (freeIndex == -1)
                {
                    freeIndex = i;
                }
            }
            // No stack of this kind yet
            if (freeIndex != -1)
            {
                setContentAt(new InventoryPlace(freeIndex, new InventoryItemStack(type)), freeIndex);
                inventoryEvent(new InventoryEvent(InventoryEvent.DROP, freeIndex, 1, item.getInventoryTypeID()));
                return true;
            }
            return false;
        } else
        {
            // Search an empty place
            for (int i = 0; i < _places.length; ++i)
            {
                if (!acceptsToTakeItem(i, item))
                {
                    continue;
                }
                if (_places[i] == null)
                {
                    setContentAt(new InventoryPlace(i, item), i);
                    inventoryEvent(new InventoryEvent(InventoryEvent.DROP, i, 1, item.getInventoryTypeID()));
                    return true;
                }
            }
            return false;
        }
    }

    public int getInventoryPlaceContentType(int index)
    {
        InventoryPlace p = getInventoryPlace(index);
        if (p == null)
        {
            return 0;
        }
        return p.getItemTypeOrStackType();
    }

    public void setContentAt(InventoryPlace obj, int index)
    {
        if (_sharedContent != null && index < _sharedContent.size())
        {
            _sharedContent.setContentAt(index, obj);
        } else
        {
            _places[index] = obj;
        }
        if (obj != null)
        {
            obj._index = index;
        }
    }

    public InventoryPlace getInventoryPlace(int index)
    {
        if (_sharedContent != null && index < _sharedContent.size())
        {
            return _sharedContent.getContentAt(index);
        }
        return _places[index];
    }

    public static class InventoryPlace
    {

        private int _index;
        private boolean _isStack;
        private InventoryItemStack _stack;
        private InventoryItem _item;

        public InventoryPlace(int index, InventoryItem item)
        {
            _index = index;
            _item = item;
            _isStack = false;
        }

        public InventoryPlace(int index, InventoryItemStack stack)
        {
            _index = index;
            _stack = stack;
            _stack._place = this;
            _isStack = true;
        }

        public void render()
        {
            if (_isStack)
            {
                _stack.render();
            } else
            {
                _item.renderInventoryItem();
            }
        }

        public boolean isStack()
        {
            return _isStack;
        }

        public InventoryItemStack getStack()
        {
            return _stack;
        }

        public InventoryItem getItem()
        {
            return _item;
        }

        public int getItemTypeOrStackType()
        {
            return _isStack ? _stack._itemType : _item.getInventoryTypeID();
        }

        public InventoryItem getItemOrStackType()
        {
            return _isStack ? ItemManager.getInstance().getInventoryItem(_stack._itemType) : _item;
        }

        public int getItemCount()
        {
            return _isStack ? _stack._itemCount : 1;
        }
    }

    public class InventoryItemStack
    {

        private InventoryPlace _place;
        private int _itemCount;
        private short _itemType;

        public InventoryItemStack(short itemType)
        {
            _itemType = itemType;
            _itemCount = 1;
        }

        public InventoryItemStack(short type, int count)
        {
            this(type);
            _itemCount = count;
        }

        public void setItemCount(int itemCount)
        {
            this._itemCount = itemCount;
        }

        public int getItemCount()
        {
            return _itemCount;
        }

        public int getItemType()
        {
            return _itemType;
        }

        public void increaseItemCount()
        {
            _itemCount++;
        }

        public void decreaseItemCount()
        {
            _itemCount--;
            System.out.println(_itemCount);
            if (_itemCount <= 0)
            {
                setContentAt(null, _place._index);
            }
        }

        private void render()
        {
            /* Render the little image */
            ItemManager.getInstance().getInventoryItem(_itemType).renderInventoryItem();


            /* Render stack amount */
            GLFont font = FontStorage.getFont("InventoryAmount");
            GL11.glColor3f(0.0f, 0.0f, 0.0f);
            font.print(-12, -18, String.valueOf(_itemCount));
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            font.print(-13, -17, String.valueOf(_itemCount));
        }

        public void addAmount(int count)
        {
            if (count < 0)
            {
                subtractAmount(count);
            } else
            {
                _itemCount += count;
            }
        }

        private void subtractAmount(int count)
        {
            if (count < 0)
            {
                addAmount(count);
            } else
            {
                _itemCount -= count;
                if (_itemCount <= 0)
                {
                    setContentAt(null, _place._index);
                }
            }
        }
    }
}
