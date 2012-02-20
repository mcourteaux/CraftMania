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
