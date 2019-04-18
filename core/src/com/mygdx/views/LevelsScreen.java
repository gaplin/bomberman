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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.BomberMan;

public class LevelsScreen extends ButtonsCount implements Screen {
    private BomberMan parent;
    private Stage stage;
    private Table table;
    private Skin skin;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background;
    private Sound buttonSound1;
    private Sound buttonSound2;

    public LevelsScreen(BomberMan parent){
        super();
        this.parent = parent;
        buttonSound1 = parent.assMan.manager.get("sounds/buttonSound.wav");
        buttonSound2 = parent.assMan.manager.get("sounds/bombMenu.mp3");
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

        CustomTextButton level_1 = new CustomTextButton("LEVEL 1", skin, "large", 0, this, buttonSound1);
        CustomTextButton test = new CustomTextButton("TEST", skin, "large", 1, this, buttonSound1);
        CustomTextButton back = new CustomTextButton("BACK", skin, "large", 2, this, buttonSound1);
        level_1.setTouchable(Touchable.disabled);
        back.setTouchable(Touchable.disabled);
        test.setTouchable(Touchable.disabled);
        nButtons = 3;

        table.add(level_1).fillX().uniformX().width(300);
        table.row().pad(50, 0, 50, 0);
        table.add(test).fillX().uniformX();
        table.row();
        table.add(back).fillX().uniformX();

        level_1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(0.03f);
                parent.changeScreen(BomberMan.GAME);
            }
        });

        test.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(0.03f);
                parent.changeScreen(BomberMan.TEST);
            }
        });

        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(0.03f);
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
            buttonSound1.play(0.03f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && pressed == -1) {
            pointer = ((pointer - 1) % nButtons + nButtons) % nButtons;
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
        atlas.dispose();
        buttonSound1.dispose();
        buttonSound2.dispose();
    }
}
