package org.craftmania.blocks.customblocks;

import java.util.Random;

import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.CrossedBlock;
import org.craftmania.blocks.DefaultBlock;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.craftmania.math.Vec3i;
import org.craftmania.rendering.particles.Smoke;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.Chunk;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class Torch extends CrossedBlock
{

	private static final float FIRE_VIEWING_DISTANCE = Game.getInstance().getConfiguration().getFireViewingDistance();
	private static final Texture PARTICLES_TEXTURE;
	private static final int CALL_LIST_BASE;
	private static final int FIRE_PARTICLE_COUNT;
	private static final float PARTICLE_SIZE;
	private static final SmartRandom RANDOM;

	private boolean _active;
	private int _loop;
	private Vec3f _vector;
	private Vec3f _playerPosition;
	private Smoke _smoke;
	private float _particleSizingSpeeds[];
	private float _particleSizes[];

	static
	{
		RANDOM = new SmartRandom(new Random());
		FIRE_PARTICLE_COUNT = 2;
		PARTICLE_SIZE = 0.1f;
		PARTICLES_TEXTURE = TextureStorage.getTexture("particles");

		CALL_LIST_BASE = GL11.glGenLists(FIRE_PARTICLE_COUNT);

		float tileSize = 16.0f / PARTICLES_TEXTURE.getImageWidth();
		float texY = 3.0f * tileSize;

		for (int i = 0; i < FIRE_PARTICLE_COUNT; ++i)
		{
			GL11.glNewList(CALL_LIST_BASE + i, GL11.GL_COMPILE);
			GL11.glBegin(GL11.GL_QUADS);

			GL11.glTexCoord2f(i * tileSize, texY);
			GL11.glVertex2f(-PARTICLE_SIZE, PARTICLE_SIZE);

			GL11.glTexCoord2f((i + 1) * tileSize, texY);
			GL11.glVertex2f(PARTICLE_SIZE, PARTICLE_SIZE);

			GL11.glTexCoord2f((i + 1) * tileSize, texY + tileSize);
			GL11.glVertex2f(PARTICLE_SIZE, -PARTICLE_SIZE);

			GL11.glTexCoord2f(i * tileSize, texY + tileSize);
			GL11.glVertex2f(-PARTICLE_SIZE, -PARTICLE_SIZE);

			GL11.glEnd();
			GL11.glEndList();
		}
	}

	public static void RELEASE_STATIC_CONTENT()
	{
		GL11.glDeleteLists(CALL_LIST_BASE, FIRE_PARTICLE_COUNT);
	}

	public Torch(Chunk chunk, Vec3i pos)
	{
		super(BlockManager.getInstance().getBlockType("torch"), chunk, pos);
		/* Add to the manual render list to draw the fire afterwards */
		addToManualRenderList();
		addToUpdateList();

		/* An auxiliary vector */
		_vector = new Vec3f();
		_particleSizes = new float[FIRE_PARTICLE_COUNT];
		_particleSizingSpeeds = new float[FIRE_PARTICLE_COUNT];
		_smoke = new Smoke(pos.x() + 0.5f, pos.y() + 0.9f, pos.z() + 0.5f);
	}

	@Override
	public void update()
	{
		_loop = (_loop + 1) % 40;


		if (_playerPosition == null)
		{
			_playerPosition = Game.getInstance().getWorld().getActivePlayer().getPosition();
		}

		
		if (_active)
		{
			if (Math.random() / Game.getInstance().getFPS() < 0.001f)
			{
				_smoke.addParticle();
			}
			_smoke.update();

			float step = Game.getInstance().getStep();

			for (int i = 0; i < FIRE_PARTICLE_COUNT; ++i)
			{
				_particleSizes[i] += _particleSizingSpeeds[i] * step;
				if (_particleSizes[i] > 1.3f)
				{
					_particleSizingSpeeds[i] = RANDOM.randomFloat(-0.8f, 0.4f);
				} else if (_particleSizes[i] < 1.0f)
				{
					_particleSizingSpeeds[i] = RANDOM.randomFloat(0.4f, 0.8f);
				}
			}
		}

		if (_loop == 0)
		{
			/* Check range */
			_vector.set(_playerPosition);
			_vector.sub(getPosition());

			_active = (_vector.lengthSquared() < FIRE_VIEWING_DISTANCE * FIRE_VIEWING_DISTANCE);
		}
	}

	/**
	 * Renders the fire of the torch
	 */
	@Override
	public void render(byte[][][] lightBuffer)
	{
		if (_active)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);

			PARTICLES_TEXTURE.bind();

			/* Compute the angle of rotation around the Y axis to the player */
			_vector.set(getPosition());
			_vector.add(DefaultBlock.HALF_BLOCK_SIZE);
			_vector.sub(_playerPosition);
			float angle = MathHelper.atan2(_vector.z(), _vector.x()) - (MathHelper.f_PI * 0.5f);

			/* Prepare the model matrix */
			GL11.glPushMatrix();
			GL11.glTranslatef(getX() + 0.5f, getY() + 0.70f, getZ() + 0.5f);
			GL11.glRotatef(-MathHelper.toDegrees(angle), 0, 1, 0);

			/* Draw all the particles */
			for (int i = 0; i < FIRE_PARTICLE_COUNT; ++i)
			{
				GL11.glPushMatrix();
				GL11.glScalef(_particleSizes[i], _particleSizes[i], _particleSizes[i]);
				GL11.glTranslatef(0.01f * i, 0, 0);
				GL11.glCallList(CALL_LIST_BASE + i);
				GL11.glPopMatrix();
			}

			/* Restore the original matrix */
			GL11.glPopMatrix();

			/* Render the smoke */
			_smoke.render();

			GL11.glDisable(GL11.GL_ALPHA_TEST);
		}
	}
}
