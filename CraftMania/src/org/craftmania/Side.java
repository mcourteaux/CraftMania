package org.craftmania;

import org.craftmania.math.Vec3i;

public enum Side
{
	BACK(new Vec3i(0, 0, -1)), FRONT(new Vec3i(0, 0, 1)), LEFT(new Vec3i(-1, 0, 0)), RIGHT(new Vec3i(1, 0, 0)), TOP(new Vec3i(0, 1, 0)), BOTTOM(new Vec3i(0, -1, 0));
	
	private Vec3i normal;
	
	private Side(Vec3i normal)
	{
		this.normal = normal;
	}
	
	public Vec3i getNormal()
	{
		return normal;
	}

	public static Side getOppositeSide(Side side)
	{
		/* This method requires the opposite sides be next to each other in the enum. */
		int ordinal = side.ordinal();
		if ((ordinal & 1) == 0)
		{
			return Side.values()[ordinal + 1];
		} else
		{
			return Side.values()[ordinal - 1];
		}
	}
}
