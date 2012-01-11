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
import java.util.Iterator;
import java.util.List;

import org.craftmania.blocks.Block;
import org.craftmania.datastructures.ViewFrustum;
import org.craftmania.game.Configuration;
import org.craftmania.game.FontStorage;
import org.craftmania.game.Game;
import org.craftmania.inventory.Inventory;
import org.craftmania.math.BlockDistanceComparator;
import org.craftmania.math.MathHelper;
import org.craftmania.rendering.GLFont;
import org.craftmania.world.characters.Player;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class World
{
	private WorldProvider _worldProvider;
	private ChunkManager _chunkManager;
	private Player _player;

	private List<BlockChunk> _localChunks;
	private List<BlockChunk> _oldChunkList;
	private List<BlockChunk> _chunksToDelete;

	private List<Block> _visibleBlocks;
	private Inventory _activatedInventory;
	private BlockDistanceComparator _blockDistanceComparator;
	private int _localBlockCount;
	private int _updatingBlocks;
	private long _worldSeed;
	private int _tick;

	public World(long seed)
	{
		_worldSeed = seed;
		_worldProvider = new DefaultWorldProvider(this);
		_chunkManager = new ChunkManager(this);
		_localChunks = new ArrayList<BlockChunk>();
		_oldChunkList = new ArrayList<BlockChunk>();
		_chunksToDelete = new ArrayList<BlockChunk>();

		_visibleBlocks = new ArrayList<Block>(30000);
		_blockDistanceComparator = new BlockDistanceComparator();
	}

	public void render()
	{
		/* Prepare Matrixes */
		Game.getInstance().initSceneRendering();

		/* Look through the camera */
		_player.getFirstPersonCamera().lookThrough();

		/* Select the visible blocks */
		selectVisibleBlocks(_player.getFirstPersonCamera().getViewFrustum());

		for (Block b : _visibleBlocks)
		{
			b.render();
		}

		_player.render();

		renderOverlay();
	}

	private void renderOverlay()
	{
		Configuration conf = Game.getInstance().getConfiguration();

		Game.getInstance().initOverlayRendering();

		glColor3f(1, 1, 1);
		GLFont infoFont = FontStorage.getFont("Monospaced_20");

		/* Down Left Info */
		infoFont.print(4, 4, "MineCraft");
		infoFont.print(4, 30, _player.coordinatesToString());
		infoFont.print(4, 45, "Visible Blocks:      " + _visibleBlocks.size());
		infoFont.print(4, 60, "Updading Blocks:     " + _updatingBlocks);
		infoFont.print(4, 75, "Total Chunks in RAM: " + _chunkManager.getTotalBlockChunkCount());
		infoFont.print(4, 90, "Local Chunks:        " + _localChunks.size());
		infoFont.print(4, 105, "Total Local Blocks:  " + _localBlockCount);

		/* Top Left Info */
		infoFont.print(4, conf.getHeight() - 20, "FPS:      " + Game.getInstance().getFPS());
		infoFont.print(4, conf.getHeight() - 20 - 15, "Sleeping: " + String.format("%9d", Game.getInstance().getSleepTime()));

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
		if (Game.getInstance().getFPS() < 3)
			return;

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
			}
		}
		if (_activatedInventory == null)
		{
			_player.update();
		} else
		{
			_activatedInventory.update();
		}

		selectLocalChunks();
		updateLocalChunks();

		if (!_chunksToDelete.isEmpty())
		{
			_chunkManager.deleteChunk(_chunksToDelete.remove(_chunksToDelete.size() - 1));
		}

		_chunkManager.performRememberedBlockChanges();

		if (_tick % 5 == 0)
		{
			checkForNewVisibleChunks();
		}
		
		_tick++;
	}

	private void checkForNewVisibleChunks()
	{
		float viewingDistance = Game.getInstance().getConfiguration().getViewingDistance();
		viewingDistance /= BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL;
		viewingDistance += 1.0f;
		int distance = MathHelper.ceil(viewingDistance);
		int distanceSq = distance * distance;

		int centerX = MathHelper.floor(getPlayer().getPosition().x() / BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL);
		int centerZ = MathHelper.floor(getPlayer().getPosition().z() / BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL);

		ViewFrustum frustum = getPlayer().getFirstPersonCamera().getViewFrustum();

		for (int x = -distance; x < distance; ++x)
		{
			for (int z = -distance; z < distance; ++z)
			{
				int distSq = x * x + z * z;
				if (distSq <= distanceSq)
				{
					// AABB aabb = BlockChunk.createAABBForBlockChunkAt(x, z);
					// if (frustum.intersects(aabb))
					{
						if (_chunkManager.getBlockChunk(centerX + x, centerZ + z, false, false) == null)
						{
							_chunkManager.getBlockChunk(centerX + x, centerZ + z, true, true);
							return;
						}
					}

				}
			}
		}

	}

	private void setActivatedInventory(Inventory inv)
	{
		_activatedInventory = inv;
		Mouse.setGrabbed(inv == null);
	}

	private void updateLocalChunks()
	{
		_updatingBlocks = 0;
		for (BlockChunk chunk : _localChunks)
		{
			for (Iterator<Block> it = chunk.getUpdatingBlocks().iterator(); it.hasNext();)
			{
				Block b = it.next();
				b.update(it);
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
		List<BlockChunk> temp = _localChunks;
		_localChunks = _oldChunkList;
		_oldChunkList = temp;

		_chunkManager.getApproximateChunks(_player.getPosition(), viewingDistance, _localChunks);

		if (!_oldChunkList.isEmpty())
		{
			/* Check for chunks getting out of sight to clear the cache */
			outer: for (int i = 0; i < _oldChunkList.size(); ++i)
			{
				BlockChunk chunkI = _oldChunkList.get(i);
				/* Check if the old chunk is also in the new list */
				for (BlockChunk chunkJ : _localChunks)
				{
					if (chunkI == chunkJ)
					{
						continue outer;
					}
				}
				if (!chunkI.isDestroying())
				{
					chunkI.clearCache();
					_chunksToDelete.add(chunkI);
				}
			}
		}

		/* Make sure every local chunk is cached */
		for (BlockChunk chunk : _localChunks)
		{
			if (chunk.isDestroying())
				continue;
			chunk.cache();
			_localBlockCount += chunk.getBlockCount();
		}
	}

	public void selectVisibleBlocks(ViewFrustum frustum)
	{

		_visibleBlocks.clear();

		for (BlockChunk chunk : _localChunks)
		{
			if (chunk.isEmpty())
				continue;
			if (frustum.intersects(chunk.getAABB()))
			{
				for (Block block : chunk.getVisibleBlocks())
				{
					if (block.isVisible())
					{
						if (frustum.intersects(block.getAABB()))
						{
							_visibleBlocks.add(block);
						}
					}
				}
			}
		}

		_blockDistanceComparator.setOrigin(_player.getFirstPersonCamera().getPosition());
		Collections.sort(_visibleBlocks, _blockDistanceComparator);
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

	public List<Block> visibleBlocks()
	{
		return _visibleBlocks;
	}

	public long getWorldSeed()
	{
		return _worldSeed;
	}
}
