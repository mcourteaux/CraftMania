package org.craftmania.game;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FOG;
import static org.lwjgl.opengl.GL11.GL_FOG_COLOR;
import static org.lwjgl.opengl.GL11.GL_FOG_END;
import static org.lwjgl.opengl.GL11.GL_FOG_HINT;
import static org.lwjgl.opengl.GL11.GL_FOG_MODE;
import static org.lwjgl.opengl.GL11.GL_FOG_START;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PERSPECTIVE_CORRECTION_HINT;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFog;
import static org.lwjgl.opengl.GL11.glFogf;
import static org.lwjgl.opengl.GL11.glFogi;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.Utilities;

import org.craftmania.blocks.BlockXMLLoader;
import org.craftmania.items.ItemXMLLoader;
import org.craftmania.math.MathHelper;
import org.craftmania.recipes.Recipe;
import org.craftmania.recipes.RecipeManager;
import org.craftmania.rendering.GLFont;
import org.craftmania.rendering.GLUtils;
import org.craftmania.world.World;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.NVFogDistance;

public class Game
{
	private World _world;
	private int _fps;
	private float _step;
	private int _sleepTimeMillis;
	private Configuration _configuration;
	private static Game __instance;

	public static Game getInstance()
	{
		if (__instance == null)
		{
			__instance = new Game();
		}
		return __instance;
	}

	/**
	 * Set the display mode to be used
	 * 
	 * @param width
	 *            The width of the display required
	 * @param height
	 *            The height of the display required
	 * @param fullscreen
	 *            True if we want fullscreen mode
	 */
	public void setDisplayMode(int width, int height, boolean fullscreen)
	{

		// return if requested DisplayMode is already set
		if ((Display.getDisplayMode().getWidth() == width) && (Display.getDisplayMode().getHeight() == height) && (Display.isFullscreen() == fullscreen))
		{
			return;
		}

		try
		{
			DisplayMode targetDisplayMode = null;

			if (fullscreen)
			{
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++)
				{
					DisplayMode current = modes[i];

					if ((current.getWidth() == width) && (current.getHeight() == height))
					{
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq))
						{
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel()))
							{
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against
						// the
						// original display mode then it's probably best to go
						// for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
								&& (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency()))
						{
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else
			{
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null)
			{
				System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);

		} catch (LWJGLException e)
		{
			System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
		}
	}

	public void init() throws IOException
	{
		loadConfiguration();
		this._fps = _configuration.getFPS();
		try
		{
			Display.setTitle("MineCraft");
			setDisplayMode(_configuration.getWidth(), _configuration.getHeight(), _configuration.isFullscreen());

			Display.create();
		} catch (LWJGLException e)
		{
			e.printStackTrace();
			System.exit(0);
		}

		initOpenGL();
		loadTextures();
		loadFonts();
		loadItems();
		loadBlocks();
		loadRecipes();
		Mouse.setGrabbed(true);
	}

	public void initOpenGL() throws IOException
	{
		// init OpenGL
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 600, 0, 1, 300);
		glMatrixMode(GL_MODELVIEW);

		float color = 0.9f;

