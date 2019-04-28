package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
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

public class EndScreen extends ButtonsCount implements Screen {
    private BomberMan parent;
    private Stage stage;
    private Skin skin;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background;
    private Table table;
    private Sound buttonSound1;
    private Sound buttonSound2;
    private Label title;

    public EndScreen(BomberMan parent){
        super();
        this.parent = parent;
        buttonSound1 = parent.assMan.manager.get("sounds/buttonSound.wav");
        buttonSound2 = parent.assMan.manager.get("sounds/bombSound.mp3");
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        skin = parent.assMan.manager.get("flat/flat-earth-ui.json");
        atlas = parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
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

        CustomTextButton newGame = new CustomTextButton("New Game", skin, "large", 0, this, buttonSound1);
        CustomTextButton menu = new CustomTextButton("MENU", skin, "large", 1, this, buttonSound1);
        newGame.setTouchable(Touchable.disabled);
        menu.setTouchable(Touchable.disabled);
        nButtons = 2;

        title = new Label("Game Over", skin, "title", "white");

        table.add(title);
        table.row().pad(50, 0, 0, 0);
        table.add(newGame).fillX().uniformX().width(300);
        table.row().pad(50, 0, 0, 0);
        table.add(menu).fillX().uniformX().width(300);
        table.row().pad(50, 0, 0, 0);

        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(BomberMan.MENU_VOLUME);
                parent.changeScreen(BomberMan.TEST);
            }
        });

        menu.addListener(new ChangeListener() {
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

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
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
        atlas.dispose();
        buttonSound1.dispose();
        buttonSound2.dispose();
    }
}
