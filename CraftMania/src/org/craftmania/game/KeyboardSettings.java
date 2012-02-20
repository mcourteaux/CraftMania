package org.craftmania.game;

import org.lwjgl.input.Keyboard;

public class KeyboardSettings
{
	
	public static enum KeyboardPreset
	{
		AZERTY, QUERTY
	}
	
	
	/* Walking */
	public static int MOVE_FORWARD;
	public static int MOVE_BACK;
	public static int MOVE_LEFT;
	public static int MOVE_RIGHT;

	/* Others */
	public static int JUMP;
	public static int CROUCH;
	public static int INVENTORY;
	
	/* Debug */
	public static int TOGGLE_LIGHT_POINT;
	public static int TOGGLE_OVERLAY;
	
	public static void initialize(KeyboardPreset preset)
	{
		switch (preset)
		{
		case AZERTY:
			intializeAzerty();
			break;
		case QUERTY:
			intializeQuerty();
		default:
			intializeQuerty();
			break;
		}
	}
	
	public static void intializeAzerty()
	{
		/* Walking */
		MOVE_FORWARD = Keyboard.KEY_Z;
		MOVE_BACK = Keyboard.KEY_S;
		MOVE_LEFT = Keyboard.KEY_Q;
		MOVE_RIGHT = Keyboard.KEY_D;
		
		/* Others */
		JUMP = Keyboard.KEY_SPACE;
		CROUCH = Keyboard.KEY_LSHIFT;
		INVENTORY = Keyboard.KEY_E;
		
		/* Debug */
		TOGGLE_LIGHT_POINT = Keyboard.KEY_L;
		TOGGLE_OVERLAY = Keyboard.KEY_O;
	}
	
	public static void intializeQuerty()
	{
		/* Walking */
		MOVE_FORWARD = Keyboard.KEY_W;
		MOVE_BACK = Keyboard.KEY_S;
		MOVE_LEFT = Keyboard.KEY_A;
		MOVE_RIGHT = Keyboard.KEY_D;
		
		/* Others */
		JUMP = Keyboard.KEY_SPACE;
		CROUCH = Keyboard.KEY_LSHIFT;
		INVENTORY = Keyboard.KEY_E;
		
		/* Debug */
		TOGGLE_LIGHT_POINT = Keyboard.KEY_L;
		TOGGLE_OVERLAY = Keyboard.KEY_O;
	}
}
