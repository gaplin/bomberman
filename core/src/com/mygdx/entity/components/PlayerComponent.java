package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.BomberMan;

public class PlayerComponent implements Component {
    public static final short defaultMaskBits = BomberMan.INDESTRUCTIBLE_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.FLAME_BIT
                                                | BomberMan.BOMB_BIT;
    public float movementSpeed;
    public int bombPower;
    public boolean canMoveBombs = true;
}
