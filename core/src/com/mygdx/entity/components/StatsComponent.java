package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class StatsComponent implements Component, Pool.Poolable {

    public float movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;
    public int bombPower = BomberMan.STARTING_BOMB_POWER;
    public int HP = 3;
    public int bombs = 1;
    public boolean canMoveBombs = false;

    @Override
    public void reset() {
        movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;
        bombPower = BomberMan.STARTING_BOMB_POWER;
        HP = 3;
        bombs = 1;
        canMoveBombs = false;
    }
}
