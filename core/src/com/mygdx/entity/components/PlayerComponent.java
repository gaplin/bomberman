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

    public float hitCountDown = 0.0f;

    public boolean gotHit = false;

    public boolean cheat = false;

    public void resetCountDown(){
        hitCountDown = 3.0f;
    }

    @Override
    public void reset() {
        hitCountDown = 0.0f;
        gotHit = false;
        cheat = false;
    }
}
