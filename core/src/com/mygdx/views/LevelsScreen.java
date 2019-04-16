package com.mygdx.views;

import com.badlogic.gdx.Screen;
import com.mygdx.game.BomberMan;

public class LevelsScreen implements Screen {
    private BomberMan parent;
    public LevelsScreen(BomberMan parent){
        super();
        this.parent = parent;
    }
    @Override
    public void show() {
        parent.changeScreen(BomberMan.GAME);

    }

    @Override
    public void render(float delta) {

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

    }

    @Override
    public void dispose() {

    }
}
