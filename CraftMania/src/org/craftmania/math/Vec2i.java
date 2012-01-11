package org.craftmania.math;

public class Vec2i
{
	private int x, y;

	/**
	 * Cached value for the length of the vector
	 */
	private float len;

	/**
	 * Constructs a new Vec2i with this value: (0, 0)
	 */
	public Vec2i()
	{
		this(0, 0);
	}

	public Vec2i(int x, int y)
	{
		set(x, y);
	}

	/**
	 * Constructs a new Vec3f and copies the values of the passed vector.
	 * 
	 * @param v
	 *            the vector to be copied
	 */
	public Vec2i(Vec2i v)
	{
		this(v.x, v.y);
	}

	public void set(int x, int y)
	{
		if (this.x != x || this.y != y)
			clearCache();
		this.x = x;
		this.y = y;
	}

	private void clearCache()
	{
		this.len = Float.NaN;
	}

	/**
	 * Uses cache.
	 * 
	 * @return the squared length of this vector
	 */
	public float lengthSquared()
	{
		return x * x + y * y;
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
	 * Performs a scalar product on this vector
	 * 
	 * @param factor
	 * @return {@code this}
	 */
	public Vec2i scale(int factor)
	{
		/* Backup the length */
		float len = this.len;

		set(x * factor, y * factor);

		/* Restore the length */
		if (!Float.isNaN(len))
		{
			this.len = len * factor;
		}

		return this;
	}

	/**
	 * Subtracts this vector with the passed vector.
	 * 
	 * @param v
	 *            the vector to subtract from this
	 * @return {@code this}
	 */
	public Vec2i sub(Vec2i v)
	{
		set(x - v.x, y - v.y);
		return this;
	}

	/**
	 * Adds the passed vector to this vector
	 * 
	 * @param v
	 *            the vector to add
	 * @return {@code this}
	 */
	public Vec2i add(Vec2i v)
	{
		set(x + v.x, y + v.y);
		return this;
	}

	public int x()
	{
		return x;
	}

	public int y()
	{
		return y;
	}

	public void setX(int x)
	{
		if (this.x != x)
			clearCache();
		this.x = x;
	}

	public void setY(int y)
	{
		if (this.y != y)
			clearCache();
		this.y = y;
	}

}
