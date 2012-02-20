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
package org.craftmania.datastructures;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3d;

import org.craftmania.math.Vec3f;

/**
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB
{

	private Vec3f _position;
	private Vec3f _dimensions;
	private Vec3f[] _vertices;

	public AABB(Vec3f position, Vec3f dimensions)
	{
		this._position = position;
		this._dimensions = dimensions;
	}

	public AABB(AABB orig)
	{
		this._position = new Vec3f(orig._position);
		this._dimensions = new Vec3f(orig._dimensions);
	}

	/**
	 * Returns true if this AABB overlaps the given AABB.
	 * 
	 * @param aabb2
	 *            The AABB to check for overlapping
	 * @return True if overlapping
	 */
	public boolean overlaps(AABB aabb2)
	{
		if (maxX() < aabb2.minX() || minX() > aabb2.maxX())
		{
			return false;
		}
		if (maxY() < aabb2.minY() || minY() > aabb2.maxY())
		{
			return false;
		}
		if (maxZ() < aabb2.minZ() || minZ() > aabb2.maxZ())
		{
			return false;
		}
		return true;
	}

	/**
	 * Returns the closest point on the AABB to a given point.
	 * 
	 * @param p
	 *            The point
	 * @return The point on the AABB closest to the given point
	 */
	public Vec3f closestPointInsideAABBToPoint(Vec3f p)
	{
		Vec3f r = new Vec3f(p);

		if (p.x() < minX())
		{
			r.x(minX());
		}
		if (p.x() > maxX())
		{
			r.x(maxX());
		}
		if (p.y() < minY())
		{
			r.y(minY());
		}
		if (p.y() > maxY())
		{
			r.y(maxY());
		}
		if (p.z() < minZ())
		{
			r.z(minZ());
		}
		if (p.z() > maxZ())
		{
			r.z(maxZ());
		}

		return r;
	}

	public Vec3f closestPointOnAABBToPoint(Vec3f p, boolean useX, boolean useY, boolean useZ)
	{
		if (!contains(p))
		{
			return closestPointInsideAABBToPoint(p);
		} else
		{
			Vec3f r = new Vec3f(p);

			float shortestExit = 1000000.0f;

			int side = -1;

			if (useX && minX() < p.x() && p.x() < maxX())
			{
				float dist = Math.min(p.x() - minX(), maxX() - p.x());
				if (dist < shortestExit)
				{
					side = 0;
					shortestExit = dist;
				}
			}
			if (useY && minY() < p.y() && p.y() < maxY())
			{
				float dist = Math.min(p.y() - minY(), maxY() - p.y());
				if (dist < shortestExit)
				{
					side = 1;
					shortestExit = dist;
				}
			}
			if (useZ && minZ() < p.z() && p.z() < maxZ())
			{
				float dist = Math.min(p.z() - minZ(), maxZ() - p.z());
				if (dist < shortestExit)
				{
					side = 2;
					shortestExit = dist;
				}
			}

			if (side == 0) // x
			{
				if (p.x() > _position.x())
				{
					r.x(maxX());
				} else
				{
					r.x(minX());
				}
			} else if (side == 1) // y
			{
				if (p.y() > _position.y())
				{
					r.y(maxY());
				} else
				{
					r.y(minY());
				}
			} else if (side == 2) // z
			{
				if (p.z() > _position.z())
				{
					r.z(maxZ());
				} else
				{
					r.z(minZ());
				}
			}
			return r;
		}

	}

	/**
	 * Returns the vertices of this AABB.
	 * 
	 * @return The vertices
	 */
	public Vec3f[] getVertices()
	{
		if (_vertices == null)
		{
			_vertices = new Vec3f[8];

			for (int i = 0; i < 8; ++i)
			{
				_vertices[i] = new Vec3f();
			}

			recalcVertices();
		}

		return _vertices;
	}

	@Override
	public String toString()
	{
		Vec3f origin = new Vec3f(getPosition());
		origin.sub(getDimensions());
		Vec3f size = new Vec3f(getDimensions());
		size.scale(2.0f);
		return String.format("(%s, %s)", origin, size);
	}

	public void render()
	{
		render(0.0f, 0.0f, 0.0f, 1.0f);
	}

	/**
	 * Renders this AABB.
	 * <p/>
	 * TODO: SLOW!
	 */
	public void render(float r, float g, float b, float a)
	{
		double offset = 0.01;

		glPushMatrix();
		glTranslatef(getPosition().x(), getPosition().y(), getPosition().z());

		glLineWidth(5.0f);
		glColor4f(r, g, b, a);

		// FRONT
		glBegin(GL_LINE_LOOP);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glEnd();

		// BACK
		glBegin(GL_LINE_LOOP);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glEnd();

		// TOP
		glBegin(GL_LINE_LOOP);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glEnd();

		// BOTTOM
		glBegin(GL_LINE_LOOP);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glEnd();

		// LEFT
		glBegin(GL_LINE_LOOP);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(-_dimensions.x() - offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glVertex3d(-_dimensions.x() - offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glEnd();

		// RIGHT
		glBegin(GL_LINE_LOOP);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, -_dimensions.z() - offset);
		glVertex3d(+_dimensions.x() + offset, -_dimensions.y() - offset, +_dimensions.z() + offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, +_dimensions.z() + offset);
		glVertex3d(+_dimensions.x() + offset, +_dimensions.y() + offset, -_dimensions.z() - offset);
		glEnd();
		glPopMatrix();
	}

	/**
	 * Returns true if the AABB contains the given point.
	 * 
	 * @param point
	 *            The point to check for inclusion
	 * @return True if containing
	 */
	public boolean contains(Vec3f point)
	{
		if (maxX() < point.x() || minX() > point.x())
		{
			return false;
		}
		if (maxY() < point.y() || minY() > point.y())
		{
			return false;
		}
		if (maxZ() < point.z() || minZ() > point.z())
		{
			return false;
		}

		return true;
	}

	public float minX()
	{
		return (getPosition().x() - _dimensions.x());
	}

	public float minY()
	{
		return (getPosition().y() - _dimensions.y());
	}

	public float minZ()
	{
		return (getPosition().z() - _dimensions.z());
	}

	public float maxX()
	{
		return (getPosition().x() + _dimensions.x());
	}

	public float maxY()
	{
		return (getPosition().y() + _dimensions.y());
	}

	public float maxZ()
	{
		return (getPosition().z() + _dimensions.z());
	}

	public Vec3f getDimensions()
	{
		return _dimensions;
	}

	public Vec3f getPosition()
	{
		return _position;
	}

	public void setPosition(Vec3f position)
	{
		_position.set(position);
		recalcVertices();
	}

	public void include(AABB aabb)
	{
		float minX = Math.min(aabb.minX(), minX());
		float maxX = Math.max(aabb.maxX(), maxX());

		float minY = Math.min(aabb.minY(), minY());
		float maxY = Math.max(aabb.maxY(), maxY());

		float minZ = Math.min(aabb.minZ(), minZ());
		float maxZ = Math.max(aabb.maxZ(), maxZ());

		float centerX = (minX + maxX) / 2.0f;
		float centerY = (minY + maxY) / 2.0f;
		float centerZ = (minZ + maxZ) / 2.0f;

		getPosition().set(centerX, centerY, centerZ);

		float width = maxX - minX;
		float height = maxY - minY;
		float depth = maxZ - minZ;

		_dimensions.set(width / 2.0f, height / 2.0f, depth / 2.0f);

		recalcVertices();
	}

	public float width()
	{
		return maxX() - minX();
	}

	public float height()
	{
		return maxY() - minY();
	}

	public float depth()
	{
		return maxZ() - minZ();
	}

	public void recalcVertices()
	{
		if (_vertices != null)
		{
			// Front
			_vertices[0].set(minX(), minY(), maxZ());
			_vertices[1].set(maxX(), minY(), maxZ());
			_vertices[2].set(maxX(), maxY(), maxZ());
			_vertices[3].set(minX(), maxY(), maxZ());
			// Back
			_vertices[4].set(minX(), minY(), minZ());
			_vertices[5].set(maxX(), minY(), minZ());
			_vertices[6].set(maxX(), maxY(), minZ());
			_vertices[7].set(minX(), maxY(), minZ());
		}
	}

	public void set(AABB aabb)
	{
		getPosition().set(aabb.getPosition());
		getDimensions().set(aabb.getDimensions());
		recalcVertices();
	}
}
