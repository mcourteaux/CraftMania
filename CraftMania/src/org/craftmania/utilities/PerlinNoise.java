package org.craftmania.utilities;

import java.util.Arrays;
import java.util.Random;

import org.craftmania.math.MathHelper;

public final class PerlinNoise
{

	public PerlinNoise(long seed)
	{
		Random rand = new Random(seed);

		p = new int[512];
		for (int i = 0; i < 256; ++i)
		{
			p[i] = i;
		}
		shuffleArray(p, 0, 256, new SmartRandom(rand));
		for (int i = 0; i < 256; ++i)
			p[256 + i] = p[i];

		System.out.println("Perlin Permutations = " + Arrays.toString(p));
	}

	public float noise(float x, float y)
	{
		int flX = MathHelper.floor(x);
		int flY = MathHelper.floor(y);

		int X = flX & 255, Y = flY & 255, Z = 0;
		x -= flX;
		y -= flY;
		float u = fade(x), v = fade(y);
		int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z,

		B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;

		return lerp(0, lerp(v, lerp(u, grad(p[AA], x, y, 0), grad(p[BA], x - 1, y, 0)), lerp(u, grad(p[AB], x, y - 1, 0), grad(p[BB], x - 1, y - 1, 0))),
				lerp(v, lerp(u, grad(p[AA + 1], x, y, -1), grad(p[BA + 1], x - 1, y, -1)), lerp(u, grad(p[AB + 1], x, y - 1, -1), grad(p[BB + 1], x - 1, y - 1, -1))));

	}

	public float noise(float x, float y, float z)
	{
		int flX = MathHelper.floor(x);
		int flY = MathHelper.floor(y);
		int flZ = MathHelper.floor(z);

		int X = flX & 255, Y = flY & 255, Z = flZ & 255;
		x -= flX;
		y -= flY;
		z -= flZ;
		float u = fade(x), v = fade(y), w = fade(z);
		int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z,

		B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;

		return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z)), lerp(u, grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z))),
				lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1)), lerp(u, grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1))));
	}

	static float fade(float t)
	{
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	static float lerp(float t, float a, float b)
	{
		return a + t * (b - a);
	}

	static float grad(int hash, float x, float y, float z)
	{
		int h = hash & 15;
		float u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}

	private static void shuffleArray(int[] array, int lower, int upper, SmartRandom random)
	{
		for (int i = lower; i < upper; ++i)
		{
			int randomIndex = random.randomInt(lower, upper);
			int temp = array[i];
			array[i] = array[randomIndex];
			array[randomIndex] = temp;
		}
	}

	private int p[] = new int[512];

}