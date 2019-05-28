package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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


public class ControlsScreen extends ButtonsCount implements Screen {
    private BomberMan parent;
    private Stage stage;
    private Table table, menuControls;
    private Skin skin;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background;

    private Label[] controls = new Label[14];
    private Label[] controlName = new Label[9];
    private Label player1Label;
    private Label player2Label;

    public ControlsScreen(BomberMan parent){
        super();
        this.parent = parent;
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        skin = parent.assMan.manager.get("flat/flat-earth-ui.json");
        atlas= parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
        player1Label = new Label("PLAYER 1", skin, "title", "white");
        player2Label = new Label("PLAYER 2", skin, "title", "white");
        player2Label.setColor(Color.RED);

        controlName[1] = new Label("UP", skin, "title", "white");
        controlName[2] = new Label("DOWN", skin, "title", "white");
        controlName[3] = new Label("LEFT", skin, "title", "white");
        controlName[4] = new Label("RIGHT", skin, "title", "white");
        controlName[5] = new Label("BOMB", skin, "title", "white");
        controlName[6] = new Label("MUTE/UNMUTE", skin, "button", "white");
        controlName[7] = new Label("PAUSE/UNPAUSE", skin, "button", "white");
        controlName[8] = new Label("SHOW/HIDE FPS", skin, "button", "white");

        controls[1] = new Label(Input.Keys.toString(Input.Keys.UP), skin, "title", "white");
        controls[2] = new Label(Input.Keys.toString(Input.Keys.DOWN), skin, "title", "white");
        controls[3] = new Label(Input.Keys.toString(Input.Keys.LEFT), skin, "title", "white");
        controls[4] = new Label(Input.Keys.toString(Input.Keys.RIGHT), skin, "title", "white");
        controls[5] = new Label(Input.Keys.toString(Input.Keys.SPACE), skin, "title", "white");
        controls[6] = new Label(Input.Keys.toString(Input.Keys.W), skin, "title", "white");
        controls[7] = new Label(Input.Keys.toString(Input.Keys.S), skin, "title", "white");
        controls[8] = new Label(Input.Keys.toString(Input.Keys.A), skin, "title", "white");
        controls[9] = new Label(Input.Keys.toString(Input.Keys.D), skin, "title", "white");
        controls[10] = new Label(Input.Keys.toString(Input.Keys.CONTROL_LEFT), skin, "title", "white");
        controls[11] = new Label("M", skin, "button", "white");
        controls[12] = new Label("P", skin, "button", "white");
        controls[13] = new Label("F", skin, "button", "white");

        for(int i = 1; i <= 5; i++){
            controlName[i].setColor(Color.BLACK);
        }

        for(int i = 6; i <= 10; i++){
            controls[i].setColor(Color.RED);
        }
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
        table.top();
        Label label = new Label("BOMB", skin, "title", "white");
        label.setVisible(false);
        table.row().pad(50.0f, 0.0f, 0.0f, 0.0f);
        table.add(label).padRight(50.0f);
        table.add(player1Label).padRight(50.0f);
        table.add(player2Label);


        for(int i = 1; i <= 5; i++){
            table.row().pad(10.0f, 0.0f, 0.0f, 0.0f);
            table.add(controlName[i]).padLeft(-200.0f);
            table.add(controls[i]).padRight(50.0f);
            table.add(controls[i + 5]);
        }


        CustomTextButton back = new CustomTextButton("BACK", skin, "large", 0, this);
        back.setTouchable(Touchable.disabled);
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.soundManager.playSound("bombSound.mp3", "menuVol");
                parent.changeScreen(BomberMan.MENU);
            }
        });

        menuControls = new Table();
        stage.addActor(menuControls);
        menuControls.setWidth(Gdx.graphics.getWidth());
        menuControls.setHeight(300f);

        menuControls.row().pad(0.0f, 0.0f, 0.0f, 0.0f);
        for(int i = 6; i <= 8; i++){
            menuControls.add(controlName[i]).padLeft(-300.0f);
            menuControls.add(controls[i + 5]).padRight(-200.0f);
            menuControls.row().pad(10.0f, 0.0f, 0.0f, 0.0f);

        }

        menuControls.add(back);
        nButtons = 1;
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
    }
}
