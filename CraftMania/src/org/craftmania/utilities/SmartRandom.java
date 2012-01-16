package org.craftmania.utilities;

import java.util.Random;


/**
 *
 * @author martijncourteaux
 */
public class SmartRandom
{
    private Random _random;

    public SmartRandom(Random _random)
    {
        this._random = _random;
    }
    
    public int randomInt(int lower, int upper)
    {
        return lower + _random.nextInt(upper - lower);
    }
    
    public float randomFloat(float lower, float upper)
    {
        return lower + _random.nextFloat() * (upper - lower);
    }
    
    public int randomInt(int upper)
    {
        return _random.nextInt(upper);
    }
    
    public float randomFloat(float upper)
    {
        return _random.nextFloat() * upper;
    }

    public boolean randomBoolean()
    {
        return _random.nextBoolean();
    }
    
    public float exponentialRandom(float upper, int exponent)
    {
        float ret = 1.0f;
        for (int i = 0; i < exponent; ++i)
        {
            ret *= randomFloat(1.0f);
        }
        return ret * upper;
    }

	public long randomLong()
	{
		return _random.nextLong();
	}
}
