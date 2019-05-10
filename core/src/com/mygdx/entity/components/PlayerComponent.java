package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.BomberMan;

public class PlayerComponent implements Component, Pool.Poolable {
    public static final short defaultMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.FLAME_BIT
                                                | BomberMan.PLAYER_BIT | BomberMan.POWER_UP_BIT | BomberMan.BOMB_BIT;
    public static final short hitMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT
                                            | BomberMan.POWER_UP_BIT | BomberMan.BOMB_BIT;

    public static final short cheatMaskBits = 0;

    public boolean cheat;

    public PlayerComponent(){
        reset();
    }

    @Override
    public void reset() {
        cheat = false;
    }
}
