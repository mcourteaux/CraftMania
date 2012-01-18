package org.craftmania;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.craftmania.game.Game;
import org.craftmania.world.World;
import org.craftmania.world.characters.Player;

public class CraftMania
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
 
		loadNativeLibs();
		initUncaughtExceptionHandler();

		/* Initialize the Game */
		Game game = Game.getInstance();
		game.init();

		/* Construct a new World */
		World world = new World("world", System.nanoTime());
		game.setWorld(world);
		world.getWorldProvider().load();

		world.setPlayer(new Player(world.getWorldProvider().getInitialSpawnPoint()));

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
				e.printStackTrace();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos, true));

				JOptionPane.showMessageDialog(null, t.getName() + ": " + baos.toString());

			}
		});
	}

	private static void loadNativeLibs() throws Exception
	{
		if (System.getProperty("os.name").equals("Mac OS X"))
		{
			addLibraryPath("natives/macosx");
		} else if (System.getProperty("os.name").equals("Linux"))
		{
			addLibraryPath("natives/linux");
		} else
		{
			addLibraryPath("natives/windows");
		}
	}

	private static void addLibraryPath(String s) throws Exception
	{
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		final String[] paths = (String[]) usrPathsField.get(null);

		for (String path : paths)
		{
			if (path.equals(s))
			{
				return;
			}
		}

		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = s;
		usrPathsField.set(null, newPaths);
	}

}
