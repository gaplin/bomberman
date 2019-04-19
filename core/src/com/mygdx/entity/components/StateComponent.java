package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;

public class StateComponent implements Component {
    public static final int STATE_MOVING_UP = 1;
    public static final int STATE_MOVING_LEFT = 2;
    public static final int STATE_MOVING_DOWN = 3;
    public static final int STATE_MOVING_RIGHT = 4;


    private int state = 3;
    public float time = 0.0f;
    public boolean isLooping = false;
    public boolean isMoving = false;

    public void set(int newState){
        if(newState != state)
            time = 0.0f;
        state = newState;
    }

    public int get(){
        return state;
    }
}
