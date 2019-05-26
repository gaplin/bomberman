package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.loader.CustomAssetManager;
import com.mygdx.manager.SoundManager;
import com.mygdx.views.*;

public class BomberMan extends Game {

	private EndScreen endScreen;
	public GameScreen gameScreen;
	private LevelsScreen levelsScreen;
	private LoadingScreen loadingScreen;
	private MenuScreen menuScreen;

	@Override
	public void render() {
		super.render();
		if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
			soundManager.setMuted(!soundManager.isMuted());
			if(soundManager.isMuted()){
				unMuted.setVisible(false);
				muted.setVisible(true);
			}
			else{
				muted.setVisible(false);
				unMuted.setVisible(true);
			}
		}
		if(getScreen() != loadingScreen){
			stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
			stage.draw();
		}
	}

	private PreferencesScreen preferencesScreen;

	public final static int MENU = 0;
	public final static int GAME = 1;
	public final static int LEVELS = 2;
	public final static int ENDGAME = 3;
	public final static int PREFERENCES = 4;

	public static final short PLAYER_BIT = 1;
	public static final short BOMB_BIT = 1 << 1;
	public static final short DESTRUCTIBLE_BIT = 1 << 2;
	public static final short INDESTRUCTIBLE_BIT = 1 << 3;
	public static final short FLAME_BIT = 1 << 4;
	public static final short POWER_UP_BIT = 1 << 5;


	private static final float GAME_SCALE = 1.0f;

	public static final float PLAYER_SCALE = 0.65f * GAME_SCALE;
	public static final float BOMB_SCALE = 0.9f * GAME_SCALE;
	public static final float SCENERY_SCALE = 1f * GAME_SCALE;

	public final static float STARTING_MOVEMENT_SPEED = 10f;
	public final static int STARTING_BOMB_POWER = 1;
	public final static float AGONY_TIME = 2.0f;
	public static int PLAYER_COUNT = 0;
	public static int ENEMY_COUNT = 0;

	public final static float PLAYER_RADIUS = 1.4f * PLAYER_SCALE;
	public final static float BOMB_RADIUS = 1.5f * BOMB_SCALE;
	public final static float TILE_WIDTH = 0.9f * SCENERY_SCALE;
	public final static float TILE_HEIGHT = 0.9f * SCENERY_SCALE;

	public static float MENU_VOLUME = 0.1f;

	public static float GAME_VOLUME = 0.2f;

	public static int BOTS = 0;


	public static final boolean CHEATS = false;

	public Preferences prefs;
	public CustomAssetManager assMan;
	public SoundManager soundManager;

	private Stage stage;
	private Image muted;
	private Image unMuted;

	@Override
	public void create() {
		prefs = Gdx.app.getPreferences("options");
		if(prefs.getFloat("gameVol", GAME_VOLUME) > 1.0f)
			prefs.putFloat("gameVol", 1.0f);
		if(prefs.getFloat("gameVol", GAME_VOLUME) < 0.0f)
			prefs.putFloat("gameVol", 0.0f);
		if(prefs.getFloat("menuVol", MENU_VOLUME) > 1.0f)
			prefs.putFloat("menuVol", 1.0f);
		if(prefs.getFloat("menuVol", MENU_VOLUME) < 0.0f)
			prefs.putFloat("menuVol", 0.0f);
		prefs.flush();

		assMan = new CustomAssetManager();
		loadingScreen = new LoadingScreen(this);
		setScreen(loadingScreen);
		soundManager = new SoundManager(this);

		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		Gdx.input.setInputProcessor(stage);
		TextureAtlas atlas = assMan.manager.get("loading/loading.atlas");
		unMuted = new Image(atlas.findRegion("speaker"));
		unMuted.setScale(0.4f);
		unMuted.setPosition(Gdx.graphics.getWidth() - 35.0f, Gdx.graphics.getHeight() - 35.0f);
		muted = new Image(atlas.findRegion("mute"));
		muted.setScale(0.4f);
		muted.setPosition(Gdx.graphics.getWidth() - 35.0f, Gdx.graphics.getHeight() - 35.0f);
		muted.setVisible(false);
		stage.addActor(unMuted);
		stage.addActor(muted);
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
			case PREFERENCES:
				if(preferencesScreen == null) preferencesScreen = new PreferencesScreen(this);
				this.setScreen(preferencesScreen);
				break;
		}
	}

	@Override
	public void dispose(){
		assMan.manager.dispose();
		stage.dispose();
	}
}
