package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

public class TextureComponent implements Component, Pool.Poolable {

    public TextureComponent(){
        reset();
    }

    public TextureRegion region;
    public Color color;
    public boolean mirror;

    @Override
    public void reset() {
        region = null;
        if(color == null)
            color = new Color();

        color.set(1, 1, 1, 1);
        mirror = false;
    }
}
