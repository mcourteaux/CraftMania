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
package org.craftmania.blocks;

import java.nio.FloatBuffer;

import org.craftmania.math.Vec3f;
import org.craftmania.world.LightBuffer;

public abstract class BlockBrush
{

	public abstract void setPosition(float x, float y, float z);

	public void setPosition(Vec3f v)
	{
		setPosition(v.x(), v.y(), v.z());
	}

	public abstract void render(LightBuffer lightBuffer);
	public abstract void storeInVBO(FloatBuffer vbo, float x, float y, float z, LightBuffer lightBuffer);
	public abstract int getVertexCount();

	public abstract void create();

	public abstract void release();
}
