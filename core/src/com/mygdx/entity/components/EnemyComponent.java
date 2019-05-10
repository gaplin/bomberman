package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class EnemyComponent implements Component, Pool.Poolable {

    public EnemyComponent(){
        reset();
    }

    public float time;
    public float newBombTimer;
    public float damageUpTimer;

    public void resetNewBombTimer(){
        newBombTimer = 10.0f;
    }

    public void resetDamageUpTimer(){
        damageUpTimer = 15.0f;
    }

    @Override
    public void reset() {
        time = 0.0f;
        resetNewBombTimer();
        resetDamageUpTimer();
    }
}
