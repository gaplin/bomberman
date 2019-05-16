package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class FlameComponent implements Component, Pool.Poolable {
    public float duration;
    public FlameComponent(){
        reset();
    }

    @Override
    public void reset() {
        duration = 2.0f;
    }
}
