package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.BomberMan;


public class MenuScreen extends ButtonsCount implements Screen {
    private BomberMan parent;
    private Stage stage;
    private Table table;
    private Skin skin;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background;
    private Sound buttonSound1;
    private Sound buttonSound2;

    public MenuScreen(BomberMan parent){
        super();
        this.parent = parent;
        this.buttonSound1 = parent.assMan.manager.get("sounds/buttonSound.wav");
        this.buttonSound2 = parent.assMan.manager.get("sounds/bombMenu.mp3");
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        parent.assMan.queueAddSkin();
        parent.assMan.manager.finishLoading();
        skin = parent.assMan.manager.get("flat/flat-earth-ui.json");
        atlas= parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
    }
    @Override
    public void show() {
        pointer = 1;
        stage.clear();
        Gdx.input.setInputProcessor(stage);
        table = new Table();
        table.setFillParent(true);
        table.setBackground(new TiledDrawable(background));
        stage.addActor(table);

        CustomTextButton play = new CustomTextButton("PLAY", skin, "large", 1, this, buttonSound1);
        CustomTextButton exit = new CustomTextButton("EXIT", skin, "large", 2, this, buttonSound1);
        nButtons = 2;
        //play.setTouchable(Touchable.disabled);
        //exit.setTouchable(Touchable.disabled);

        table.add(play).fillX().uniformX().width(300);
        table.row().pad(50, 0, 50, 0);
        table.add(exit).fillX().uniformX();

        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(0.01f);
                parent.changeScreen(BomberMan.LEVELS);
            }
        });

        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && pressed == 0) {
            pointer = pointer % nButtons + 1;
            buttonSound1.play(0.03f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && pressed == 0) {
            pointer = pointer % nButtons + 1;
            buttonSound1.play(0.03f);
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
        buttonSound1.dispose();
        buttonSound2.dispose();
    }
}
