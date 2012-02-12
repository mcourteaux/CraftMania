package org.craftmania.world;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.craftmania.blocks.Block;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.ViewFrustum;
import org.craftmania.game.Configuration;
import org.craftmania.game.FontStorage;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.inventory.Inventory;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.craftmania.rendering.GLFont;
import org.craftmania.rendering.GLUtils;
import org.craftmania.utilities.FastArrayList;
import org.craftmania.utilities.IntList;
import org.craftmania.utilities.MultiTimer;
import org.craftmania.world.characters.Player;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class World
{

	private static final float SECONDS_IN_DAY = 3600f * 24f;

	private WorldProvider _worldProvider;
	private ChunkManager _chunkManager;
	private Player _player;
	private Sky _sky;

	private List<Chunk> _localChunks;
	private List<Chunk> _oldChunkList;
	private List<Chunk> _chunksToBeSetDirty;

	private FastArrayList<Chunk> _visibleChunks;
	private Inventory _activatedInventory;
	private int _localBlockCount;
	private int _updatingBlocks;
	private long _worldSeed;
	private String _worldName;
	private float _time;
	private int _tick;
	private Vec3f _fogColor;
	private float _sunlight;

	private AABB _chunkVisibilityTestingAABB;
	private ChunkDistanceComparator _chunkDistanceComparator;

	public World(String name, long seed) throws Exception
	{
		_worldName = name;
		_worldSeed = seed;
		_worldProvider = new DefaultWorldProvider(this);
		_sky = new Sky();
		_chunkManager = new ChunkManager(this);
		_localChunks = new ArrayList<Chunk>();
		_oldChunkList = new ArrayList<Chunk>();
		_chunksToBeSetDirty = new ArrayList<Chunk>();
		_visibleChunks = new FastArrayList<Chunk>(30);
		_chunkVisibilityTestingAABB = new AABB(new Vec3f(), new Vec3f());
		_chunkDistanceComparator = new ChunkDistanceComparator();
		_fogColor = new Vec3f();

		_time = SECONDS_IN_DAY * 0.5f;
	}

	public void save() throws Exception
	{
		_worldProvider.save();

		/* Make sure the BlockChunkLoader is free. */
		int i = 0;
		do
		{
			++i;
			try
			{
				Thread.sleep(10);
			} catch (Exception e)
			{
			}
		} while (_chunkManager.isBlockChunkThreadingBusy() && i < 300);

		/* Save the local chunks, by destroying them */
		for (Chunk chunk : _localChunks)
		{
			_chunkManager.saveAndUnloadChunk(chunk, false);
		}
		_localChunks.clear();
	}

	public void render()
	{
		/* Prepare Matrixes */
		Game.getInstance().initSceneRendering();

		/* Look through the camera */
		_player.getFirstPersonCamera().lookThrough();

		/* Set the fog color based on time */
		_fogColor.set(Game.getInstance().getConfiguration().getFogColor());
		_fogColor.scale(_sunlight / 15.001f);
		GL11.glFog(GL11.GL_FOG_COLOR, GLUtils.wrapDirect(_fogColor.x(), _fogColor.y(), _fogColor.z(), 1.0f));
		GL11.glClearColor(_fogColor.x(), _fogColor.y(), _fogColor.z(), 1.0f);

		_sky.render();

		/* Select the visible blocks */
		selectVisibleChunks(_player.getFirstPersonCamera().getViewFrustum());

		/* Bind the terrain texture */
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		TextureStorage.getTexture("terrain").bind();

		MultiTimer timer = new MultiTimer(_visibleChunks.size());
		int i = 0;
		for (Chunk ch : _visibleChunks)
		{
			timer.start(i);
			ch.render();
			timer.stop(i);
			i++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_L))
		{
			System.out.println();

			for (int j = 0; j < timer.getTimerCount(); ++j)
			{
				System.out.println("RT: " + _visibleChunks.get(j).toString() + ": " + timer.get(j));
			}

			System.out.println();
		}

		_player.render();

		if (Game.RENDER_OVERLAY)
			renderOverlay();
	}

	private void renderOverlay()
	{
		Configuration conf = Game.getInstance().getConfiguration();

		Game.getInstance().initOverlayRendering();

		glColor3f(1, 1, 1);
		GLFont infoFont = FontStorage.getFont("Monospaced_20");

		/* Down Left Info */
		infoFont.print(4, 4, "CraftMania");
		infoFont.print(4, 30, _player.coordinatesToString());
		infoFont.print(4, 45, "Visible Chunks:      " + _visibleChunks.size());
		infoFont.print(4, 60, "Updading Blocks:     " + _updatingBlocks);
		infoFont.print(4, 75, "Total Chunks in RAM: " + _chunkManager.getTotalBlockChunkCount());
		infoFont.print(4, 90, "Local Chunks:        " + _localChunks.size());
		infoFont.print(4, 105, "Total Local Blocks:  " + _localBlockCount);
		infoFont.print(4, 120, "Time:  " + _time);
		infoFont.print(4, 135, "Sunlight:  " + _sunlight);

		/** RENDER **/
		if (_activatedInventory != null)
		{
			Game.getInstance().renderTransculentOverlayLayer();
			_activatedInventory.renderInventory();
		} else
		{
			// Center Cross
			{
				int width = conf.getWidth();
				int height = conf.getHeight();
				int crossSize = 10;
				int crossHole = 5;

				glDisable(GL_TEXTURE_2D);
				glLineWidth(2.5f);

				glColor3f(0, 0, 0);
				glBegin(GL_LINES);
				glVertex3f(width / 2 - crossSize - crossHole, height / 2, 0);
				glVertex3f(width / 2 - crossHole, height / 2, 0);

				glVertex3f(width / 2 + crossSize + crossHole, height / 2, 0);
				glVertex3f(width / 2 + crossHole, height / 2, 0);

				glVertex3f(width / 2, height / 2 - crossSize - crossHole, 0);
				glVertex3f(width / 2, height / 2 - crossHole, 0);

				glVertex3f(width / 2, height / 2 + crossSize + crossHole, 0);
				glVertex3f(width / 2, height / 2 + crossHole, 0);

				glEnd();
				glEnable(GL_TEXTURE_2D);
			}
		}
	}

	public void update()
	{
		/*
		 * Do not update the game if it goes very slow, otherwise floats might
		 * become Infinite and NaN
		 */
		if (Game.getInstance().getFPS() < 5)
			return;

		_time += Game.getInstance().getStep() * 100;
		_tick = MathHelper.floor(_time / 100);

		float todNew = MathHelper.simplify(_time, SECONDS_IN_DAY) / SECONDS_IN_DAY;

		int oldSunlight = MathHelper.round(_sunlight);

		_sunlight = -MathHelper.cos(todNew * MathHelper.f_2PI) * 0.5f + 0.5f;
		_sunlight = Math.max(0.2f, _sunlight);
		_sunlight *= 15.0f;

		if (oldSunlight != MathHelper.round(_sunlight))
		{
			/* Update chunk lights */
			for (Chunk c : _localChunks)
			{
				_chunksToBeSetDirty.add(c);
			}
		}

		_sky.update();

		while (Keyboard.next())
		{
			if (Keyboard.getEventKey() == Keyboard.KEY_E && Keyboard.getEventKeyState())
			{
				if (_activatedInventory != null)
				{
					setActivatedInventory(null);
				} else
				{
					setActivatedInventory(_player.getInventory());
				}
			} else if (Keyboard.getEventKey() == Keyboard.KEY_F && Keyboard.getEventKeyState())
			{
				_player.toggleFlying();
			} else if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState())
			{
				System.out.println("Rebuiling Visibility Buffers...");
				for (Chunk c : _localChunks)
				{
					System.out.print(c.getVisibleBlocks().size() + " --> ");
					c.rebuildVisibilityBuffer();
					System.out.println(c.getVisibleBlocks().size());
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
					{
						c.unsetNetVBONeeded();
					}
				}
				System.out.println();
			} else if (Keyboard.getEventKey() == Keyboard.KEY_O && Keyboard.getEventKeyState())
			{
				Game.RENDER_OVERLAY = !Game.RENDER_OVERLAY;
			} else if (Keyboard.getEventKey() == Keyboard.KEY_A && Keyboard.getEventKeyState())
			{
				_player.spreadLight();
			} else if (Keyboard.getEventKey() == Keyboard.KEY_X && Keyboard.getEventKeyState())
			{
				_player.unspreadLight();
			} else if (Keyboard.getEventKey() == Keyboard.KEY_W && Keyboard.getEventKeyState())
			{
				Vec3f pos = _player.getPosition();
				Chunk chunk = getChunkManager().getChunkContaining(MathHelper.floor(pos.x()), MathHelper.floor(pos.y()), MathHelper.floor(pos.z()), false, false, false);
				for (int x = 0; x < Chunk.CHUNK_SIZE_HORIZONTAL; ++x)
				{
					for (int z = 0; z < Chunk.CHUNK_SIZE_HORIZONTAL; ++z)
					{
						chunk.spreadSunlight(chunk.getAbsoluteX() + x, chunk.getAbsoluteZ() + z);
					}
				}
			}
		}
		if (_activatedInventory == null)
		{
			if (!(_localChunks.size() < 4 && _oldChunkList.size() < 4))
			{
				_player.update();
			}
		} else
		{
			_activatedInventory.update();
		}
		
		if (!_chunksToBeSetDirty.isEmpty())
		{
			_chunksToBeSetDirty.remove(0).setSunlightDirty(true);
		}

		selectLocalChunks();
		updateLocalChunks();
		checkForNewVisibleChunks();
		
		_chunkManager.performRememberedBlockChanges();

		_tick++;
	}

	private void checkForNewVisibleChunks()
	{
		float viewingDistance = Game.getInstance().getConfiguration().getViewingDistance();
		viewingDistance /= Chunk.CHUNK_SIZE_HORIZONTAL;
		viewingDistance += 1.0f;
		int distance = MathHelper.ceil(viewingDistance);
		int distanceSq = distance * distance;

		int centerX = MathHelper.floor(getPlayer().getPosition().x() / Chunk.CHUNK_SIZE_HORIZONTAL);
		int centerZ = MathHelper.floor(getPlayer().getPosition().z() / Chunk.CHUNK_SIZE_HORIZONTAL);

		ViewFrustum frustum = getPlayer().getFirstPersonCamera().getViewFrustum();

		boolean generate = false;
		int xToGenerate = 0, zToGenerate = 0;

		for (int x = -distance; x < distance; ++x)
		{
			for (int z = -distance; z < distance; ++z)
			{
				int distSq = x * x + z * z;
				if (distSq <= distanceSq)
				{
					if (!generate || (xToGenerate * xToGenerate + zToGenerate * zToGenerate > distSq))
					{
						if (distSq > 1)
						{
							Chunk.createAABBForBlockChunkAt(centerX + x, centerZ + z, _chunkVisibilityTestingAABB);
							_chunkVisibilityTestingAABB.recalcVertices();
						}
						if (distSq <= 1 || frustum.intersects(_chunkVisibilityTestingAABB))
						{
							Chunk chunk = _chunkManager.getChunk(centerX + x, centerZ + z, false, false, false);
							if (chunk == null || (!chunk.isGenerated() && !chunk.isLoading()))
							{
								generate = true;
								xToGenerate = x;
								zToGenerate = z;
							}
						}
					}
				}
			}
		}
		if (generate)
		{
			System.out.println("New chunk in sight: " + (centerX + xToGenerate) + ", " + (centerZ + zToGenerate));
			Chunk ch = _chunkManager.getChunk(centerX + xToGenerate, centerZ + zToGenerate, true, false, false);
			_chunkManager.loadAndGenerateChunk(ch, true);
		}

	}

	public void setActivatedInventory(Inventory inv)
	{
		_activatedInventory = inv;
		Mouse.setGrabbed(inv == null);
	}

	private void updateLocalChunks()
	{
		_updatingBlocks = 0;
		for (Chunk chunk : _localChunks)
		{
			if (chunk.isDestroying() || chunk.isLoading())
				continue;
			IntList list = chunk.getUpdatingBlocks();
			for (int i = 0; i < list.size(); ++i)
			{
				int index = list.get(i);
				Block block = chunk.getChunkData().getSpecialBlock(index);
				block.update();
			}
			_updatingBlocks += chunk.getUpdatingBlocks().size();
			chunk.performListChanges();
		}
	}

	public void selectLocalChunks()
	{
		_localBlockCount = 0;
		float viewingDistance = Game.getInstance().getConfiguration().getViewingDistance();

		/* Swap the lists */
		List<Chunk> temp = _localChunks;
		_localChunks = _oldChunkList;
		_oldChunkList = temp;

		_chunkManager.getApproximateChunks(_player.getPosition(), viewingDistance + Chunk.CHUNK_SIZE_HORIZONTAL, _localChunks);

		if (!_oldChunkList.isEmpty())
		{
			/* Check for chunks getting out of sight to clear the cache */
			outer: for (int i = 0; i < _oldChunkList.size(); ++i)
			{
				Chunk chunkI = _oldChunkList.get(i);
				/* Check if the old chunk is also in the new list */
				for (Chunk chunkJ : _localChunks)
				{

					if (chunkI == chunkJ)
					{
						continue outer;
					}
				}
				/* The old chunk wasn't found in the new list */
				if (!chunkI.isDestroying() && !chunkI.isLoading())
				{
					/* Unload it if it is free */
					_chunkManager.saveAndUnloadChunk(chunkI, true);
				} else
				{
					/*
					 * If it is busy, add it to the list again, to make sure it
					 * will be removed one of the next iterations
					 */
					_localChunks.add(chunkI);
				}
			}
		}

		/* Make sure every local chunk is cached */
		for (Chunk chunk : _localChunks)
		{
			if (chunk.isDestroying() || chunk.isLoading() || !chunk.isLoaded())
				continue;
			_localBlockCount += chunk.getBlockCount();
		}
	}

	private void selectVisibleChunks(ViewFrustum frustum)
	{
		_visibleChunks.clear(true);
		Chunk chunk = null;
		for (int chunkIndex = 0; chunkIndex < _localChunks.size(); ++chunkIndex)
		{
			chunk = _localChunks.get(chunkIndex);
			if (chunk.isDestroying() || chunk.isLoading())
			{
				continue;
			}

			if (frustum.intersects(chunk.getAABB()))
			{
				_visibleChunks.add(chunk);
			}
		}

		_chunkDistanceComparator.setCenter(_player.getPosition().x(), _player.getPosition().y());
		Collections.sort(_visibleChunks, _chunkDistanceComparator);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void setPlayer(Player p)
	{
		_player = p;
	}

	public ChunkManager getChunkManager()
	{
		return _chunkManager;
	}

	public WorldProvider getWorldProvider()
	{
		return _worldProvider;
	}

	public long getWorldSeed()
	{
		return _worldSeed;
	}

	public String getWorldName()
	{
		return _worldName;
	}

	public byte getSunlight()
	{
		return (byte) MathHelper.floor(_sunlight);
	}

	public float getTime()
	{
		return _time;
	}

	public void setTime(float time)
	{
		_time = time;
		float todNew = MathHelper.simplify(_time, SECONDS_IN_DAY) / SECONDS_IN_DAY;

		int oldSunlight = MathHelper.round(_sunlight);

		_sunlight = -MathHelper.cos(todNew * MathHelper.f_2PI) * 0.5f + 0.5f;
		_sunlight = Math.max(0.2f, _sunlight);
		_sunlight *= 15.0f;

		if (oldSunlight != MathHelper.round(_sunlight))
		{
			/* Update chunk lights */
			for (Chunk c : _localChunks)
			{
				_chunksToBeSetDirty.add(c);
			}
		}
		
	}
}
