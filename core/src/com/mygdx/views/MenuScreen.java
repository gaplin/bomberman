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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
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
    private Image logo;
    private Skin skin;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background, title;
    private Sound buttonSound1;
    private Sound buttonSound2;
    public MenuScreen(BomberMan parent){
        super();
        this.parent = parent;
        this.buttonSound1 = parent.assMan.manager.get("sounds/buttonSound.wav");
        this.buttonSound2 = parent.assMan.manager.get("sounds/bombSound.mp3");
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        parent.assMan.queueAddSkin();
        parent.assMan.manager.finishLoading();
        skin = parent.assMan.manager.get("flat/flat-earth-ui.json");
        atlas= parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
        title = atlas.findRegion("logo");
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

        CustomTextButton play = new CustomTextButton("PLAY", skin, "large", 0, this, buttonSound1);
        CustomTextButton preferences = new CustomTextButton("PREFERENCES", skin, "large", 1, this, buttonSound1);
        CustomTextButton exit = new CustomTextButton("EXIT", skin, "large", 2, this, buttonSound1);

        nButtons = 3;
        play.setTouchable(Touchable.disabled);
        exit.setTouchable(Touchable.disabled);
        preferences.setTouchable(Touchable.disabled);

        ImageButton logo = new ImageButton(new TiledDrawable(title));

        table.add(logo);
        table.row().pad(50, 0, 0, 0);
        table.add(play).fillX().uniformX().width(400);
        table.row().pad(50, 0, 0, 0);
        table.add(preferences).fillX().uniformX().width(400);
        table.row().pad(50, 0, 0, 0);
        table.add(exit).fillX().uniformX().width(400);

        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(BomberMan.MENU_VOLUME);
                parent.changeScreen(BomberMan.LEVELS);
            }
        });

        preferences.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound2.play(BomberMan.MENU_VOLUME);
                parent.changeScreen(BomberMan.PREFERENCES);
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
        buttonSound1.dispose();
        buttonSound2.dispose();
    }
}
