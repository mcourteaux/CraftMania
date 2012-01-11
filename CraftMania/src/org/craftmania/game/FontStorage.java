package org.craftmania.game;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.craftmania.rendering.GLFont;

/**
 * 
 * @author martijncourteaux
 */
public class FontStorage
{

	private static Map<String, GLFont> map = new HashMap<String, GLFont>();

	public FontStorage()
	{
	}

	public static void release()
	{
		for (Entry<String, GLFont> item : map.entrySet())
		{
			System.out.printf("Releasing '%s'%n", item.getKey());
			item.getValue().destroyFont();
		}
	}

	public static GLFont loadFont(String id, InputStream in, float size) throws IOException
	{
		try
		{
			GLFont font = new GLFont(Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(size));
			map.put(id, font);
			return font;
		} catch (FontFormatException ex)
		{
			Logger.getLogger(FontStorage.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static GLFont loadFont(String id, String resource, float size) throws IOException
	{
		File res = new File("res/fonts/" + resource);
		return loadFont(id, new FileInputStream(res), size);
	}

	public static GLFont getFont(String id)
	{
		GLFont text = map.get(id);
		return text;
	}
}
