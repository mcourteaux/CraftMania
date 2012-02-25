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


import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertex2i;

import org.craftmania.game.Configuration;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.items.ItemManager;
import org.craftmania.math.Vec2f;
import org.craftmania.recipes.Recipe;
import org.craftmania.recipes.RecipeManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Point;
import org.lwjgl.util.ReadablePoint;
import org.lwjgl.util.Rectangle;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author martijncourteaux
 */
public class DefaultPlayerInventory extends Inventory
{

    /* STATIC OPENGL SHIT */
    private static int _inventoryDrawList = -1;
    private static Vec2f _texPosUpLeft;
    private static Vec2f _texPosDownRight;
    private static Texture _tex;
    private static int _hw = 176;
    private static int _hh = 167;
    /** INVENTORY VARIABLES */
    private static boolean _validRecipePresent = false;

    private int _recipeXonTable = 0;
    private int _recipeYonTable = 0;
    private int _recipeWidthOnTable = 0;
    private int _recipeHeightOnTable = 0;

    public DefaultPlayerInventory()
    {
        super(45);
        _raster = new DefaultPlayerInventoryRaster();
    }

    private void checkForRecipe()
    {
        _recipeXonTable = 0;
        _recipeYonTable = 0;
        _recipeWidthOnTable = 0;
        _recipeHeightOnTable = 0;
        
        /* Check for valid crafting recipes */
        int[][] table = getCraftingTable();

        Recipe r = RecipeManager.getInstance().getRecipe(table);
        if (r != null)
        {
            short type = (short) r.getResultingItem();
            System.out.println("Recipe Product = " + type);
            InventoryItem item = ItemManager.getInstance().getInventoryItem(type);
            if (item.isStackable())
            {
                setContentAt(new InventoryPlace(0, new InventoryItemStack(type, r.getResultAmount())), DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 4);
            } else
            {
                setContentAt(new InventoryPlace(0, item), DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 4);
            }
            _validRecipePresent = true;


            int minX = 10;
            int minY = 10;
            int maxX = 0;
            int maxY = 0;
            boolean containsData = false;

            for (int y = 0; y < table.length; ++y)
            {
                for (int x = 0; x < table[0].length; ++x)
                {
                    int elem = table[y][x];
                    if (elem != 0)
                    {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                        containsData = true;
                    }
                }
            }
            if (!containsData)
            {
                return;
            }

            _recipeXonTable = minX;
            _recipeYonTable = maxX;
            _recipeWidthOnTable = maxX - minX + 1;
            _recipeHeightOnTable = maxY - minY + 1;
        } else
        {
            setContentAt(null, DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 4);
            _validRecipePresent = false;
        }
    }

    @Override
    protected void inventoryEvent(InventoryEvent evt)
    {
        
        if (evt.getAction() == InventoryEvent.DROP && evt.getIndex() >= DefaultPlayerInventoryRaster.CRAFTING_OFFSET)
        {
            checkForRecipe();
        } else if (evt.getAction() == InventoryEvent.TAKE && evt.getIndex() == 44)
        {
            System.out.println("Take product!");

            for (int y = 0; y < 2; ++y)
            {
                for (int x = 0; x < 2; ++x)
                {
                    int index = DefaultPlayerInventoryRaster.CRAFTING_OFFSET + (y * 2) + x;
                    InventoryPlace place = getInventoryPlace(index);
                    if (place != null)
                    {
                        if (place.isStack())
                        {
                            place.getStack().decreaseItemCount();
                        } else
                        {
                            setContentAt(null, index);
                        }
                    }
                }

            }
            checkForRecipe();
        } else if (evt.getAction() == InventoryEvent.CANCELED)
        {
            checkForRecipe();
        } else if (evt.getAction() == InventoryEvent.TAKE)
        {
            if (evt.getIndex() >= DefaultPlayerInventoryRaster.CRAFTING_OFFSET)
            {
                checkForRecipe();
            }
        }
        
        inventoryContentChanged();
    }

    public int[][] getCraftingTable()
    {
        int[][] table = new int[2][2];
        table[0][0] = getInventoryPlaceContentType(DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 0);
        table[0][1] = getInventoryPlaceContentType(DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 1);
        table[1][0] = getInventoryPlaceContentType(DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 2);
        table[1][1] = getInventoryPlaceContentType(DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 3);
        return table;
    }

    public void inventoryContentChanged()
    {
        Game.getInstance().getWorld().getActivePlayer().inventoryContentChanged();
    }

    @Override
    public void renderInventory()
    {
        Configuration conf = Game.getInstance().getConfiguration();

        if (_inventoryDrawList == -1)
        {
            loadStaticContent();
            createRenderList();
        }

        glPushMatrix();
        glTranslatef(conf.getWidth() / 2.0f, conf.getHeight() / 2.0f, 0.0f);

        _tex.bind();

        glCallList(_inventoryDrawList);
        glPopMatrix();

        for (int i = 0; i < _raster.getCellCount(); ++i)
        {
            glPushMatrix();
            ReadablePoint r = _raster.getCenterOfCell(i);
            glTranslatef(r.getX(), r.getY(), 0);
            if (Mouse.getX() < r.getX() + 16 && Mouse.getX() > r.getX() - 16 && Mouse.getY() < r.getY() + 16 && Mouse.getY() > r.getY() - 16)
            {
                glDisable(GL_TEXTURE_2D);
                glColor4f(1.0f, 1.0f, 1.0f, 0.2f);
                glBegin(GL_QUADS);
                glVertex2i(-16, -16);
                glVertex2i(+16, -16);
                glVertex2i(+16, +16);
                glVertex2i(-16, +16);
                glEnd();
                glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            }

            InventoryPlace place = getInventoryPlace(i);
            if (place != null)
            {
                place.render();
            }
            glPopMatrix();
        }

        /* Draggin item */
        if (_dragging && _draggingItem != null)
        {
            glPushMatrix();
            glTranslatef(Mouse.getX(), Mouse.getY(), 0);
            _draggingItem.render();
            glPopMatrix();
        }

    }

