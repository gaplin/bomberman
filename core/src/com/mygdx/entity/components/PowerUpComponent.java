package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;

public class PowerUpComponent implements Component {
    public static final int bombPowerUp = 0;
    public static final int speedPowerUp = 1;
    public static final int damagePowerUp = 2;
    public static final int kickPowerUp = 3;

    public int type;

    public float time = 10.0f;
}
