package org.craftmania.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLUtils
{
    public static FloatBuffer wrapDirect(float... floats)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(floats.length * 4);
        bb.order(ByteOrder.nativeOrder());
        return (FloatBuffer) bb.asFloatBuffer().put(floats).flip();
    }
    
    
}
