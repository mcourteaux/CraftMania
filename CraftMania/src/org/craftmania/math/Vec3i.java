package org.craftmania.math;

public class Vec3i
{
	private int x, y, z;

	/**
	 * Cached value for the length of the vector
	 */
	private float len;

	/**
	 * Constructs a new Vec3i with this value: (0, 0, 0)
	 */
	public Vec3i()
	{
		this(0, 0, 0);
	}
	
	public Vec3i(int x, int y, int z)
	{
		super();
		set(x, y, z);
	}

	/**
	 * Constructs a new Vec3i and copies the values of the passed vector.
	 * @param v the vector to be copied
	 */
	public Vec3i(Vec3i v)
	{
		this(v.x, v.y, v.z);
	}

	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		
		clearCache();
	}
	
	private void clearCache()
	{
		this.len = Float.NaN;
	}

	/**
	 * Subtracts this vector with the passed vector.
	 * 
	 * @param v
	 *            the vector to subtract from this
	 * @return {@code this}
	 */
	public Vec3i sub(Vec3i v)
	{
		set(x - v.x, y - v.y, z - v.z);
		return this;
	}

	/**
	 * Adds the passed vector to this vector
	 * 
	 * @param v
	 *            the vector to add
	 * @return {@code this}
	 */
	public Vec3i add(Vec3i v)
	{
		set(x + v.x, y + v.y, z + v.z);
		return this;
	}
	
	
	/**
	 * Performs a scalar product on this vector
	 * @param f
	 * @return {@code this}
	 */
	public Vec3i scale(float f)
	{
		/* Backup the length */
		float len = this.len;
		
		set(MathHelper.floor(x * f), MathHelper.floor(y * f), MathHelper.floor(z * f));
		
		/* Restore the length */
		if (!Float.isNaN(len))
		{
			this.len = len * f;
		}
		
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
		if (Float.isNaN(len))
		{
			len = (float) Math.sqrt(lengthSquared());
		}
		return len;
	}

	/**
	 * This vector will be the result of the cross product, performed on the two
	 * vectors passed. Returns {@code this} vector.
	 * 
	 * @param a
	 * @param b
	 * @return {@code this}
	 */
	public Vec3i cross(Vec3i a, Vec3i b)
	{
		set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
		return this;
	}

	/**
	 * Performs a dot product on the two specified vectors.
	 * @param a
	 * @param b
	 * @return the result of the dot product.
	 */
	public static int dot(Vec3i a, Vec3i b)
	{
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public int x()
	{
		return x;
	}

	public int y()
	{
		return y;
	}

	public int z()
	{
		return z;
	}
	
	public void setX(int x)
	{
		this.x = x;
		clearCache();
	}
	
	public void setY(int y)
	{
		this.y = y;
		clearCache();
	}
	
	public void setZ(int z)
	{
		this.z = z;
		clearCache();
	}
	
	public void x(int x)
	{
		setX(x);
	}
	
	public void y(int y)
	{
		setY(y);
	}
	
	public void z(int z)
	{
		setZ(z);
	}

	public void set(Vec3i vec)
	{
		set(vec.x, vec.y, vec.z);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		
		if (obj instanceof Vec3i)
		{
			Vec3i v = (Vec3i) obj;
			return v.x == x && v.y == y && v.z == z;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Vec3i [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