    private static void createRenderList()
    {
        _inventoryDrawList = glGenLists(1);
        glNewList(_inventoryDrawList, GL_COMPILE);

        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        glTexCoord2f(_texPosUpLeft.x(), _texPosUpLeft.y());
        glVertex2f(-_hw, _hh);
        glTexCoord2f(_texPosDownRight.x(), _texPosUpLeft.y());
        glVertex2f(_hw, _hh);
        glTexCoord2f(_texPosDownRight.x(), _texPosDownRight.y());
        glVertex2f(_hw, -_hh);
        glTexCoord2f(_texPosUpLeft.x(), _texPosDownRight.y());
        glVertex2f(-_hw, -_hh);
        glEnd();

        glEndList();
    }

    public static void loadStaticContent()
    {
        _texPosUpLeft = new Vec2f(0, 0);
        _texPosDownRight = new Vec2f(_texPosUpLeft.x() + _hw, _texPosUpLeft.y() + _hh);

        _tex = TextureStorage.getTexture("gui.inventory");
        _texPosUpLeft.x(_texPosUpLeft.x() / _tex.getTextureWidth());
        _texPosUpLeft.y(_texPosUpLeft.y() / _tex.getTextureHeight());
        _texPosDownRight.x(_texPosDownRight.x() / _tex.getTextureWidth());
        _texPosDownRight.y(_texPosDownRight.y() / _tex.getTextureHeight());

    }

    @Override
    public boolean acceptsToTakeItem(int index, InventoryItem item)
    {
        if (index < DefaultPlayerInventoryRaster.AMOR_OFFSET)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptsToPutItem(int index, InventoryItem item)
    {
        if (index < DefaultPlayerInventoryRaster.AMOR_OFFSET)
        {
            return true;
        }
        if (index < DefaultPlayerInventoryRaster.CRAFTING_OFFSET)
        {
            return false;
        }
        if (index < DefaultPlayerInventoryRaster.CRAFTING_OFFSET + 4)
        {
            return true;
        }
        return false;
    }

    public void setSharedContent(SharedInventoryContent sharedInventoryContent)
    {
        _sharedContent = sharedInventoryContent;
    }

    private final class DefaultPlayerInventoryRaster implements InventoryRaster
    {

        public static final int AMOR_OFFSET = 9 * 4;
        public static final int CRAFTING_OFFSET = AMOR_OFFSET + 4;
        private Rectangle[] _cellAABBs;
        private ReadablePoint[] _cellCenters;
        private int _x, _y;

        public DefaultPlayerInventoryRaster()
        {
            Configuration conf = Game.getInstance().getConfiguration();
            _x = conf.getWidth() / 2 - _hw;
            _y = conf.getHeight() / 2 - _hh;

            _cellAABBs = new Rectangle[getCellCount()];
            _cellCenters = new ReadablePoint[getCellCount()];

            /* Default Inventory */
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 9; x++)
                {
                    int add = 0;
                    if (y > 0)
                    {
                        add = 8;
                    }
                    addCell(32 + x * 36, 34 + y * 36 + add);
                }
            }

            /* Amor */
            for (int y = 0; y < 4; ++y)
            {
                addCell(32, 194 + y * 36);
            }

            /* Crafting Table */
            for (int y = 0; y < 2; ++y)
            {
                for (int x = 0; x < 2; ++x)
                {
                    addCell(192 + x * 36, 266 - y * 36);
                }
            }
            addCell(304, 246);

        }

        private void addCell(int x, int y)
        {
            int index = 0;
            while (_cellCenters[index] != null)
            {
                ++index;
            }
            _cellCenters[index] = new Point(_x + x, _y + y);
            _cellAABBs[index] = new Rectangle(_x + x - getCellWidth() / 2, _y + y - getCellHeight() / 2, getCellWidth(), getCellHeight());
        }

        @Override
        public boolean isInsideRasterAABB(int x, int y)
        {
            return x > _x && y > _y && x < _x + _hw + _hw && y < _y + _hh + _hh;
        }

        @Override
        public int getCellAt(int x, int y)
        {
            for (int i = 0; i < getCellCount(); ++i)
            {
                Rectangle r = _cellAABBs[i];
                if (r.contains(x, y))
                {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public ReadablePoint getCenterOfCell(int index)
        {
            return _cellCenters[index];
        }

        @Override
        public int getCellWidth()
        {
            return 32;
        }

        @Override
        public int getCellHeight()
        {
            return 32;
        }

        @Override
        public int getCellCount()
        {
            return 9 * 4 + 9;
        }

        @Override
        public Rectangle getCellAABB(int x, int y)
        {
            return _cellAABBs[getCellAt(x, y)];
        }
    }
}
