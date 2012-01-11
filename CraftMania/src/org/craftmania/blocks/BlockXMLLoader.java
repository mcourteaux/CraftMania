package org.craftmania.blocks;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.craftmania.Side;
import org.craftmania.items.ItemManager;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author martijncourteaux
 */
public class BlockXMLLoader
{

    public static void parseXML() throws Exception
    {
        File fXmlFile = new File("res/blocks.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        /* Get NodeList of all the blocks */
        NodeList blocksList = doc.getElementsByTagName("block");

        for (int i = 0; i < blocksList.getLength(); ++i)
        {
            Node node = blocksList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) node;
                BlockType blockType = parseBlockType(element);
                BlockManager.getInstance().addBlock(blockType);
            }
        }

        BlockManager.getInstance().load();
    }

    private static BlockType parseBlockType(Element element)
    {
        int id = Integer.parseInt(element.getAttribute("id"));
        String name = element.getAttribute("name");

//        System.out.println("Loading BlockType: " + name);

        /* BlockBrush */
        DefaultBlockBrush bb = new DefaultBlockBrush();
        Element brushElement = (Element) element.getElementsByTagName("brush").item(0);
        if (brushElement.hasAttribute("alphaBlending"))
        {
            bb.setAlphaBlending(Boolean.parseBoolean(brushElement.getAttribute("alphaBlending")));
            System.out.println("Enable alphaBlending for " + name);
        }
        NodeList brushSidesList = brushElement.getChildNodes();
        for (int i = 0; i < brushSidesList.getLength(); ++i)
        {
            if (brushSidesList.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                Element sideElement = (Element) brushSidesList.item(i);
                String sideName = sideElement.getTagName();
                Vec2f pos = parseVec2f(sideElement.getTextContent());
                Vec3f color = null;
                float inset = 0;
                if (sideElement.hasAttribute("color"))
                {
                    String colorStr = sideElement.getAttribute("color");
                    int colorInt = Integer.parseInt(colorStr.substring(1), 16);
                    int r = (colorInt >> 16) & 0xFF;
                    int g = (colorInt >> 8) & 0xFF;
                    int b = colorInt & 0xFF;

                    color = new Vec3f(r / 255f, g / 255f, b / 255f);
                }
                if (sideElement.hasAttribute("inset"))
                {
                    inset = Float.parseFloat(sideElement.getAttribute("inset"));
                }
                if (sideName.equals("allsides"))
                {
                    if (color != null)
                    {
                        bb.setGlobalColor(color);
                    }
                    bb.setInset(inset);
                    bb.setTexture(pos);
                } else if (sideName.equals("mantle"))
                {
                    if (color != null)
                    {
                        bb.setMantleColor(color);
                    }
                    bb.setInsetForMantle(inset);
                    bb.setTextureForMantle(pos);
                } else if (sideName.equals("top"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.TOP, color);
                    }
                    bb.setSideInset(Side.TOP, inset);
                    bb.setTextureForSize(pos, Side.TOP);
                } else if (sideName.equals("bottom"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.BOTTOM, color);
                    }
                    bb.setSideInset(Side.BOTTOM, inset);
                    bb.setTextureForSize(pos, Side.BOTTOM);
                } else if (sideName.equals("front"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.FRONT, color);
                    }
                    bb.setSideInset(Side.FRONT, inset);
                    bb.setTextureForSize(pos, Side.FRONT);
                } else if (sideName.equals("back"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.BACK, color);
                    }
                    bb.setSideInset(Side.BACK, inset);
                    bb.setTextureForSize(pos, Side.BACK);
                } else if (sideName.equals("left"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.LEFT, color);
                    }
                    bb.setSideInset(Side.LEFT, inset);
                    bb.setTextureForSize(pos, Side.LEFT);
                } else if (sideName.equals("right"))
                {
                    if (color != null)
                    {
                        bb.setSideColor(Side.RIGHT, color);
                    }
                    bb.setSideInset(Side.RIGHT, inset);
                    bb.setTextureForSize(pos, Side.RIGHT);
                }
            }
        }
        /* Load the brush into the brushstorage */
        BlockBrushStorage.loadBrush(name, bb);

        /* Custom settings */
        BlockType blockType = new BlockType(id, name);
        BlockManager bm = BlockManager.getInstance();

        NodeList settingsList = element.getChildNodes();
        for (int i = 0; i < settingsList.getLength(); ++i)
        {
            if (settingsList.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                Element settingsElement = (Element) settingsList.item(i);
                String settingName = settingsElement.getTagName();
                String valueStr = settingsElement.getTextContent();
                if (settingName.equals("brush"))
                {
                    continue;
                }

                if (settingName.equals("mineResult"))
                {
                    if (valueStr.equals("null"))
                    {
                        bm.setBlockTypeSetting(blockType, settingName, 0);
                    } else
                    {
                        bm.setBlockTypeSetting(blockType, settingName, ItemManager.getInstance().getItemID(valueStr));
                    }
                    continue;
                }

                Class<?> settingsType = getSettingsType(blockType, settingName);
                if (settingsType == null)
                {
//                    System.out.println("Unsupported BlockType settings: " + settingName);
                    continue;
                }

//                System.out.println("Setting Type: " + settingsType.getName());

                Object value = null;

                if (settingsType == boolean.class)
                {
                    value = Boolean.parseBoolean(valueStr);
                } else if (settingsType == int.class)
                {
                    value = Integer.parseInt(valueStr);
                } else if (settingsType == BlockType.BlockClass.class)
                {
                    value = BlockType.BlockClass.valueOf(valueStr);
                } else if (settingsType == String.class)
                {
                    value = valueStr;
                }

                bm.setBlockTypeSetting(blockType, settingName, value);
            }
        }

        return blockType;
    }

    private static Class<?> getSettingsType(BlockType type, String name)
    {
        try
        {
            return type.getClass().getDeclaredField(name).getType();
        } catch (Exception ex)
        {
            return null;
        }
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
}
