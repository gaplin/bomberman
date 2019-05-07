package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class EnemyComponent implements Component, Pool.Poolable {

    public EnemyComponent(){
        reset();
    }

    public float time;

    @Override
    public void reset() {
        time = 0.0f;
    }
}
