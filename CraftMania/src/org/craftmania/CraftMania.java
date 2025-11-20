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
package org.craftmania;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JOptionPane;

import org.craftmania.game.Game;
import org.craftmania.utilities.SmartRandom;
import org.craftmania.world.World;
import org.craftmania.world.characters.Player;

public class CraftMania
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		initUncaughtExceptionHandler();

		/* Initialize the Game */
		Game game = Game.getInstance();
		game.init();

		/* Construct a new World */
		World world = new World("world_1");
		game.setWorld(world);
		world.getWorldProvider().load();

		/* Load the Player */
		Player player = new Player(world.getWorldProvider().getSpawnPoint());
		player.load();

		world.setPlayer(player);

		/* Start the Game */
		game.startGameLoop();
	}

	private static void initUncaughtExceptionHandler()
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{

			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				try
				{
					e.printStackTrace();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(baos, true, "UTF-8"));

					JOptionPane.showMessageDialog(null, t.getName() + ": " + baos.toString("UTF-8"));
				} catch (Exception e2)
				{
					e2.printStackTrace();
				}

			}
		});
	}
}
