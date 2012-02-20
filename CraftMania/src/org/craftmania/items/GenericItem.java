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
package org.craftmania.items;

import org.craftmania.math.Vec2i;
import org.newdawn.slick.opengl.Texture;


/**
 *
 * @author martijncourteaux
 */
public class GenericItem extends TexturedItem
{

    private boolean stackable;

    public GenericItem(String name, float animSpeed, boolean stackable, Texture tex, Vec2i texPos)
    {
        super(name, animSpeed, tex, texPos);
        this.stackable = stackable;
    }
    
    @Override
    public boolean isStackable()
    {
        return stackable;
    }

    @Override
    public void update()
    {
        // Do nothing
    }
    
}
