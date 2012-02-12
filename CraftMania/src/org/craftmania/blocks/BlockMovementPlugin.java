package org.craftmania.blocks;

import org.craftmania.game.Game;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;

public class BlockMovementPlugin
{
	
	private Block _block;
	private boolean _falling;
	private Vec3f _motion;
	private Vec3f _additionalCoordinates;
	
	public BlockMovementPlugin(Block b)
	{
		_block = b;
		_motion = new Vec3f();
		_additionalCoordinates = new Vec3f();
	}
	
	public void solveMotion()
	{
		/* First apply the motion */
		float delta = 0.001f;
		if (_motion.lengthSquared() > delta)
		{
			_additionalCoordinates.addFactor(_motion, Game.getInstance().getStep());
			_block.getAABB().getPosition().set(_block.getPosition()).add(_block.getAABB().getDimensions()).add(_additionalCoordinates);
			_block.getAABB().recalcVertices();
		}

		int moveX = 0;
		int moveY = 0;
		int moveZ = 0;
		if (_additionalCoordinates.x() >= 1.0f || _additionalCoordinates.x() <= -1.0f)
		{
			float addX = MathHelper.roundDelta(_additionalCoordinates.x(), delta);
			moveX = MathHelper.roundToZero(addX);
			_additionalCoordinates.setX(addX - moveX);
		}
		if (_additionalCoordinates.y() >= 1.0f || _additionalCoordinates.y() <= -1.0f)
		{
			float addY = MathHelper.roundDelta(_additionalCoordinates.y(), delta);
			moveY = MathHelper.roundToZero(addY);
			_additionalCoordinates.setY(addY - moveY);
		}
		if (_additionalCoordinates.z() >= 1.0f || _additionalCoordinates.z() <= -1.0f)
		{
			float addZ = MathHelper.roundDelta(_additionalCoordinates.z(), delta);
			moveZ = MathHelper.roundToZero(addZ);
			_additionalCoordinates.setZ(addZ - moveZ);
		}

		if (moveX != 0 || moveY != 0 || moveZ != 0)
		{
			Game.getInstance().getWorld().getChunkManager().rememberBlockMovement(_block.getX(), _block.getY(), _block.getZ(), _block.getX() + moveX, _block.getY() + moveY, _block.getZ() + moveZ);
		}
		
		if (_block.getY() < -40)
		{
			_block.destory();
		}
	}
	
	public boolean isFalling()
	{
		return _falling;
	}

	public void setFalling(boolean b)
	{
		_falling = b;
	}
	
	public boolean isMoving()
	{
		return _falling || _motion.lengthSquared() > 0.0001f;
	}

	public Vec3f getMotion()
	{
		return _motion;
	}
	
	public Vec3f getAdditionCoordinates()
	{
		return _additionalCoordinates;
	}

}
