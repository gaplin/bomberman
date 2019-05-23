package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class FlameComponent implements Component, Pool.Poolable {
    public static final float flameTime = 2.0f;
    public float duration;
    public FlameComponent(){
        reset();
    }

    @Override
    public void reset() {
        duration = flameTime;
    }
}
