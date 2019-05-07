package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class StateComponent implements Component, Pool.Poolable {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_MOVING_UP = 1;
    public static final int STATE_MOVING_LEFT = 2;
    public static final int STATE_MOVING_DOWN = 3;
    public static final int STATE_MOVING_RIGHT = 4;


    public int state = 3;
    public float time = 0.0f;
    public boolean isLooping = false;
    public boolean isMoving = false;

    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;
    public boolean placeBombJustPressed;

    public void set(int newState){
        if(newState != state)
            time = 0.0f;
        state = newState;
    }

    public int get(){
        return state;
    }

    public void resetPresses(){
        upPressed = false;
        leftPressed = false;
        downPressed = false;
        rightPressed = false;
    }

    @Override
    public void reset() {
        time = 0.0f;
        state = 3;
        isLooping = false;
        isMoving = false;

        upPressed = false;
        leftPressed = false;
        downPressed = false;
        rightPressed = false;
        placeBombJustPressed = false;
    }
}
