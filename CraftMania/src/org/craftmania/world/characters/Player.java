package org.craftmania.world.characters;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import java.util.List;

import org.craftmania.GameObject;
import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockType;
import org.craftmania.game.Game;
import org.craftmania.inventory.DefaultPlayerInventory;
import org.craftmania.inventory.Inventory.InventoryPlace;
import org.craftmania.inventory.InventoryItem;
import org.craftmania.inventory.SharedInventoryContent;
import org.craftmania.items.ItemManager;
import org.craftmania.math.MathHelper;
import org.craftmania.math.RayBlockIntersection;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.utilities.ReverseIterator;
import org.craftmania.world.BlockChunk;
import org.craftmania.world.Camera;
import org.craftmania.world.ChunkManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * 
 * @author martijncourteaux
 */
public class Player extends GameObject
{

	private Camera _camera;
	/** Postion of the player absolute in the world */
	private float x, y, z;
	private Vec3f _position;
	/** Rotation of the players head, in radians */
	private float rotX, rotY; // Radialen
	/* Movement variables */
	private float speedForward = 0.0f;
	private float speedSide = 0.0f;
	private float maxSpeed = 6.0f;
	private float eyeHeight = 1.7f;
	private float playerHeight = 1.85f;
	private float acceleration = 15.0f;
	private float ySpeed = 0.0f;
	private boolean onGround = false;
	private boolean _flying = false;
	/* Body */
	private CharacterBody _body;
	/* Editing */
	private float _rayCastLength;
	private Block _aimedBlock;
	private Vec3i _aimedAdjacentBlockPosition;
	private ChunkManager _chunkManager;
	private InventoryItem _selectedItem;
	private int _selectedInventoryItemIndex = 0;
	private boolean _airSmashed;
	private boolean _destroying;
	/* Inventory */
	private DefaultPlayerInventory _inventory;
	private SharedInventoryContent _sharedInventoryContent;

	public Player(float x, float y, float z)
	{

		_position = new Vec3f(x, y, z);
		_rayCastLength = Game.getInstance().getConfiguration().getMaximumPlayerEditingDistance();
		_chunkManager = Game.getInstance().getWorld().getChunkManager();
		_body = new CharacterBody();

		_camera = new Camera();
		_camera.setFovy(Game.getInstance().getConfiguration().getFOVY());
		this.x = x;
		this.y = y;
		this.z = z;

		_inventory = new DefaultPlayerInventory();
		_sharedInventoryContent = new SharedInventoryContent(4 * 9);
		_inventory.setSharedContent(_sharedInventoryContent);

		setSelectedInventoryItemIndex(0);
	}

	public Player(Vec3f spawnPoint)
	{
		this(spawnPoint.x(), spawnPoint.y(), spawnPoint.z());
	}

	public Camera getFirstPersonCamera()
	{
		return _camera;
	}

	public void setPosition(Vec3f position)
	{
		this.x = position.x();
		this.y = position.y();
		this.z = position.z();
	}

	public Vec3f getPosition()
	{
		return _position;
	}

	@Override
	public void render()
	{

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);

		glEnable(GL_BLEND);
		glClear(GL_DEPTH_BUFFER_BIT);

		if (_aimedBlock != null)
		{
			glClear(GL_DEPTH_BUFFER_BIT);
			glDisable(GL_TEXTURE_2D);

			_aimedBlock.getAABB().render(0.0f, 0.0f, 0.0f, 0.1f);
			glEnable(GL_TEXTURE_2D);
		}

