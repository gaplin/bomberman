package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.BomberMan;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "BomberMan";
		cfg.useGL30 = false;
		cfg.height = 640;
		cfg.width = 830;
		new LwjglApplication(new BomberMan(), cfg);
	}
}
