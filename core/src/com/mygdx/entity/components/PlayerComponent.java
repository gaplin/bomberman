package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class PlayerComponent implements Component, Pool.Poolable {
    public static final short defaultMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.FLAME_BIT
                                                | BomberMan.BOMB_BIT | BomberMan.POWER_UP_BIT;
    public static final short hitMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.BOMB_BIT
                                            | BomberMan.POWER_UP_BIT;

    public static final short cheatMaskBits = 0;

    public float movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;
    public int bombPower = BomberMan.STARTING_BOMB_POWER;
    public int HP = 3;
    public int bombs = 1;

    public boolean canMoveBombs = false;

    public float hitCountDown = 0.0f;

    public boolean gotHit = false;

    public boolean cheat = false;

    public void resetCountDown(){
        hitCountDown = 3.0f;
    }

    @Override
    public void reset() {
        movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;
        bombPower = BomberMan.STARTING_BOMB_POWER;
        HP = 3;
        bombs = 1;
        canMoveBombs = false;
        hitCountDown = 0.0f;
        gotHit = false;
        cheat = false;
    }
}
