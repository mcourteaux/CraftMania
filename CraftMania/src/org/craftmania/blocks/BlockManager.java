package org.craftmania.blocks;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
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
		if (__instance == null)
		{
			synchronized (__lock)
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
				bt.getBrush().generateDisplayListForEachFace();
				bt.getBrush().generateDisplayList();
				_typeStrings.put(bt.getType(), Byte.valueOf((byte) i));

				BufferedImage img = inventoryImageCreator.createInventoryImage(bt);

				Texture inventoryTexture = TextureStorage.loadTexture(bt.getType(), img);

				bt.setInventoryTexture(inventoryTexture);
			}
		}

		System.out.println(_typeStrings);
		System.out.println(_blockTypes);
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
}
