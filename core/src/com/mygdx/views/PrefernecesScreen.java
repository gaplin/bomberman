package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.BomberMan;

public class PrefernecesScreen extends ButtonsCount implements Screen {
    private BomberMan parent;

    private Sound buttonSound1;
    private Sound buttonSound2;

    private SpriteBatch sb;

    private Stage stage;

    private Table table, menuTable, gameTable;

    private Skin skin;

    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background, loadingBomb;

    private Label title;

    private final int range = 10;


    public PrefernecesScreen(BomberMan parent){
        super();
        this.parent = parent;
        this.buttonSound1 = parent.assMan.manager.get("sounds/buttonSound.wav");
        this.buttonSound2 = parent.assMan.manager.get("sounds/bombSound.mp3");
        sb = new SpriteBatch();
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
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

        CustomTextButton menuVol = new CustomTextButton("MENU VOLUME", skin, 0, this, buttonSound1);
        CustomTextButton gameVol = new CustomTextButton("GAME VOLUME", skin, 1, this, buttonSound1);
        CustomTextButton back = new CustomTextButton("BACK", skin, "large", 2, this, buttonSound1);
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
                buttonSound2.play(BomberMan.MENU_VOLUME);
            }
        });

        gameVol.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(BomberMan.MENU_VOLUME);
            }
        });

        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(BomberMan.MENU_VOLUME);
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
            buttonSound1.play(BomberMan.MENU_VOLUME);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && pressed == -1) {
            pointer = ((pointer - 1) % nButtons + nButtons) % nButtons;
            buttonSound1.play(BomberMan.MENU_VOLUME);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            if(pointer == 0){
                BomberMan.MENU_VOLUME = Math.max(0.0f, BomberMan.MENU_VOLUME - 0.1f);
            }
            else if(pointer == 1){
                BomberMan.GAME_VOLUME = Math.max(0.0f, BomberMan.GAME_VOLUME - 0.1f);
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            if(pointer == 0){
                BomberMan.MENU_VOLUME = Math.min(1.0f, BomberMan.MENU_VOLUME + 0.1f);
            }
            else if(pointer == 1){
                BomberMan.GAME_VOLUME = Math.min(1.0f, BomberMan.GAME_VOLUME + 0.1f);
            }
        }


        int menuVol = (int)(BomberMan.MENU_VOLUME * 10);
        int gameVol = (int)(BomberMan.GAME_VOLUME * 10);

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
        stage.getViewport().update(width, height);
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
        buttonSound1.dispose();
        buttonSound2.dispose();
    }
}
