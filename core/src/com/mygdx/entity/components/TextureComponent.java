package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

public class TextureComponent implements Component, Pool.Poolable {
    public TextureRegion region = null;
    public Color color = new Color(1, 1, 1, 1);
    public boolean mirror = false;

    @Override
    public void reset() {
        region = null;
        color.set(1, 1, 1, 1);
        mirror = false;
    }
}
