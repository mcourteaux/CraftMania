package org.craftmania.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.craftmania.blocks.Block;
import org.craftmania.utilities.FastArrayList;

/**
 * 
 * @author martijncourteaux
 */
public class BlockList implements Iterable<Block>
{

	public static interface BlockAcceptor
	{

		/**
		 * Decides wether a Block should be added to the cache or not.
		 * 
		 * @param block
		 * @param x
		 *            The x coordinate of the block (relative to the chunk)
		 * @param y
		 *            The y coordinate of the block (relative to the chunk)
		 * @param z
		 *            The z coordinate of the block (relative to the chunk)
		 * @return True if the block should be added to the cache, false
		 *         otherwise.
		 */
		public boolean accept(Block block, int x, int y, int z);

		/**
		 * Returns the Block its BlockType {@code wantsToBeUpdated()} value.
		 */
		public static BlockAcceptor BLOCK_UPDATE_ACCEPTOR = new BlockAcceptor()
		{

			@Override
			public boolean accept(Block block, int x, int y, int z)
			{
				boolean u = block.getBlockType().wantsToBeUpdated();
				block.setUpdatingFlag(u);
				return u;
			}
		};
		public static BlockAcceptor BLOCK_VISIBLE_ACCEPTOR = new BlockAcceptor()
		{

			@Override
			public boolean accept(Block block, int x, int y, int z)
			{
				boolean v = block.isVisible();
				block.setRenderingFlag(v);
				return v;
			}
		};
	}

	private FastArrayList<Block> _blocks = new FastArrayList<Block>(128);
	private FastArrayList<Block> _blocksToRemove = new FastArrayList<Block>(128);
	private FastArrayList<Block> _blocksToAdd = new FastArrayList<Block>(64);
	private boolean _cached;

	public BlockList()
	{
	}

	public void rememberToRemoveBlock(Block bl)
	{
		synchronized (_blocksToRemove)
		{
			_blocksToRemove.add(bl);
		}
	}

	public void rememberToAddBlock(Block bl)
	{
		synchronized (_blocksToAdd)
		{
			_blocksToAdd.add(bl);
		}
	}

	public void cache(BlockChunk blockChunk, BlockAcceptor acceptor)
	{
		clearCache();
		int count = 0;
		for (int x = 0; x < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; ++x)
		{
			for (int y = 0; y < BlockChunk.BLOCKCHUNK_SIZE_VERTICAL; ++y)
			{
				for (int z = 0; z < BlockChunk.BLOCKCHUNK_SIZE_HORIZONTAL; ++z)
				{
					Block block = blockChunk.getBlockRelative(x, y, z);
					if (block == null)
					{
						continue;
					}
					count++;
					if (acceptor.accept(block, x, y, z))
					{
						_blocks.add(block);
					}
					if (count == blockChunk.getBlockCount())
					{
						_cached = true;
						return;
					}
				}
			}
		}
		_cached = true;
	}

	public Block getBlockAbsolute(int x, int y, int z)
	{
		if (_cached)
		{
			for (int i = 0; i < _blocks.size(); ++i)
			{
				Block bl = _blocks.get(i);
				if (bl.getX() == x && bl.getY() == y && bl.getZ() == z)
				{
					return bl;
				}
			}
		} else
		{
			// TODO something
			System.out.println("Not cached!!");
		}
		return null;
	}

	public void clearCache()
	{
		if (!_blocks.isEmpty())
		{
			// System.out.println("Uncache");
		}
		_blocks.clear();
		synchronized (_blocksToAdd)
		{
			_blocksToAdd.clear(true);
		}
		synchronized (_blocksToRemove)
		{
			_blocksToRemove.clear(true);
		}
		_cached = false;
	}

	public boolean hasCache()
	{
		return _cached;
	}

	public Iterable<Block> getCachedBlocks()
	{
		return _blocks;
	}

	/**
	 * Absolute positions
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeBlock(int x, int y, int z)
	{
		if (_cached)
		{
			for (int i = 0; i < _blocks.size(); ++i)
			{
				Block bl = _blocks.get(i);
				if (bl.getX() == x && bl.getY() == y && bl.getZ() == z)
				{
					_blocks.remove(i);
					return;
				}
			}
		}
	}

	public void addBlock(Block bl)
	{
		if (_cached)
		{
			_blocks.add(bl);
		}
	}

	public void removeBlock(Block block)
	{
		if (_cached)
		{
			_blocks.remove(block);
		}
	}

	public void updateCacheManagment()
	{
		/* Remove blocks */
		synchronized (_blocksToRemove)
		{
			if (!_blocksToRemove.isEmpty())
			{
				for (Block bl : _blocksToRemove)
				{
					removeBlock(bl);
				}
				_blocksToRemove.clear(false);
			}
		}

		/* Add blocks */
		synchronized (_blocksToAdd)
		{
			if (!_blocksToAdd.isEmpty())
			{
				for (Block bl : _blocksToAdd)
				{
					addBlock(bl);
				}
				_blocksToAdd.clear(false);
			}
		}
	}

	@Override
	public Iterator<Block> iterator()
	{
		return _blocks.iterator();
	}

	public int size()
	{
		return _blocks.size();
	}

	public Block getBlockAtIndex(int blockIndex)
	{
		return _blocks.get(blockIndex);
	}
}