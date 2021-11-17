package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ControlsComponent implements Component, Pool.Poolable {

    public ControlsComponent(){
    }

    public int UP;
    public int DOWN;
    public int LEFT;
    public int RIGHT;
    public int PLACE_BOMB;

    public void setControls(int UP, int DOWN, int LEFT, int RIGHT, int PLACE_BOMB){
        this.UP = UP;
        this.DOWN = DOWN;
        this.LEFT = LEFT;
        this.RIGHT = RIGHT;
        this.PLACE_BOMB = PLACE_BOMB;
    }

    @Override
    public void reset() {
    }
}
