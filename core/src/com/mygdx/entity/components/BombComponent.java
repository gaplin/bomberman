package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class BombComponent implements Component, Pool.Poolable {
    public Entity owner;
    public int range;
    public float detonationTime;
    public static final short defaultMaskBits = BomberMan.BOMB_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.INDESTRUCTIBLE_BIT
            | BomberMan.FLAME_BIT | BomberMan.PLAYER_BIT;

    public BombComponent(){
        reset();
    }

    @Override
    public void reset() {
        detonationTime = 3.0f;
        owner = null;
        range = 0;
    }
}
