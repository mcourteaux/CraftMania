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
package org.craftmania.utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.craftmania.math.Vec3f;

public class IOUtilities
{
	
	
	public static void writeVec3f(DataOutputStream dos, Vec3f vec) throws IOException
	{
		dos.writeFloat(vec.x());
		dos.writeFloat(vec.y());
		dos.writeFloat(vec.z());
	}

	public static void readVec3f(DataInputStream dis, Vec3f vec) throws IOException
	{
		vec.x(dis.readFloat());
		vec.y(dis.readFloat());
		vec.z(dis.readFloat());
	}

}
