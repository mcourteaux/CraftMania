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
package org.craftmania.world.characters;

import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.craftmania.game.Game;

/**
 * 
 * @author martijncourteaux
 */
public class CharacterBody
{

	protected boolean _usingRightHand;
	protected float _progressRightHand;
	protected boolean _dirRightHand;
	protected float _animationSpeedRightHand;
	protected float _blockDistance;

	public void enableUsingRightHand()
	{
		_usingRightHand = true;
	}

	public void disableUsingRightHand()
	{
		_usingRightHand = false;
	}

	public void update()
	{
		updateRightHand();
	}

	public void updateRightHand()
	{
		if (_usingRightHand)
		{
			if (_dirRightHand)
			{
				_progressRightHand -= Game.getInstance().getStep() * _animationSpeedRightHand;
				if (_progressRightHand < 0.0f)
				{
					_dirRightHand = !_dirRightHand;
				}
			} else
			{
				_progressRightHand += Game.getInstance().getStep() * _animationSpeedRightHand;
				if (_progressRightHand > 1.0f)
				{
					_dirRightHand = !_dirRightHand;
				}
			}
		} else
		{
			if (_progressRightHand > 0)
			{
				_progressRightHand -= Game.getInstance().getStep() * _animationSpeedRightHand * 0.9f;
				if (_progressRightHand < 0)
				{
					_dirRightHand = true;
				}
			}
		}
	}

	public void airSmash()
	{
		_usingRightHand = true;
		_progressRightHand = 0.1f;
	}

	public void setBlockDistance(float distance)
	{
		this._blockDistance = distance;
	}

	public void forceDisableUsingRightHand()
	{
		disableUsingRightHand();
		_progressRightHand = 0.0f;
		_dirRightHand = false;
	}

	public void transformToRightHand()
	{
		/* Transform the matix */
		glLoadIdentity();

		glTranslatef(0.2f - _progressRightHand / 10.0f, -0.05f, -0.2f - _progressRightHand / 20.0f * +(_progressRightHand * _blockDistance * 1.2f));
		glRotatef(-65, 0, 1, 0);
		glRotated(_progressRightHand * 50 - 10.0d, 0, 0, 1);
		glRotated(_progressRightHand * 20, 1, 1, 0);
		glRotatef(90, 0, 0, 1);
		glRotatef(20, 0, 1, 0);
	}
}
