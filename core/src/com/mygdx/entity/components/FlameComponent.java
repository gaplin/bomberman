package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class FlameComponent implements Component, Pool.Poolable {
    public float duration = 2.0f;

    @Override
    public void reset() {

    }
}
