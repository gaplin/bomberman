package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.BomberMan;

public class PlayerComponent implements Component {
    public static final short defaultMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.FLAME_BIT
                                                | BomberMan.BOMB_BIT;
    public static final short hitMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.BOMB_BIT;

    public static final short cheatMaskBits = 0;

    public float movementSpeed;
    public int bombPower;
    public int HP = 3;

    public boolean canMoveBombs = true;

    public float hitCountDown = 0.0f;

    public boolean gotHit = false;

    public boolean cheat = false;

    public void resetCountDown(){
        hitCountDown = 3.0f;
    }
}
