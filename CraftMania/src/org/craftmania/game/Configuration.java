package org.craftmania.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
*
* @author martijncourteaux
*/
public class Configuration
{

   /* Predefined viewing distances */
   public static final float VIEWING_DISTANCE_ULTRA = 120.0f;
   public static final float VIEWING_DISTANCE_FAR = 90.0f;
   public static final float VIEWING_DISTANCE_NORMAL = 60.0f;
   public static final float VIEWING_DISTANCE_SHORT = 30.0f;
   public static final float VIEWING_DISTANCE_TINY = 15.0f;
   
   /* Configurations */
   private float _viewingDistance;
   private float _maxPlayerEditingDistance;
   private int _width;
   private int _height;
   private boolean _fullscreen;
   private int _fps;
   private boolean _vsync;
   private boolean _updateVisibleOnly;
   private String _texturePack;

   public Configuration()
   {
   }

   public int getWidth()
   {
       return _width;
   }

   public int getHeight()
   {
       return _height;
   }

   public boolean isFullscreen()
   {
       return _fullscreen;
   }

   public void setViewingDistance(float viewingDistance)
   {
       this._viewingDistance = viewingDistance;
   }

   public float getViewingDistance()
   {
       return _viewingDistance;
   }

   public void setDisplaySettings(int w, int h, boolean fullscreen)
   {
       this._width = w;
       this._height = h;
       this._fullscreen = fullscreen;
   }

   public void setMaximumPlayerEditingDistance(float maxPlayerEditingDistance)
   {
       this._maxPlayerEditingDistance = maxPlayerEditingDistance;
   }

   public float getMaximumPlayerEditingDistance()
   {
       return _maxPlayerEditingDistance;
   }

   public int getFPS()
   {
       return _fps;
   }

   public void setFPS(int _fps)
   {
       this._fps = _fps;
   }

   public boolean getVSync()
   {
       return _vsync;
   }

   public boolean getUpdateVisibleOnly()
   {
       return _updateVisibleOnly;
   }

   public String getTexturePack()
   {
       return _texturePack;
   }
   
   

   public void loadFromFile(String string) throws IOException
   {
       File f = new File(string);

       BufferedReader br = new BufferedReader(new FileReader(f));

       String line;
       while ((line = br.readLine()) != null)
       {
           if (line.trim().isEmpty() || line.trim().startsWith("#"))
           {
               continue;
           }
           String[] prop = line.split("=");
           String p = prop[0];
           String v = prop[1];

           if (p.equals("fullscreen"))
           {
               _fullscreen = Boolean.parseBoolean(v);
           } else if (p.equals("width"))
           {
               _width = Integer.parseInt(v);
           } else if (p.equals("height"))
           {
               _height = Integer.parseInt(v);
           } else if (p.equals("vsync"))
           {
               _vsync = Boolean.parseBoolean(v);
           } else if (p.equals("update_visible_only"))
           {
               _updateVisibleOnly = Boolean.parseBoolean(v);
           } else if (p.equals("fps"))
           {
               _fps = Integer.parseInt(v);
           } else if (p.equals("viewing_distance"))
           {
               _viewingDistance = Float.parseFloat(v);
           } else if (p.equals("texture_pack"))
           {
               _texturePack = v;
           }

       }


   }
}
