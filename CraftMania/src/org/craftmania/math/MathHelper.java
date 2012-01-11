package org.craftmania.math;

public class MathHelper
{

	public static float f_PI = (float) Math.PI;
	public static float f_2PI = (float) (2.0d * Math.PI);

	private static float SIN_TABLE[];

	static
	{
		/**
		 * Initializes the sin lookup table
		 */
		SIN_TABLE = new float[0x10000];
		for (int i = 0; i < 0x10000; i++)
		{
			SIN_TABLE[i] = (float) Math.sin(((double) i * 3.1415926535897931D * 2D) / 65536D);
		}
	}

	public static final int floorDivision(int i, int divisor)
	{
		return floor(((float) i) / divisor);
	}

	public static final float sin(float f)
	{
		return SIN_TABLE[(int) (f * 10430.38F) & 0xffff];
	}

	public static final float cos(float f)
	{
		return SIN_TABLE[(int) (f * 10430.38F + 16384F) & 0xffff];
	}
	
	public static float tan(float f)
	{
		return sin(f) / cos(f);
	}

	public static int floor(float f)
	{
		int i = (int) f;
		return f >= (float) i ? i : i - 1;
	}

	public static int floor(double d)
	{
		int i = (int) d;
		return d >= (double) i ? i : i - 1;
	}

	public static int ceil(float f)
	{
		return floor(f) + 1;
	}

	public static int round(double d)
	{
		return floor(d + 0.5d);
	}

	public static int round(float f)
	{
		return floor(f + 0.5f);
	}

	public static int roundToZero(float x)
	{
		return (int) x;
	}

	public static int pow(int i, int exp)
	{
		if (exp == 0)
		{
			return 1;
		}
		return i * pow(i, exp - 1);
	}

	/**
	 * Simplifies an angle, given in radians
	 * 
	 * @param rad
	 *            the angle
	 * @return same angle within the range <bb>]-PI, PI]</bb>
	 */
	public static float simplifyRadians(float rad)
	{
		while (rad <= -f_PI)
		{
			rad += f_2PI;
		}
		while (rad > f_PI)
		{
			rad -= f_2PI;
		}
		return rad;
	}

	/**
	 * Simplifies an angle, given in degrees
	 * 
	 * @param rad
	 *            the angle
	 * @return same angle within the range <bb>]-180, 180]</bb>
	 */
	public static float simplifyDegrees(float deg)
	{
		while (deg <= -180.0f)
		{
			deg += 360.0f;
		}
		while (deg > 180.0f)
		{
			deg -= 360.0f;
		}
		return deg;
	}

	/**
	 * Clamps a float
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float clamp(float value, float min, float max)
	{
		return value < min ? min : value > max ? max : value;
	}

	/**
	 * Clamps an int
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static int clamp(int value, int min, int max)
	{
		return value < min ? min : value > max ? max : value;
	}

	public static int getPowerOfTwoBiggerThan(int i)
	{
		int r = 1;
		while (r < i)
		{
			r <<= 1;
		}
		return r;
	}

	private static final int[][] EMPTY_MATRIX = new int[0][0];

	public static int[][] cropMatrix(int[][] matrix)
	{
		int minX = 10;
		int minY = 10;
		int maxX = 0;
		int maxY = 0;
		boolean containsData = false;

		for (int y = 0; y < matrix.length; ++y)
		{
			for (int x = 0; x < matrix[0].length; ++x)
			{
				int elem = matrix[y][x];
				if (elem != 0)
				{
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
					containsData = true;
				}
			}
		}
		if (!containsData)
		{
			return EMPTY_MATRIX;
		}

		int w = maxX - minX + 1;
		int h = maxY - minY + 1;

		int[][] ret = new int[h][w];

		for (int x = 0; x < w; ++x)
		{
			for (int y = 0; y < h; ++y)
			{
				ret[y][x] = matrix[y + minY][x + minX];
			}
		}

		return ret;
	}

	/**
	 * Linear interpolation.
	 */
	private static double lerp(double x, double x1, double x2, double q00, double q01)
	{
		return ((x2 - x) / (x2 - x1)) * q00 + ((x - x1) / (x2 - x1)) * q01;
	}

	/**
	 * Bilinear interpolation.
	 */
	public static double biLerp(double x, double y, double q11, double q12, double q21, double q22, double x1, double x2, double y1, double y2)
	{
		double r1 = lerp(x, x1, x2, q11, q21);
		double r2 = lerp(x, x1, x2, q12, q22);
		return lerp(y, y1, y2, r1, r2);
	}

	/**
	 * Trilinear interpolation.
	 */
	public static double triLerp(double x, double y, double z, double q000, double q001, double q010, double q011, double q100, double q101, double q110, double q111, double x1,
			double x2, double y1, double y2, double z1, double z2)
	{
		double x00 = lerp(x, x1, x2, q000, q100);
		double x10 = lerp(x, x1, x2, q010, q110);
		double x01 = lerp(x, x1, x2, q001, q101);
		double x11 = lerp(x, x1, x2, q011, q111);
		double r0 = lerp(y, y1, y2, x00, x01);
		double r1 = lerp(y, y1, y2, x10, x11);
		return lerp(z, z1, z2, r0, r1);
	}

	/**
	 * Maps a 2D value, made by two integers, to a unique 1D value.
	 * 
	 * @param k1
	 * @param k2
	 * @return the unique 1D value.
	 */
	public static int cantorize(int k1, int k2)
	{
		return ((k1 + k2) * (k1 + k2 + 1) / 2) + k2;
	}

	public static int cantorX(int c)
	{
		int j = (int) (Math.sqrt(0.25 + 2 * c) - 0.5);
		return j - cantorY(c);
	}

	private static int cantorY(int c)
	{
		int j = (int) (Math.sqrt(0.25 + 2 * c) - 0.5);
		return c - j * (j + 1) / 2;
	}

	/**
	 * Maps any given value to be positive only.
	 */
	public static int mapToPositive(int x)
	{
		if (x >= 0)
			return x << 1;

		return -(x << 1) - 1;
	}

	/**
	 * Recreates the original value after applying {@code mapToPositive}.
	 */
	public static int redoMapToPositive(int x)
	{
		if ((x & 1) == 0)
		{
			return x >> 1;
		}

		return -(x >> 1) - 1;
	}

	public static float roundDelta(float x, float delta)
	{
		float rounded = Math.round(x);
		float diff = Math.abs(x - rounded);
		if (diff < delta)
		{
			return rounded;
		}
		return x;
	}



}
