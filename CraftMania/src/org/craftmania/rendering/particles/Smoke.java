package org.craftmania.rendering.particles;

import java.util.ArrayList;
import java.util.List;

import org.craftmania.GameObject;
import org.craftmania.game.Game;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class Smoke extends GameObject
{
	private static final int CALL_LIST_BASE;
	private static final int PARTICLE_COUNT;
	private static final Vec3f UP_VECTOR;
	private static final Texture PARTICLES_TEXTURE;

	static
	{
		UP_VECTOR = new Vec3f(0.0f, 2.0f, 0.0f);
		PARTICLE_COUNT = 8;
		float PARTICLE_SIZE = 0.1f;
		PARTICLES_TEXTURE = TextureStorage.getTexture("particles");

		CALL_LIST_BASE = GL11.glGenLists(PARTICLE_COUNT);

		float tileSize = 16.0f / PARTICLES_TEXTURE.getImageWidth();

		for (int i = 0; i < PARTICLE_COUNT; ++i)
		{
			GL11.glNewList(CALL_LIST_BASE + i, GL11.GL_COMPILE);
			GL11.glBegin(GL11.GL_QUADS);

			GL11.glTexCoord2f(i * tileSize, 0.0f);
			GL11.glVertex2f(-PARTICLE_SIZE, PARTICLE_SIZE);

			GL11.glTexCoord2f((i + 1) * tileSize, 0.0f);
			GL11.glVertex2f(PARTICLE_SIZE, PARTICLE_SIZE);

			GL11.glTexCoord2f((i + 1) * tileSize, tileSize);
			GL11.glVertex2f(PARTICLE_SIZE, -PARTICLE_SIZE);

			GL11.glTexCoord2f(i * tileSize, tileSize);
			GL11.glVertex2f(-PARTICLE_SIZE, -PARTICLE_SIZE);

			GL11.glEnd();
			GL11.glEndList();
		}
	}

	private List<Particle> _particles;
	private Vec3f _origin;
	private Vec3f _vector;
	private Vec3f _playerPosition;

	private class Particle
	{
		public Vec3f _position;
		public float _size;
		public float _speed;
		public float _rotation;
		public float _rotationSpeed;
		public int _texture;

		public Particle(float x, float y, float z, float size)
		{
			_position = new Vec3f(x, y, z);
			_size = size;
		}
	}

	public Smoke(float x, float y, float z)
	{
		_particles = new ArrayList<Particle>();
		_origin = new Vec3f(x, y, z);
		_vector = new Vec3f();
	}

	public void addParticle()
	{
		float size = (float) (Math.random() * 0.2d + 0.2d);
		Particle p = new Particle(_origin.x(), _origin.y(), _origin.z(), size);
		p._rotationSpeed = (float) (Math.random() * 40.0d - 20.0d);
		p._texture = PARTICLE_COUNT - 1;
		_particles.add(p);
	}

	@Override
	public void update()
	{


		float step = Game.getInstance().getStep();
		for (int i = 0; i < _particles.size(); ++i)
		{
			Particle p = _particles.get(i);
			p._position.addFactor(UP_VECTOR, p._speed * step);
			p._speed += step * 0.8f;
			p._rotation += p._rotationSpeed * step;
			p._size -= step * 0.4f;
			p._texture = MathHelper.clamp((int) (p._size * 20.0f), 0, PARTICLE_COUNT - 1);

			if (p._size <= 0.0f)
			{
				/* Remove this particle */
				_particles.remove(i--);
			}
		}
	}

	@Override
	public void render()
	{
		if (_playerPosition == null)
		{
			_playerPosition = Game.getInstance().getWorld().getActivePlayer().getPosition();
		}
		
		
		PARTICLES_TEXTURE.bind();

		/* Compute the angle of rotation around the Y axis to the player */
		_vector.set(_origin);
		_vector.sub(_playerPosition);
		float angle = MathHelper.atan2(_vector.z(), _vector.x()) - (MathHelper.f_PI * 0.5f);

		for (int i = 0; i < _particles.size(); ++i)
		{
			Particle p = _particles.get(i);

			/* Prepare the model matrix */
			GL11.glPushMatrix();
			GL11.glTranslatef(p._position.x(), p._position.y(), p._position.z());
			GL11.glRotatef(p._rotation, 0, 0, 1);
			GL11.glRotatef(-MathHelper.toDegrees(angle), 0, 1, 0);

			/* Draw all the particles */
			GL11.glCallList(CALL_LIST_BASE + p._texture);

			/* Restore the original matrix */
			GL11.glPopMatrix();
		}
	}

}
