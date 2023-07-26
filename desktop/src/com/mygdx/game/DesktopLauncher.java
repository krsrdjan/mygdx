package com.mygdx.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(24);
		config.setTitle("My GDX Game");
		config.setWindowedMode(
				GameBoard.BOARD_SQUARE_LENGHT * GameBoard.SQUARE_SIZE,
				GameBoard.BOARD_SQUARE_LENGHT * GameBoard.SQUARE_SIZE
				);
		new Lwjgl3Application(new MyGdxGame(), config);
	}
}
