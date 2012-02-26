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
package org.craftmania.rendering;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public class GLUtils
{
	
	private static boolean _vboSupported;
	
	static
	{
		try
		{
			ContextCapabilities cap = GLContext.getCapabilities();
			long glGenBuffers = getFunctionPointer(cap, "glGenBuffers");
			_vboSupported = glGenBuffers != 0;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static FloatBuffer wrapDirect(float... floats)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect(floats.length * 4);
		bb.order(ByteOrder.nativeOrder());
		return (FloatBuffer) bb.asFloatBuffer().put(floats).flip();
	}

	public static boolean isVBOSupported()
	{
		return _vboSupported;
	}
	
	
	private static long getFunctionPointer(ContextCapabilities cap, String function) throws Exception
	{
		Class<? extends ContextCapabilities> capClass = cap.getClass();
		Field f = capClass.getDeclaredField(function);
		f.setAccessible(true);
		Object value = f.get(cap);
		if (value instanceof Long)
		{
			return (Long) value;
		}
		throw new NoSuchFieldException(function);
	}
}
