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

	public final static int MENU = 0;
	public final static int GAME = 1;
	public final static int LEVELS = 2;
	public final static int ENDGAME = 3;

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
		}
	}

	@Override
	public void dispose(){
		assMan.manager.dispose();
	}
}
