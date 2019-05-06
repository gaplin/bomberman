package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class BlockComponent implements Component, Pool.Poolable {
    public static final int WALL = 0;
    public static final int DESTROYABLE = 1;

    public boolean toDestroy = false;
    public int type = 0;

    @Override
    public void reset() {
        type = 0;
        toDestroy = false;
    }
}
