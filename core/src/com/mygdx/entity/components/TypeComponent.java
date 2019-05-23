package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class TypeComponent implements Component, Pool.Poolable {
    public static final int PLAYER = 0;
    public static final int ENEMY = 1;
    public static final int BOMB = 2;
    public static final int FLAME = 3;
    public static final int INDESTRUCTIBLE_BLOCK = 4;
    public static final int DESTRUCTIBLE_BLOCK = 5;
    public static final int POWER_UP = 6;
    public static final int SCENERY = 7;
    public static final int OTHER = 8;

    public int type = OTHER;

    @Override
    public void reset() {
        type = OTHER;
    }
}