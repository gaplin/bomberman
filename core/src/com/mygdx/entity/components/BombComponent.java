package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.BomberMan;

public class BombComponent implements Component {
    public PlayerComponent owner = null;
    public int range;
    public float detonationTime = 3.0f;
    public static final short defaultMaskBits = BomberMan.BOMB_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.INDESTRUCTIBLE_BIT
            | BomberMan.FLAME_BIT | BomberMan.PLAYER_BIT;
}
