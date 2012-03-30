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
package org.craftmania.blocks;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.craftmania.game.TextureStorage;
import org.craftmania.inventory.InventoryImageCreator;
import org.newdawn.slick.opengl.Texture;

public class BlockManager
{

	private static BlockManager __instance;
	private static final Object __lock = new Object();

	private BlockManager() throws IOException
	{
		_blockTypes = new BlockType[265];
		_typeStrings = new HashMap<String, Byte>();
	}

	public static BlockManager getInstance()
	{
		synchronized (__lock)
		{
			if (__instance == null)
			{
				try
				{
					__instance = new BlockManager();
				} catch (Exception e)
				{
					e.printStackTrace(System.err);
					System.exit(-1);
				}
			}
		}
		return __instance;
	}

	private final BlockType[] _blockTypes;
	private final Map<String, Byte> _typeStrings;

	public void load() throws IOException
	{
		InventoryImageCreator inventoryImageCreator = new InventoryImageCreator(64);
		for (int i = 0; i < _blockTypes.length; ++i)
		{
			BlockType bt = _blockTypes[i];

			if (bt != null)
			{
				/*
				 * Brush might be null like in case of Tall Grass, where there
				 * are six block brushes. Initialized by the class itself
				 */
				if (bt.getBrush() != null)
				{
					bt.getBrush().create();
				}

				/* Make sure all static initializer bodies are called */
				if (bt.getCustomClass() != null)
				{
					try
					{
						System.out.println("Loading static content for " + bt.getCustomClass());
						Class.forName(bt.getCustomClass());
					} catch (Exception e)
					{
						System.err.println("Custom Block class is not found!");
						e.printStackTrace();
					}
				}
				_typeStrings.put(bt.getType(), Byte.valueOf((byte) i));

				if (bt.getCustomInventoryImage() != null)
				{
					bt.setInventoryTexture(TextureStorage.getTexture(bt.getCustomInventoryImageTexture()));
				} else
				{
					BufferedImage img = inventoryImageCreator.createInventoryImage(bt);
					Texture inventoryTexture = TextureStorage.loadTexture(bt.getType(), img);
					bt.setInventoryTexture(inventoryTexture);
				}
			}
		}

		System.out.println(_typeStrings);
		System.out.println(Arrays.toString(_blockTypes));
	}

	public void release()
	{
		for (int i = 0; i < _blockTypes.length; ++i)
		{
			BlockType bt = _blockTypes[i];

			if (bt != null)
			{
				/*
				 * Brush might be null like in case of Tall Grass, where there
				 * are six block brushes. Initialized by the class itself
				 */
				if (bt.getBrush() != null)
				{
					bt.getBrush().create();
				}

				/* Make sure all static initializer bodies are called */
				if (bt.getCustomClass() != null)
				{
					try
					{
						Class<?> clazz = Class.forName(bt.getCustomClass());
						Method[] methods = clazz.getDeclaredMethods();

						for (int m = 0; m < methods.length; ++m)
						{
							Method method = methods[m];
							if (method.getName().equals("RELEASE_STATIC_CONTENT"))
							{
								System.out.println("Releasing static content from " + clazz.getName());
								method.invoke(null);
							}
						}
					} catch (Exception e)
					{
						System.err.println("Custom Block class is not found!");
						e.printStackTrace();
					}
				}
			}
		}
	}

	public byte blockID(String id)
	{
		Byte bt = _typeStrings.get(id);
		if (bt == null)
		{
			System.out.println("BlockType not found! " + id);
			return -1;
		}
		return bt;
	}

	public BlockType getBlockType(byte type)
	{
		return _blockTypes[type];
	}

	public void setBlockTypeSetting(BlockType bt, String setting, Object value)
	{
		try
		{
			Class<? extends BlockType> c = bt.getClass();
			Field f = c.getDeclaredField(setting);
			f.setAccessible(true);
			f.set(bt, value);
		} catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	public void addBlock(BlockType blockType)
	{
		_blockTypes[blockType.getID()] = blockType;
		_typeStrings.put(blockType.getType(), blockType.getID());
	}

	public BlockType getBlockType(String string)
	{
		return getBlockType(blockID(string));
	}
}