		glClearColor(color, color, color, color);
		glEnable(GL_TEXTURE_2D);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_ALWAYS);

		glEnable(GL_CULL_FACE);

		glEnable(GL_FOG);
		glFog(GL_FOG_COLOR, GLUtils.wrapDirect(color, color, color, 1.0f));
		glFogi(GL_FOG_MODE, GL_LINEAR);
		glFogf(GL_FOG_START, _configuration.getViewingDistance() * 0.65f);
		glFogf(GL_FOG_END, _configuration.getViewingDistance());
		glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV);
		glHint(GL_FOG_HINT, GL_NICEST);

	}

	public float getFPS()
	{
		return _fps;
	}

	public float getStep()
	{
		return _step;
	}

	public void update()
	{
		if (_world != null)
		{
			_world.update();
		}
	}

	public void render()
	{
		// Clear the screen and depth buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if (_world != null)
		{
			_world.render();
		}
		
		renderOnScreenInfo();
	}

	private void renderOnScreenInfo()
	{
		GLFont infoFont = FontStorage.getFont("Monospaced_20");
		
		/* Top Left Info */
		infoFont.print(4, _configuration.getHeight() - 20, "FPS:      " + Game.getInstance().getFPS());
		infoFont.print(4, _configuration.getHeight() - 20 - 15, "Sleeping: " + String.format("%4d", Game.getInstance().getSleepTime()));
		infoFont.print(4, _configuration.getHeight() - 20 - 30, "Heap Size: " + MathHelper.bytesToMagaBytes(Runtime.getRuntime().maxMemory()) + " MB");
		infoFont.print(4, _configuration.getHeight() - 20 - 45, "Heap Use:  " + MathHelper.bytesToMagaBytes(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) + " MB");
	}

	public void initOverlayRendering()
	{

		Configuration conf = Game.getInstance().getConfiguration();

		glDisable(GL_FOG);

		glClear(GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, conf.getWidth(), 0, conf.getHeight(), -100, 100);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glEnable(GL_COLOR_MATERIAL);
		glEnable(GL_ALPHA_TEST);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
	}

	public void initSceneRendering()
	{

		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		// glDepthFunc(GL_ALWAYS);
		glEnable(GL_CULL_FACE);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glEnable(GL_FOG);
	}

	public void renderTransculentOverlayLayer()
	{
		glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_QUADS);
		glVertex2i(0, 0);
		glVertex2i(_configuration.getWidth(), 0);
		glVertex2i(_configuration.getWidth(), _configuration.getHeight());
		glVertex2i(0, _configuration.getWidth());
		glEnd();
	}

	public void startGameLoop()
	{
		if (_configuration.getVSync())
		{
			Display.setVSyncEnabled(true);
		}
		Thread.currentThread().setPriority(9);
		while (!Display.isCloseRequested())
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) && Keyboard.isKeyDown(Keyboard.KEY_RETURN))
			{
			}
			long startTiming = System.nanoTime();
			_step = 1.0f / _fps;
			update();
			render();
			Display.update();
			long stopTiming = System.nanoTime();

			long frameTimeNanos = (stopTiming - startTiming);
			long desiredFrameTimeNanos = 1000000000L / _configuration.getFPS();

			long diff = desiredFrameTimeNanos - frameTimeNanos;
			if (frameTimeNanos < desiredFrameTimeNanos)
			{
				try
				{
					_sleepTimeMillis = (int) (diff / 1000000L);
					Thread.sleep(_sleepTimeMillis, (int) (diff % 1000000L));
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				_fps = _configuration.getFPS();
			} else
			{
				_fps = (int) (1000000000.0f / frameTimeNanos);
				_sleepTimeMillis = 0;
			}

		}

		TextureStorage.release();
		FontStorage.release();
		Display.destroy();
	}

	private void loadTextures() throws IOException
	{
		TextureStorage.setTexturePack(_configuration.getTexturePack());
		TextureStorage.loadTexture("terrain", "PNG", "terrain.png");
		TextureStorage.loadTexture("items", "PNG", "items.png");
		TextureStorage.loadTexture("gui.inventory", "PNG", "gui/inventory.png");
		TextureStorage.loadTexture("gui.crafting", "PNG", "gui/crafting.png");
	}

	private void loadFonts() throws IOException
	{
		FontStorage.loadFont("Monospaced_20", "novamono.ttf", 22);
		FontStorage.loadFont("InventoryAmount", "visitor1.ttf", 14);
	}

	private void loadRecipes()
	{
		RecipeManager.getInstance().addRecipe(new Recipe("wood0", "planks", 4));
		RecipeManager.getInstance().addRecipe(new Recipe("planks,planks;planks,planks", "crafting_table", 1));
		RecipeManager.getInstance().addRecipe(new Recipe("planks;planks", "stick", 4));
		RecipeManager.getInstance().addRecipe(new Recipe("coal;stick", "torch", 4));

		/* Shovels */
		RecipeManager.getInstance().addRecipe(new Recipe("stone;stick;stick", "stone_shovel", 1));
		RecipeManager.getInstance().addRecipe(new Recipe("planks;stick;stick", "wooden_shovel", 1));

		/* Pickaxe */
		RecipeManager.getInstance().addRecipe(new Recipe("planks,planks,planks;,stick;,stick", "wooden_pickaxe", 1));
		RecipeManager.getInstance().addRecipe(new Recipe("stone,stone,stone;,stick;,stick", "stone_pickaxe", 1));

		/* Axes */
		RecipeManager.getInstance().addRecipe(new Recipe("planks,planks;planks,stick;,stick", "wooden_axe", 1));
		RecipeManager.getInstance().addRecipe(new Recipe("stone,stone;stone,stick;,stick", "stone_axe", 1));
	}

	private void loadItems()
	{
		try
		{
			ItemXMLLoader.parseXML();
		} catch (Exception ex)
		{
			Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void loadBlocks()
	{
		try
		{
			BlockXMLLoader.parseXML();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setWorld(World world)
	{
		this._world = world;
	}

	public World getWorld()
	{
		return this._world;
	}

	private void loadConfiguration() throws IOException
	{
		this._configuration = new Configuration();
		this._configuration.loadFromFile("conf/conf.txt");
		this._configuration.setMaximumPlayerEditingDistance(5.0f);
	}

	public Configuration getConfiguration()
	{
		return _configuration;
	}

	/**
	 * Returns the sleeptime in milliseconds for the last frame.
	 * 
	 * @return
	 */
	public int getSleepTime()
	{
		return _sleepTimeMillis;
	}
}
