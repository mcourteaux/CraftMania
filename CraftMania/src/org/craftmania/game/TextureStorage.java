package org.craftmania.game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.BufferedImageUtil;

/**
 * 
 * @author martijncourteaux
 */
public class TextureStorage
{
	private static Map<String, Texture> map = new HashMap<String, Texture>();
	// private static String _texturePack = "QuantumCraft [3.1]";
	// private static String _texturePack = "CUBISM1.00";
	private static String _texturePack = "GoodMorningCraftv3.2";
	private static String _fallBackTexturePack = "CUBISM1.00";

	static void setTexturePack(String texturePack)
	{
		_texturePack = texturePack;
	}

	public TextureStorage()
	{
	}

	public static void release()
	{
		for (Entry<String, Texture> item : map.entrySet())
		{
			System.out.printf("Releasing '%s'%n", item.getKey());
			item.getValue().release();
		}
	}

	public static Texture loadTexture(String id, String format, InputStream in) throws IOException
	{
		Texture t = TextureLoader.getTexture(format, in, GL11.GL_NEAREST);
		map.put(id, t);
		return t;
	}

	public static Texture loadTexture(String id, BufferedImage bi) throws IOException
	{
		Texture t = BufferedImageUtil.getTexture(id, bi, GL11.GL_TEXTURE_2D, // target
				GL11.GL_RGBA8, // dest pixel format
				GL11.GL_NEAREST, // min filter (unused)
				GL11.GL_NEAREST);
		map.put(id, t);
		return t;
	}

	public static Texture loadTexture(String id, String format, String resource) throws IOException
	{
		File res = new File("res/textures/" + _texturePack + "/" + resource);
		if (!res.exists())
		{
			res = new File("res/textures/" + _fallBackTexturePack + "/" + resource);
		}
		return loadTexture(id, format, new FileInputStream(res));
	}

	public static Texture getTexture(String id)
	{
		Texture text = map.get(id);
		return text;
	}

}
