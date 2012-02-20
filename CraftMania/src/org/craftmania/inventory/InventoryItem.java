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



import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.craftmania.GameObject;
import org.craftmania.blocks.Block;

/**
 *
 * @author martijncourteaux
 */
public abstract class InventoryItem extends GameObject
{

    protected String _name;
    protected float _animationSpeed;

    protected InventoryItem(String name, float animationSpeed)
    {
        this._name = name;
        this._animationSpeed = animationSpeed;
    }

    public abstract float calcDamageFactorToBlock(byte block);

    public abstract float calcDamageInflictedByBlock(byte block);

    public abstract short getInventoryTypeID();

    public abstract void renderInventoryItem();

    public abstract float getHealth();

    public abstract boolean isStackable();

    public abstract void inflictDamage(float toolDamage);

    public String getName()
    {
        return _name;
    }

    public void renderHoldableObject(byte[][][] lightBuffer)
    {
        glPushMatrix();
        float scale = 0.1f / 16.0f;
        glScalef(scale, scale, scale);
        glColor3f(0.5f, 0.5f, 0.5f);
        /* Render the texture */
        for (float i = 0.0f; i < 0.02f; i += 0.002f)
        {
            if (i > 0.016f)
            {
                glColor3f(1.0f, 1.0f, 1.0f);
            }
            renderInventoryItem();
            glTranslatef(0, 0, 0.002f);
        }
        glPopMatrix();
    }

    @Override
    public void render()
    {
        throw new UnsupportedOperationException("You shouldn't use this method!");
    }

    public float getAnimationSpeed()
    {
        return _animationSpeed;
    }
}
