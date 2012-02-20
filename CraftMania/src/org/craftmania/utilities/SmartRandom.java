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
