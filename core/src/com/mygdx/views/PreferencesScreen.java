package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.mygdx.game.BomberMan;

public class PreferencesScreen extends ButtonsCount implements Screen {
    private BomberMan parent;

    private SpriteBatch sb;

    private Stage stage;

    private Table table, menuTable, gameTable;

    private Skin skin;

    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background, loadingBomb;

    private Label title;

    private final int range = 10;


    public PreferencesScreen(BomberMan parent){
        super();
        this.parent = parent;
        sb = new SpriteBatch();
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        stage = new Stage(BomberMan.gameViewPort);
        skin = parent.assMan.manager.get("flat/flat-earth-ui.json");
        atlas = parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
        loadingBomb = atlas.findRegion("loading-bomb");
    }



    @Override
    public void show() {
        super.pointer = 0;
        stage.clear();
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        table.setBackground(new TiledDrawable(background));
        stage.addActor(table);

        CustomTextButton menuVol = new CustomTextButton("MENU VOLUME", skin, 0, this);
        CustomTextButton gameVol = new CustomTextButton("GAME VOLUME", skin, 1, this);
        CustomTextButton back = new CustomTextButton("BACK", skin, "large", 2, this);
        nButtons = 3;
        menuVol.setTouchable(Touchable.disabled);
        menuVol.setPressable(false);
        gameVol.setTouchable(Touchable.disabled);
        gameVol.setPressable(false);
        back.setTouchable(Touchable.disabled);

        menuTable = new Table();
        gameTable = new Table();
        for(int i = 0; i < range; i++){
            menuTable.add(new LoadingBar(loadingBomb, 30f, 25f));
            gameTable.add(new LoadingBar(loadingBomb, 30f, 25f));
        }

        title = new Label("PREFERENCES", skin, "title", "white");


        table.add(title).colspan(2);
        table.row().pad(50, 0, 0, 50);
        table.add(menuVol).fillX().uniformX().width(250);
        table.add(menuTable);
        table.row().pad(50, 0, 0, 50);
        table.add(gameVol).fillX().uniformX().width(250);
        table.add(gameTable);
        table.row().pad(50, 0, 0, 50);
        table.add(back).colspan(2);


        menuVol.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.soundManager.playSound("bombSound.mp3", "menuVol");
            }
        });

        gameVol.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.soundManager.playSound("bombSound.mp3", "menuVol");
            }
        });

        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.soundManager.playSound("bombSound.mp3", "menuVol");
                parent.changeScreen(BomberMan.MENU);
            }
        });

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && pressed == -1) {
            pointer = (pointer + 1) % nButtons;
            parent.soundManager.playSound("buttonSound.wav", "menuVol");
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && pressed == -1) {
            pointer = ((pointer - 1) % nButtons + nButtons) % nButtons;
            parent.soundManager.playSound("buttonSound.wav", "menuVol");
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            if(pointer == 0){
                float prev = parent.prefs.getFloat("menuVol", BomberMan.MENU_VOLUME);
                parent.soundManager.decreaseVolume("menuVol");
                float current = parent.prefs.getFloat("menuVol", BomberMan.MENU_VOLUME);
                if(prev != current)
                    parent.soundManager.playSound("bombSound.mp3", "menuVol");
            }
            else if(pointer == 1){
                float prev = parent.prefs.getFloat("gameVol", BomberMan.MENU_VOLUME);
                parent.soundManager.decreaseVolume("gameVol");
                float current = parent.prefs.getFloat("gameVol", BomberMan.MENU_VOLUME);
                if(prev != current)
                    parent.soundManager.playSound("bombSound.mp3", "gameVol");
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            if(pointer == 0){
                float prev = parent.prefs.getFloat("menuVol", BomberMan.MENU_VOLUME);
                parent.soundManager.increaseVolume("menuVol");
                float current = parent.prefs.getFloat("menuVol", BomberMan.MENU_VOLUME);
                if(prev != current)
                    parent.soundManager.playSound("bombSound.mp3", "menuVol");
            }
            else if(pointer == 1){
                float prev = parent.prefs.getFloat("gameVol", BomberMan.MENU_VOLUME);
                parent.soundManager.increaseVolume("gameVol");
                float current = parent.prefs.getFloat("gameVol", BomberMan.MENU_VOLUME);
                if(prev != current)
                    parent.soundManager.playSound("bombSound.mp3", "gameVol");
            }
        }


        int menuVol = (int)(parent.prefs.getFloat("menuVol", BomberMan.MENU_VOLUME) * 10);
        int gameVol = (int)(parent.prefs.getFloat("gameVol", BomberMan.GAME_VOLUME) * 10);


        for(int i = 0; i < menuVol; i++){
            menuTable.getCells().get(i).getActor().setVisible(true);
        }
        for(int i = range - 1; i >= menuVol; i--){
            menuTable.getCells().get(i).getActor().setVisible(false);
        }

        for(int i = 0; i < gameVol; i++){
            gameTable.getCells().get(i).getActor().setVisible(true);
        }

        for(int i = range - 1; i >= gameVol; i--){
            gameTable.getCells().get(i).getActor().setVisible(false);
        }


        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        stage.clear();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
