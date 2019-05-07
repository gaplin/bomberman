package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class StatsComponent implements Component, Pool.Poolable {

    public StatsComponent(){
        reset();
    }

    public float movementSpeed;
    public int bombPower;
    public int HP;
    public int bombs;
    public boolean canMoveBombs;
    public boolean afterHit;
    public boolean dead;
    public boolean markedToDeath;
    public float deathCountDown;
    public float hitCountDown;
    public boolean gotHit;

    public void resetCountDown(){
        hitCountDown = 3.0f;
    }

    @Override
    public void reset() {
        movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;
        bombPower = BomberMan.STARTING_BOMB_POWER;
        HP = 3;
        bombs = 1;
        canMoveBombs = true;
        afterHit = false;
        dead = false;
        markedToDeath = false;
        deathCountDown = BomberMan.AGONY_TIME;
        gotHit = false;
    }
}