		if (_selectedItem != null)
		{
			glPushMatrix();
			glEnable(GL_TEXTURE_2D);
			glDisable(GL_CULL_FACE);

			_body.transformToRightHand();
			_selectedItem.renderHoldableObject();
			glEnable(GL_CULL_FACE);
			glPopMatrix();
		}
		glDisable(GL_DEPTH_TEST);
	}

	@Override
	public void update()
	{
		physics();
		movement();
		_position.set(x, y, z);

		/* Update the camera */
		_camera.setPosition(x, y + eyeHeight, z);
		_camera.setRotation(rotX, rotY);

		_body.update();
		_body.disableUsingRightHand();

		if (_selectedItem != null)
		{
			_body._animationSpeedRightHand = _selectedItem.getAnimationSpeed();
		} else
		{
			_body._animationSpeedRightHand = 1.0f;
		}

		rayCastBlock();

		while (Mouse.next())
		{
			int button = Mouse.getEventButton();
			if (button != -1)
			{
				// if (button == 0 && Mouse.getEventButtonState() &&
				// Keyboard.isKeyDown(Keyboard.KEY_V))
				// {
				// /* Print the visible blocks */
				//
				// for (Block bl :
				// Game.getInstance().getWorld().visibleBlocks())
				// {
				// if (Keyboard.isKeyDown(Keyboard.KEY_C))
				// {
				// bl.needsVisibilityCheck();
				// } else
				// {
				// System.out.printf("Block: %4d, %4d, %4d (%12s) (mask = %s)%n",
				// bl.getX(), bl.getY(), bl.getZ(), bl.getBlockType().getType(),
				// String.format("%6s",
				// Integer.toBinaryString(bl.getFaceMask())).replace(' ', '0'));
				// }
				// }
				// try
				// {
				// Thread.sleep(100);
				//
				// } catch (Exception e)
				// {
				// }
				// } else if (button == 0 && Mouse.getEventButtonState() &&
				// Keyboard.isKeyDown(Keyboard.KEY_U))
				// {
				// /* Print the update blocks */
				// for (BlockChunk chunk :
				// Game.getInstance().getWorld().getLocalChunks())
				// {
				// for (Block bl : chunk.cachedUpdateListBlocks())
				// {
				// System.out.printf("Block: %4d, %4d, %4d (%s)%n", bl.getX(),
				// bl.getY(), bl.getZ(), bl.getBlockType().getType());
				// }
				// }
				// try
				// {
				// Thread.sleep(100);
				//
				// } catch (Exception e)
				// {
				// }
				// } else
				if (button == 0 && Mouse.getEventButtonState()) // Create OR Do
																// Action
				{
					if (_aimedBlock != null)
					{
						if (_aimedBlock.hasSpecialAction())
						{
							_aimedBlock.performSpecialAction();
						} else if (_selectedItem instanceof BlockType)
						{
							int bX = _aimedAdjacentBlockPosition.x();
							int bY = _aimedAdjacentBlockPosition.y();
							int bZ = _aimedAdjacentBlockPosition.z();

							int pX = MathHelper.floor(x);
							int pY = MathHelper.floor(y);
							int pZ = MathHelper.floor(z);

							if (bX == pX && (bY == pY || bY == pY + 1) && bZ == pZ)
							{
								// Player is where the block has to come
							} else
							{
								BlockChunk bc = _chunkManager.getBlockChunkContaining(bX, bY, bZ, true, true, true);
								Block currentBlock = bc.getBlockAbsolute(bX, bY, bZ);
								if (currentBlock == null)
								{
									bc.setBlockTypeAbsolute(bX, bY, bZ, ((BlockType) _selectedItem).getID(), true, true, true);
									_inventory.getInventoryPlace(_selectedInventoryItemIndex).getStack().decreaseItemCount();
									setSelectedInventoryItemIndex(_selectedInventoryItemIndex);
								}
							}
						}
					}
				}
			} else
			{
				int wheel = Mouse.getEventDWheel();
				if (wheel != 0)
				{
					wheel /= 100;
					setSelectedInventoryItemIndex(MathHelper.clamp(_selectedInventoryItemIndex + wheel, 0, 8));
				}
			}

		}
		{
			if (Mouse.isButtonDown(1)) // Destroy
			{
				if (_aimedBlock != null)
				{
					_destroying = true;
					float toolDamage = 0.0f;
					if (_selectedItem != null)
					{
						_body.enableUsingRightHand();
						toolDamage = _selectedItem.calcDamageInflictedByBlock(_aimedBlock);
					}

					boolean destroyed = _aimedBlock.smash(_selectedItem);
					if (destroyed)
					{
						/* Add block to the inventory */
						int mineResult = _aimedBlock.getBlockType().getMineResult();
						int mineResultCount = _aimedBlock.getBlockType().getMineResultCount();
						if (mineResult != 0)
						{
							for (int i = 0; i < mineResultCount; ++i)
							{
								boolean added = _inventory.addToInventory(ItemManager.getInstance().getInventoryItem(
										(short) (mineResult == -1 ? _aimedBlock.getBlockType().getInventoryTypeID() : mineResult)));

								if (added)
								{
									// TODO Play sound for taking an item
								}
							}
						}
						_aimedBlock = null;
						if (_selectedItem != null)
						{
							_selectedItem.inflictDamage(toolDamage);
						}
					}
				} else if (_selectedItem != null)
				{
					if (!_airSmashed && !_destroying)
					{
						_body.airSmash();
						_airSmashed = true;
					}
				}
			} else
			{
				_airSmashed = false;
				_destroying = false;
			}
		}
	}

	private void movement()
	{
		float step = Game.getInstance().getStep();
		float factor = (onGround ? 1.0f : 0.5f);
		float accelerationStep = acceleration * step;

		float xStep = 0.0f;
		float zStep = 0.0f;
		// Forward movement
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_Z))
			{
				speedForward = Math.min(maxSpeed * (_flying ? 3.0f : 1.0f), speedForward + acceleration * step);
			} else if (Keyboard.isKeyDown(Keyboard.KEY_S))
			{
				speedForward = Math.max(-maxSpeed * (_flying ? 3.0f : 1.0f), speedForward - acceleration * step);
			} else
			{
				if (speedForward != 0.0f)
				{
					if (speedForward < 0.0f)
					{
						speedForward += factor * accelerationStep;
						if (speedForward > 0.0f)
						{
							speedForward = 0.0f;
						}
					} else
					{
						speedForward -= factor * accelerationStep;
						if (speedForward < 0.0f)
						{
							speedForward = 0.0f;
						}
					}
				}

			}
			float xAdd = (float) (MathHelper.cos(rotY)) * speedForward * step;
			float zAdd = (float) (MathHelper.sin(-rotY)) * speedForward * step;

			xStep += xAdd;
			zStep += zAdd;

		}
		// Side movement
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_Q))
			{
				speedSide = Math.max(-maxSpeed * (_flying ? 3.0f : 1.0f), speedSide - acceleration * step);
			} else if (Keyboard.isKeyDown(Keyboard.KEY_D))
			{
				speedSide = Math.min(maxSpeed * (_flying ? 3.0f : 1.0f), speedSide + acceleration * step);
			} else
			{
				if (speedSide != 0.0f)
				{
					if (speedSide < 0.0f)
					{
						speedSide += factor * accelerationStep;
						if (speedSide > 0.0f)
						{
							speedSide = 0.0f;
						}
					} else
					{
						speedSide -= factor * accelerationStep;
						if (speedSide < 0.0f)
						{
							speedSide = 0.0f;
						}
					}
				}
			}
			float xAdd = (float) (MathHelper.sin(rotY)) * speedSide * step;
			float zAdd = (float) (MathHelper.cos(rotY)) * speedSide * step;

			xStep += xAdd;
			zStep += zAdd;
		}

		x += xStep;
		z += zStep;

		BlockChunk bc = null;

		Block wall = null;
		Block wall2 = null;
		{
			int xx = MathHelper.floor(x), yy = MathHelper.floor(y + 0.1f), zz = MathHelper.floor(z);
			bc = _chunkManager.getBlockChunkContaining(xx, yy, zz, false, true, true);
			if (bc != null)
			{
				wall = bc.getBlockAbsolute(xx, yy, zz);
			}
		}
		{
			int xx = MathHelper.floor(x), yy = MathHelper.floor(y + 0.1f) + 1, zz = MathHelper.floor(z);
			if (bc != null)
			{
				wall2 = bc.getBlockAbsolute(xx, yy, zz);
			} else
			{
				bc = _chunkManager.getBlockChunkContaining(xx, yy, zz, false, true, true);
				if (bc != null)
				{
					wall2 = bc.getBlockAbsolute(xx, yy, zz);
				}
			}
		}
		if ((wall != null && wall.getBlockType().isSolid()) || (wall2 != null && wall2.getBlockType().isSolid()))
		{

			x -= xStep;
			z -= zStep;

			speedForward *= -0.2f;
			speedSide *= -0.2f;
		}

		float dx = Mouse.getDX();
		float dy = Mouse.getDY();

		rotY -= dx / 300.0f;
		rotX += dy / 300.0f;

		rotY = MathHelper.simplifyRadians(rotY);
		rotX = MathHelper.clamp(rotX, -MathHelper.f_PI / 2.001f, MathHelper.f_PI / 2.001f);

	}

	private void physics()
	{
		float step = Game.getInstance().getStep();
		ChunkManager chunkManager = Game.getInstance().getWorld().getChunkManager();
		Block support = chunkManager.getBlock(MathHelper.floor(x), MathHelper.floor(y - 0.1f), MathHelper.floor(z), false, false, false);
		Block subSupport = chunkManager.getBlock(MathHelper.floor(x), MathHelper.floor(y - 0.1f) - 1, MathHelper.floor(z), false, false, false);
		float supportHeight = Float.NEGATIVE_INFINITY;

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			if (onGround)
			{
				ySpeed = 7f;
				onGround = false;
			} else if (_flying)
			{
				ySpeed += 14f * step;
			}
		} else if (_flying && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			ySpeed -= 14f * step;
		}

		if (support != null)
		{
			if (support.getBlockType().isSolid())
			{
				supportHeight = support.getAABB().maxY();
				onGround = false;
			}
		} else if (subSupport != null)
		{
			if (subSupport.getBlockType().isSolid())
			{
				supportHeight = subSupport.getAABB().maxY();
				onGround = false;
			}
		}

		if (supportHeight > y)
		{
			y = supportHeight;
			ySpeed = 0.0f;
			onGround = true;
		} else
		{
			if (!_flying)
			{
				ySpeed -= step * 20f;
			} else
			{
				float newSpeedY = ySpeed * 0.1f;
				float diffY = newSpeedY - ySpeed;
				ySpeed += diffY * step;
			}
			y += ySpeed * step;
			if (ySpeed > 0.0f)
			{
				int headbangY = MathHelper.floor(y + 0.1f) + 2;
				Block headbang = chunkManager.getBlock(MathHelper.floor(x), headbangY, MathHelper.floor(z), false, false, false);
				if (headbang != null && (headbangY < y + playerHeight))
				{
					y = (float) headbangY - playerHeight;
					ySpeed = 0.0f;
				}
			}
			{
				onGround = false;
				if (supportHeight >= y)
				{
					y = supportHeight;
					ySpeed = 0.0f;
					onGround = true;
				}
			}
		}
	}

	private void rayCastBlock()
	{
		float rayLenSquared = _rayCastLength * _rayCastLength;

		Vec3f rayDirection = _camera.getLookDirection();
		Vec3f rayOrigin = _camera.getPosition();

		List<Block> visibleBlocks = Game.getInstance().getWorld().visibleBlocks();

		RayBlockIntersection.Intersection closestIntersection = null;
		Block closestBlock = null;

		/* Iterate over all posible candidates for the raycast */
		Vec3f v = new Vec3f();
		for (Block bl : new ReverseIterator<Block>(visibleBlocks))
		{
			if (bl.isMoving())
			{
				continue;
			}

			v.set(bl.getAABB().getPosition());

			v.sub(getPosition());
			float lenSquared = v.lengthSquared();
			if (lenSquared < rayLenSquared)
			{
				/* Perform the raycast */
				List<RayBlockIntersection.Intersection> intersections = RayBlockIntersection.executeIntersection(bl.getX(), bl.getY(), bl.getZ(), bl.getAABB(), rayOrigin,
						rayDirection);
				if (!intersections.isEmpty())
				{
					if (closestIntersection == null || intersections.get(0).getDistance() < closestIntersection.getDistance())
					{
						closestIntersection = intersections.get(0);
						closestBlock = bl;
					}
				}
			} else
			{
				break;
			}

		}

		_aimedBlock = closestBlock;
		_aimedAdjacentBlockPosition = _aimedBlock == null ? null : closestIntersection.calcAdjacentBlockPos();

		if (_selectedItem != null && _aimedBlock != null)
		{
			_body.setBlockDistance(closestIntersection.getDistance());
		} else
		{
			_body.setBlockDistance(0.0f);
		}
	}

	public DefaultPlayerInventory getInventory()
	{
		return _inventory;
	}

	private void setSelectedInventoryItemIndex(int i)
	{
		InventoryPlace oldPlace = _inventory.getInventoryPlace(_selectedInventoryItemIndex);
		if (oldPlace != null)
		{
			if (oldPlace.getItem() != null)
			{
				_body.forceDisableUsingRightHand();
			}
		}
		_selectedInventoryItemIndex = i;
		InventoryPlace newPlace = _inventory.getInventoryPlace(_selectedInventoryItemIndex);
		if (newPlace != null)
		{
			if (newPlace.isStack())
			{
				int itemType = newPlace.getStack().getItemType();
				_selectedItem = ItemManager.getInstance().getInventoryItem((short) itemType);
			} else
			{
				_selectedItem = newPlace.getItem();
			}
		} else
		{
			_selectedItem = null;
		}

	}

	public void inventoryContentChanged()
	{
		setSelectedInventoryItemIndex(_selectedInventoryItemIndex);
	}

	public String coordinatesToString()
	{
		return String.format("x: %8s, y: %8s, z: %8s", String.format("%.3f", x), String.format("%.3f", y), String.format("%.3f", z));
	}

	public SharedInventoryContent getSharedInventoryContent()
	{
		return _sharedInventoryContent;
	}

	public void toggleFlying()
	{
		_flying = !_flying;
	}
}