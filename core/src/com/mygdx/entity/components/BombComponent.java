package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;

public class BombComponent implements Component {
    public boolean forSomeone = false;
    public int range;
    public float detonationTime = 3.0f;
}
