package org.craftmania.items;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.craftmania.game.TextureStorage;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec2i;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author martijncourteaux
 */
public class ItemXMLLoader
{

	public static void parseXML() throws Exception
	{
		File xmlFile = new File("res/items.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();

		/* Get NodeList of all the blocks */
		NodeList blocksList = doc.getElementsByTagName("item");

		for (int i = 0; i < blocksList.getLength(); ++i)
		{
			Node node = blocksList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				try
				{
					Element element = (Element) node;
					InventoryItem item = parseItem(element);
					ItemManager.getInstance().putInventoryItem(item.getInventoryTypeID(), item);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	private static InventoryItem parseItem(Element element) throws Exception
	{
		String name = element.getAttribute("name");

		String className = "minecraft.world.items.GenericItem";

		if (element.hasAttribute("class"))
		{
			className = element.getAttribute("class");
		}

		if (!className.equals("minecraft.world.items.GenericItem"))
		{
			@SuppressWarnings("unchecked")
			Class<? extends InventoryItem> cl = (Class<? extends InventoryItem>) Class.forName(className);
			return cl.newInstance();
		}

		float animationSpeed = 8.0f;
		boolean stackable = false;
		Vec2i tex = null;

		NodeList settingsList = element.getChildNodes();
		for (int i = 0; i < settingsList.getLength(); ++i)
		{
			if (settingsList.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				Element settingsElement = (Element) settingsList.item(i);
				String settingName = settingsElement.getTagName();
				String valueStr = settingsElement.getTextContent();

				if (settingName.equals("stackable"))
				{
					stackable = Boolean.parseBoolean(valueStr);
				} else if (settingName.equals("animationSpeed"))
				{
					animationSpeed = Float.parseFloat(valueStr);
				} else if (settingName.equals("texture"))
				{
					tex = parseVec2i(valueStr);
				}
			}
		}

		return new GenericItem(name, animationSpeed, stackable, TextureStorage.getTexture("items"), tex);

	}

	private static Vec2f parseVec2f(String str)
	{
		float x = 0.0f;
		float y = 0.0f;
		int indexOfComa = str.indexOf(",");
		if (indexOfComa == -1)
		{
			throw new IllegalArgumentException("For string input: " + str);
		}
		try
		{
			x = Float.parseFloat(str.substring(0, indexOfComa));
			y = Float.parseFloat(str.substring(indexOfComa + 1));
		} catch (Exception e)
		{
			throw new IllegalArgumentException("Cannot parse Vec2f", e);
		}

		return new Vec2f(x, y);
	}

	private static Vec2i parseVec2i(String str)
	{
		int x = 0;
		int y = 0;
		int indexOfComa = str.indexOf(",");
		if (indexOfComa == -1)
		{
			throw new IllegalArgumentException("For string input: " + str);
		}
		try
		{
			x = Integer.parseInt(str.substring(0, indexOfComa));
			y = Integer.parseInt(str.substring(indexOfComa + 1));
		} catch (Exception e)
		{
			throw new IllegalArgumentException("Cannot parse Vec2f", e);
		}

		return new Vec2i(x, y);
	}
}
