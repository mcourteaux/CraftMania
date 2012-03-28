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
package org.craftmania.math;

public class Vec3f
{
	private float x, y, z;

	/**
	 * Constructs a new Vec3f and clones the values of the passed vector.
	 */
	public Vec3f(Vec3i v)
	{
		this(v.x(), v.y(), v.z());
	}

	public Vec3f(float x, float y, float z)
	{
		super();
		set(x, y, z);
	}

	/**
	 * Constructs a new Vec3f and copies the values of the passed vector.
	 * 
	 * @param v
	 *            the vector to be copied
	 */
	public Vec3f(Vec3f v)
	{
		this(v.x, v.y, v.z);
	}

	/**
	 * Constructs a new Vec3f with the value: (0, 0, 0)
	 */
	public Vec3f()
	{
		this(0.0f, 0.0f, 0.0f);
	}

	public Vec3f set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Subtracts this vector by the passed vector.
	 * 
	 * @param v
	 *            the vector to subtract from this
	 * @return {@code this}
	 */
	public Vec3f sub(Vec3f v)
	{
		set(x - v.x, y - v.y, z - v.z);
		return this;
	}
	
	/**
	 * Subtracts this vector by the passed vector.
	 * 
	 * @param v
	 *            the vector to subtract from this
	 * @return {@code this}
	 */
	public Vec3f sub(Vec3i v)
	{
		set(x - v.x(), y - v.y(), z - v.z());
		return this;
	}

	/**
	 * Adds the passed vector to this vector
	 * 
	 * @param v
	 *            the vector to add
	 * @return {@code this}
	 */
	public Vec3f add(Vec3f v)
	{
		set(x + v.x, y + v.y, z + v.z);
		return this;
	}

	/**
	 * Adds the passed vector to this vector
	 * 
	 * @param v
	 *            the vector to add
	 * @return {@code this}
	 */
	public Vec3f add(Vec3i v)
	{
		set(x + v.x(), y + v.y(), z + v.z());
		return this;
	}
	
	/**
	 * Performs a scalar product on this vector
	 * 
	 * @param factor
	 * @return {@code this}
	 */
	public Vec3f scale(float factor)
	{
		set(x * factor, y * factor, z * factor);

		return this;
	}

	/**
	 * Uses cache.
	 * 
	 * @return the squared length of this vector
	 */
	public float lengthSquared()
	{
		return x * x + y * y + z * z;
	}

	/**
	 * Uses cache.
	 * 
	 * @return the length of this vector
	 */
	public float length()
	{
		return (float) Math.sqrt(lengthSquared());
	}

	/**
	 * This vector will be the result of the cross product, performed on the two
	 * vectors passed. Returns {@code this} vector.
	 * 
	 * @param a
	 * @param b
	 * @return {@code this}
	 */
	public Vec3f cross(Vec3f a, Vec3f b)
	{
		set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
		return this;
	}

	/**
	 * Performs a dot product on the two specified vectors.
	 * 
	 * @param a
	 * @param b
	 * @return the result of the dot product.
	 */
	public static float dot(Vec3f a, Vec3f b)
	{
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public float x()
	{
		return x;
	}

	public float y()
	{
		return y;
	}

	public float z()
	{
		return z;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public void setZ(float z)
	{
		this.z = z;
	}

	public void x(float x)
	{
		setX(x);
	}

	public void y(float y)
	{
		setY(y);
	}

	public void z(float z)
	{
		setZ(z);
	}

	public Vec3f set(Vec3f vec)
	{
		set(vec.x, vec.y, vec.z);
		return this;
	}

	public Vec3f set(Vec3i vec)
	{
		set(vec.x(), vec.y(), vec.z());
		return this;
	}

	@Override
	public String toString()
	{
		return "Vec3f [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

	/**
	 * Performs the operation: {@code this = this + (v * factor);}
	 * 
	 * @param v
	 *            the vector to add
	 * @param factor
	 *            the multiplier for the additional vector
	 * @return {@code this}
	 */
	public Vec3f addFactor(Vec3f v, float factor)
	{
		set(x + v.x * factor, y + v.y * factor, z + v.z * factor);
		return this;
	}

	public Vec3f addFactor(Vec3i v, float factor)
	{
		set(x + v.x() * factor, y + v.y() * factor, z + v.z() * factor);
		return this;
	}

	public float normalise()
	{
		float len = length();
		scale(1.0f / len);
		return len;
	}

	public Vec3f sub(float x, float y, float z)
	{
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vec3f add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vec3f other = (Vec3f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}




}
