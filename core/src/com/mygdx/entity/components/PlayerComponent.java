package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class PlayerComponent implements Component, Pool.Poolable {
    public static final short defaultMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.FLAME_BIT
                                                | BomberMan.BOMB_BIT | BomberMan.POWER_UP_BIT;
    public static final short hitMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.BOMB_BIT
                                            | BomberMan.POWER_UP_BIT;

    public static final short cheatMaskBits = 0;

    public PlayerComponent(){
        reset();
    }

    public float hitCountDown;

    public boolean gotHit;

    public boolean cheat;

    public void resetCountDown(){
        hitCountDown = 3.0f;
    }

    public int UP = Input.Keys.UP;
    public int DOWN = Input.Keys.DOWN;
    public int LEFT = Input.Keys.LEFT;
    public int RIGHT = Input.Keys.RIGHT;
    public int PLACE_BOMB = Input.Keys.SPACE;

    public void setControls(int UP, int DOWN, int LEFT, int RIGHT, int PLACE_BOMB){
        this.UP = UP;
        this.DOWN = DOWN;
        this.LEFT = LEFT;
        this.RIGHT = RIGHT;
        this.PLACE_BOMB = PLACE_BOMB;
    }

    @Override
    public void reset() {
        hitCountDown = 0.0f;
        gotHit = false;
        cheat = false;

        UP = Input.Keys.UP;
        DOWN = Input.Keys.DOWN;
        LEFT = Input.Keys.LEFT;
        RIGHT = Input.Keys.RIGHT;
        PLACE_BOMB = Input.Keys.SPACE;
    }
}
