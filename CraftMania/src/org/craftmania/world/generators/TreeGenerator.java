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
package org.craftmania.world.generators;

import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.datastructures.DataNode;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Chunk;

public class TreeGenerator extends Generator
{
	private SmartRandom _random;

	public TreeGenerator(long seed)
	{
		super();
		_random = new SmartRandom(new Random(seed));
	}

	public void generateBroadLeavedTree(Chunk targetChunk, int _x, int _y, int _z, boolean flatBottom)
	{
		String leafType = "leafs" + _random.randomInt(2);
		int height = _random.randomInt(9, 13);

		int radius = _random.randomInt(3, (int) (height / 2.2f)) + (flatBottom ? 1 : 0);
		float radiusSq = radius * radius;
		height -= (flatBottom ? 1 : 0);

		// Treetrunk of wood
		BlockType trunk = BlockManager.getInstance().getBlockType("wood0");
		for (int i = 0; i < height - 1; ++i)
		{
			targetChunk.setDefaultBlockAbsolute(_x, _y + i, _z, trunk, (byte) 0, true, true, false);
		}

		// Leafs
		BlockType blLeafs = BlockManager.getInstance().getBlockType(leafType);
		for (int x = -radius; x < radius; ++x)
		{
			for (int y = -radius; y < radius; ++y)
			{
				for (int z = -radius; z < radius; ++z)
				{
					if (x == 0 && z == 0 && y < radius - 1)
					{
						continue;
					}
					if (flatBottom && y < -radius + 3)
					{
						continue;
					}
					if (x * x + y * y + z * z < radiusSq)
					{
						/*
						 * Do NOT generate (but create) the chunks where the
						 * leafs are in, otherwise this will cause continuously
						 * chunks, that contains tree which invokes the
						 * generation of a new chunk.
						 */
						targetChunk.setDefaultBlockAbsolute(_x + x, _y + height - radius + y, _z + z, blLeafs, (byte) 0, true, true, false);
					}
				}
			}
		}
	}

	public void generateNiceBroadLeavedTree(Chunk targetChunk, int rootX, int rootY, int rootZ)
	{
		String leafType = "leafs" + _random.randomInt(2);

		int trunkHeight = _random.randomInt(3, 5);

		DataNode<Vec3i> tree = new DataNode<Vec3i>(new Vec3i(rootX, rootY, rootZ));
		tree.addChild(new DataNode<Vec3i>(new Vec3i(rootX, rootY + trunkHeight, rootZ)));

		int depth = _random.randomInt(4, 5);

		/* Generate a random tree of branches */
		generateBraches(tree.getData(), tree.getChild(0), depth);

		/* Build the leaves */
		buildLeaves(targetChunk, _blockManager.getBlockType(leafType), tree, Math.max(4, depth - 4));

		/* Build the branches and overwrite the leaves */
		BlockType wood = _blockManager.getBlockType("wood0");
		buildBraches(targetChunk, wood, tree);

		/* Enlarge the trunk */
		if (true)
		{
			if (_random.randomInt(4) == 0)
			{
				for (int y = rootY; y < tree.getChild(0).getData().y() + 4; ++y)
				{
					targetChunk.setDefaultBlockAbsolute(rootX - 1, y, rootZ, wood, (byte) 0, false, false, false);
					targetChunk.setDefaultBlockAbsolute(rootX + 1, y, rootZ, wood, (byte) 0, false, false, false);
					targetChunk.setDefaultBlockAbsolute(rootX, y, rootZ - 1, wood, (byte) 0, false, false, false);
					targetChunk.setDefaultBlockAbsolute(rootX, y, rootZ + 1, wood, (byte) 0, false, false, false);
				}
			} else
			{
				for (int y = rootY; y < tree.getChild(0).getData().y() + 4; ++y)
				{
					targetChunk.setDefaultBlockAbsolute(rootX + 1, y, rootZ, wood, (byte) 0, false, false, false);
					targetChunk.setDefaultBlockAbsolute(rootX, y, rootZ + 1, wood, (byte) 0, false, false, false);
					targetChunk.setDefaultBlockAbsolute(rootX + 1, y, rootZ + 1, wood, (byte) 0, false, false, false);
				}
			}
		}

	}

	private void buildLeavesSphere(Chunk chunk, BlockType leaves, int cx, int cy, int cz, float radius)
	{
		int iRadius = MathHelper.ceil(radius);
		for (int x = -iRadius; x <= iRadius; ++x)
		{
			for (int y = -iRadius; y <= iRadius; ++y)
			{
				for (int z = -iRadius; z <= iRadius; ++z)
				{
					if (x * x + y * y + z * z < radius * radius)
					{
						chunk.setDefaultBlockAbsolute(cx + x, cy + y, cz + z, leaves, (byte) 0, true, true, false);
					}
				}
			}
		}
	}

