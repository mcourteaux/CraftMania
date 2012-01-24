package org.craftmania.rendering;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public class GLUtils
{
	
	private static boolean _vboSupported;
	private static Object _openGL_lock;
	
	static
	{
		_openGL_lock = new Object();
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
	
	public static Object getOpenGLLock()
	{
		return _openGL_lock;
	}
}
