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
package org.craftmania.world;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.craftmania.blocks.Block;
import org.craftmania.datastructures.AABB;
import org.craftmania.datastructures.ViewFrustum;
import org.craftmania.game.Configuration;
import org.craftmania.game.ControlSettings;
import org.craftmania.game.FontStorage;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.inventory.Inventory;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.craftmania.rendering.ChunkMeshBuilder.MeshType;
import org.craftmania.rendering.GLFont;
import org.craftmania.rendering.GLUtils;
import org.craftmania.utilities.FastArrayList;
import org.craftmania.utilities.IntList;
import org.craftmania.world.characters.Player;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class World
{

	private static final float SECONDS_IN_DAY = 60f * 10f; // 15 minutes / day

	private WorldProvider _worldProvider;
	private ChunkManager _chunkManager;
	private Player _player;
	private Sky _sky;

	private List<Chunk> _localChunks;
	private List<Chunk> _oldChunkList;
	private List<Chunk> _chunksThatNeedsNewVBO;

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

	private boolean _checkForNewChunks;

	public World(String name, long seed) throws Exception
	{
		_worldName = name;
		_worldSeed = seed;
		_worldProvider = new DefaultWorldProvider(this);
		_sky = new Sky();
		_chunkManager = new ChunkManager(this);
		_localChunks = new ArrayList<Chunk>();
		_oldChunkList = new ArrayList<Chunk>();
		_chunksThatNeedsNewVBO = new ArrayList<Chunk>();
		_visibleChunks = new FastArrayList<Chunk>(30);
		_chunkVisibilityTestingAABB = new AABB(new Vec3f(), new Vec3f());
		_chunkDistanceComparator = new ChunkDistanceComparator();
		_fogColor = new Vec3f();
		_time = SECONDS_IN_DAY * 0.3f;
	}

	public void save() throws Exception
	{
		_player.save();
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
		Configuration configuration = Game.getInstance().getConfiguration();

		/* Look through the camera with high viewing distance to render the sky */
		_player.getFirstPersonCamera().lookThrough(512.0f);

		/* Set the fog color based on time */
		_fogColor.set(Game.getInstance().getConfiguration().getFogColor());
		_fogColor.scale(_sunlight - 0.05f);
		GL11.glFog(GL11.GL_FOG_COLOR, GLUtils.wrapDirect(_fogColor.x(), _fogColor.y(), _fogColor.z(), 1.0f));
		GL11.glClearColor(_fogColor.x(), _fogColor.y(), _fogColor.z(), 1.0f);
		glFogf(GL_FOG_START, 200);
		glFogf(GL_FOG_END, 400);

		_sky.render();

		/* Restore the fog distance */
		glFogf(GL_FOG_START, configuration.getViewingDistance() * 0.55f);
		glFogf(GL_FOG_END, configuration.getViewingDistance());

		/* Select the visible blocks */
		selectVisibleChunks(_player.getFirstPersonCamera().getViewFrustum());

		/* Bind the terrain texture */
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		TextureStorage.getTexture("terrain").bind();

		for (int i = 0; i < _visibleChunks.size(); ++i)
		{
			_visibleChunks.get(i).render(MeshType.SOLID);
		}
		for (int i = 0; i < _visibleChunks.size(); ++i)
		{
			_visibleChunks.get(i).render(MeshType.TRANSLUCENT);
			_visibleChunks.get(i).renderManualBlocks();
		}

		_player.render();

		renderOverlay();
	}

	private void renderOverlay()
	{
		Configuration conf = Game.getInstance().getConfiguration();

		Game.getInstance().initOverlayRendering();

		glColor3f(1, 1, 1);

		if (Game.RENDER_INFORMATION_OVERLAY)
		{
			GLFont infoFont = FontStorage.getFont("Monospaced_20");

			/* Down Left Info */
			infoFont.print(4, 4, "CraftMania");
			infoFont.print(4, 30, _player.coordinatesToString());
			infoFont.print(4, 45, "Visible Chunks:      " + _visibleChunks.size());
			infoFont.print(4, 60, "Updading Blocks:     " + _updatingBlocks);
			infoFont.print(4, 75, "Total Chunks in RAM: " + _chunkManager.getTotalChunkCount());
			infoFont.print(4, 90, "Local Chunks:        " + _localChunks.size());
			infoFont.print(4, 105, "Total Local Blocks:  " + _localBlockCount);
			infoFont.print(4, 120, "Time:  " + _time);
			infoFont.print(4, 135, "Sunlight:  " + _sunlight);

		}
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

		_time += Game.getInstance().getStep();
		_tick = MathHelper.floor(_time);

		float todNew = MathHelper.simplify(_time, SECONDS_IN_DAY) / SECONDS_IN_DAY;

		int oldSunlight = MathHelper.floor(_sunlight * 29.99f);

		_sunlight = -MathHelper.cos(todNew * MathHelper.f_2PI) * 0.5f + 0.5f;
		_sunlight = Math.max(0.15f, _sunlight);

		if (oldSunlight != MathHelper.floor(_sunlight * 29.99f))
		{
			/* Update chunk lights */

			for (Chunk c : _localChunks)
			{
				if (!_chunksThatNeedsNewVBO.contains(c))
				{
					_chunksThatNeedsNewVBO.add(c);
				}
			}
		}

		_sky.update();

		processInput();

		if (_activatedInventory == null)
		{
			if (!(_localChunks.size() < 4 && _oldChunkList.size() < 4))
			{
				_player.update();
			} else
			{
				checkForNewVisibleChunks();
				selectLocalChunks();
			}
		} else
		{
			_activatedInventory.update();
		}

		if (!_chunksThatNeedsNewVBO.isEmpty())
		{
			_chunksThatNeedsNewVBO.remove(0).needsNewVBO();
		}

		if (_checkForNewChunks)
		{
			checkForNewVisibleChunks();
			selectLocalChunks();
		}

		updateLocalChunks();

		_chunkManager.performRememberedBlockChanges();

		_tick++;
	}

	public void checkForNewVisibleChunks()
	{
		float viewingDistance = Game.getInstance().getConfiguration().getViewingDistance();
		viewingDistance /= Chunk.CHUNK_SIZE_HORIZONTAL;
		viewingDistance += 1.0f;
		int distance = MathHelper.ceil(viewingDistance);
		int distanceSq = distance * distance;

		int centerX = MathHelper.floor(getActivePlayer().getPosition().x() / Chunk.CHUNK_SIZE_HORIZONTAL);
		int centerZ = MathHelper.floor(getActivePlayer().getPosition().z() / Chunk.CHUNK_SIZE_HORIZONTAL);

		ViewFrustum frustum = getActivePlayer().getFirstPersonCamera().getViewFrustum();

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
			_checkForNewChunks = true;
		} else
		{
			_checkForNewChunks = false;
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

	public Player getActivePlayer()
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

	public float getSunlight()
	{
		return _sunlight;
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
		_sunlight = Math.max(0.15f, _sunlight);

		if (oldSunlight != MathHelper.round(_sunlight))
		{
			/* Update chunk lights */
			for (Chunk c : _localChunks)
			{
				c.needsNewVBO();
			}
		}

	}

	private void processInput()
	{
		/* Keyboard */
		while (Keyboard.next())
		{
			processInputSection(false);
		}
		while (Mouse.next())
		{
			processInputSection(true);
			if (_activatedInventory == null)
			{
				_player.scrollInventoryItem();
			} else
			{
				_activatedInventory.mouseEvent();
			}
		}
	}

	public void processInputSection(boolean mouse)
	{
		if (ControlSettings.isCurrentEvent(ControlSettings.INVENTORY, mouse))
		{
			if (_activatedInventory != null)
			{
				setActivatedInventory(null);
			} else
			{
				setActivatedInventory(_player.getInventory());
			}
		} else if (ControlSettings.isCurrentEvent(ControlSettings.TOGGLE_GOD_MODE, mouse))
		{
			_player.toggleFlying();
		} else if (ControlSettings.isCurrentEvent(ControlSettings.TOGGLE_OVERLAY, mouse))
		{
			Game.RENDER_INFORMATION_OVERLAY = !Game.RENDER_INFORMATION_OVERLAY;
		} else if (ControlSettings.isCurrentEvent(ControlSettings.TOGGLE_LIGHT_POINT, mouse))
		{
			_player.toggleLight();
		} else if (ControlSettings.isCurrentEvent(ControlSettings.BUILD_OR_ACTION, mouse))
		{
			_player.buildOrAction();
		}

	}

}
