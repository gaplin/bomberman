package com.mygdx.views;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class LoadingBar extends Actor {
    TextureAtlas.AtlasRegion image;
    public LoadingBar(TextureAtlas.AtlasRegion ar){
        super();
        image = ar;
        this.setWidth(30);
        this.setHeight(25);
        this.setVisible(false);
    }

    public LoadingBar(TextureAtlas.AtlasRegion ar, float width, float height){
        this(ar);
        this.setWidth(width);
        this.setHeight(height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(image, getX(),getY(), getWidth(), getHeight() + 5);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void act(float delta){
        super.act(delta);
    }
}
