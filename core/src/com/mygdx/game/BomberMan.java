package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.loader.CustomAssetManager;
import com.mygdx.views.*;

public class BomberMan extends Game {

	private EndScreen endScreen;
	private GameScreen gameScreen;
	private LevelsScreen levelsScreen;
	private LoadingScreen loadingScreen;
	private MenuScreen menuScreen;
	public TestScreen testScreen;
	private PrefernecesScreen prefernecesScreen;

	public final static int MENU = 0;
	public final static int GAME = 1;
	public final static int LEVELS = 2;
	public final static int ENDGAME = 3;
	public final static int TEST = 4;
	public final static int PREFERENCES = 5;

	public static final float GAME_SCALE = 1f;

	public static final float PLAYER_SCALE = 1f;
	public static final float BOMB_SCALE = 1f;
	public static final float SCENERY_SCALE = 1f;

	public final static float STARTING_MOVEMENT_SPEED = 10f;

	public final static int STARTING_BOMB_POWER = 1;
	public final static float PLAYER_RADIUS = 1.4f * GAME_SCALE * PLAYER_SCALE;
	public final static float BOMB_RADIUS = 1.5f * GAME_SCALE * BOMB_SCALE;
	public final static float TILE_WIDTH = 0.9f * GAME_SCALE * SCENERY_SCALE;
	public final static float TILE_HEIGHT = 0.9f * GAME_SCALE * SCENERY_SCALE;

	public static float MENU_VOLUME = 0.1f;

	public static float GAME_VOLUME = 0.2f;


	public CustomAssetManager assMan = new CustomAssetManager();

	@Override
	public void create() {
		loadingScreen = new LoadingScreen(this);
		setScreen(loadingScreen);
	}

	public void changeScreen(int screen){
		switch(screen){
			case MENU:
				if(menuScreen == null)menuScreen = new MenuScreen(this);
				this.setScreen(menuScreen);
				break;
			case GAME:
				if(gameScreen == null)gameScreen = new GameScreen(this);
				this.setScreen(gameScreen);
				break;
			case LEVELS:
				if(levelsScreen == null)levelsScreen = new LevelsScreen(this);
				this.setScreen(levelsScreen);
				break;
			case ENDGAME:
				if(endScreen == null)endScreen = new EndScreen(this);
				this.setScreen(endScreen);
				break;
			case TEST:
				if(testScreen == null)testScreen = new TestScreen(this);
				this.setScreen(testScreen);
				break;
			case PREFERENCES:
				if(prefernecesScreen == null)prefernecesScreen = new PrefernecesScreen(this);
				this.setScreen(prefernecesScreen);
				break;
		}
	}

	@Override
	public void dispose(){
		assMan.manager.dispose();
	}
}