	private void buildLeaves(Chunk chunk, BlockType leaves, DataNode<Vec3i> node, int startingDepth)
	{
		for (int i = 0; i < node.childCount(); ++i)
		{
			DataNode<Vec3i> child = node.getChild(i);
			if (startingDepth == 0)
			{

				Vec3i src = node.getData();
				Vec3i dst = child.getData();

				Vec3f branch = new Vec3f(dst);
				branch.sub(new Vec3f(src));

				int len = MathHelper.ceil(branch.normalise());

				Vec3f current = new Vec3f(src);
				for (int p = 0; p <= len; ++p)
				{
					buildLeavesSphere(chunk, leaves, MathHelper.round(current.x()), MathHelper.round(current.y()), MathHelper.round(current.z()), 2.7f);
					current.add(branch);
				}

			}

			buildLeaves(chunk, leaves, child, Math.max(0, startingDepth - 1));
		}
	}

	private void buildBraches(Chunk chunk, BlockType wood, DataNode<Vec3i> tree)
	{
		Vec3i src = tree.getData();
		for (int i = 0; i < tree.childCount(); ++i)
		{
			DataNode<Vec3i> dstNode = tree.getChild(i);
			Vec3i dst = dstNode.getData();

			Vec3f branch = new Vec3f(dst);
			branch.sub(new Vec3f(src));

			int len = MathHelper.ceil(branch.normalise());

			Vec3f current = new Vec3f(src);
			for (int p = 0; p <= len; ++p)
			{
				chunk.setDefaultBlockAbsolute(MathHelper.round(current.x()), MathHelper.round(current.y()), MathHelper.round(current.z()), wood, (byte) 0, true, true, false);
				current.add(branch);
			}

			buildBraches(chunk, wood, dstNode);
		}
	}

	private void generateBraches(Vec3i root, DataNode<Vec3i> child, int depth)
	{
		if (depth == 0)
			return;

		boolean useRandomDirections = false;
		float childAngle = 0;
		if (child.getData().z() - root.z() == 0 && child.getData().x() - root.x() == 0)
		{
			useRandomDirections = true;
		} else
		{
			childAngle = (float) Math.atan2(child.getData().z() - root.z(), child.getData().x() - root.x());
		}
		if (depth % 2 == 0)
		{
			childAngle += MathHelper.f_PI;
		}
		int count = _random.randomInt((depth + 3) / 2, (depth + 5) / 2);
		for (int i = 0; i < count; ++i)
		{
			if (useRandomDirections)
			{
				childAngle += _random.randomFloat(MathHelper.f_PI / 3.0f, MathHelper.f_PI);
			}
			Vec3i vec = new Vec3i();
			vec.setY(child.getData().y() + _random.randomInt(depth - 2, depth));
			float offset = 1.5f + _random.exponentialRandom(2.0f, 4);
			float rotation = childAngle + _random.randomFloat(-0.7f, 0.7f);
			vec.setX(child.getData().x() + MathHelper.round(MathHelper.cos(rotation) * offset));
			vec.setZ(child.getData().z() + MathHelper.round(MathHelper.sin(rotation) * offset));
			child.addChild(new DataNode<Vec3i>(vec));
			generateBraches(root, child.getChild(i), depth - 1);
		}
	}

	public void generateCactus(Chunk targetChunk, int x, int y, int z)
	{
		int height = _random.randomInt(3, 5);
		BlockType bt = BlockManager.getInstance().getBlockType("cactus");
		for (int i = 0; i < height; ++i)
		{
			targetChunk.setDefaultBlockAbsolute(x, y + i, z, bt, (byte) 0, true, true, false);
		}
	}

	public void generatePinophyta(Chunk chunk, int _x, int _y, int _z)
	{
		int trunkHeight = _random.randomInt(6, 8);
		int needlesHeight = _random.randomInt(trunkHeight, trunkHeight + 5);
		int needlesElevation = _random.randomInt(trunkHeight - 3, trunkHeight - 1);
		int needlesRadius = _random.randomInt(2, 4);

		BlockType wood = BlockManager.getInstance().getBlockType("wood1");
		BlockType needles = BlockManager.getInstance().getBlockType("needles");

		for (int h = 0; h <= needlesHeight; ++h)
		{
			int radius = (int) ((float) needlesRadius * (1.0f - ((float) h / (float) needlesHeight)));
			if ((h & 1) == 1)
			{
				radius--;
			}
			if (radius < 0)
			{
				radius++;
			}
			for (int x = -radius; x <= radius; ++x)
			{
				for (int z = -radius; z <= radius; ++z)
				{
					if (x == 0 && z == 0 && h + needlesElevation < trunkHeight)
					{
						continue;
					}
					chunk.setDefaultBlockAbsolute(_x + x, _y + h + needlesElevation, _z + z, needles, (byte) 0, true, true, false);
				}
			}
		}

		// Treetrunk of wood
		for (int i = 0; i < trunkHeight; ++i)
		{
			chunk.setDefaultBlockAbsolute(_x, _y + i, _z, wood, (byte) 0, true, true, false);
		}

	}
}
