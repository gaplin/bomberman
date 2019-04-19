package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import java.util.LinkedList;

public class PlayerComponent implements Component {
    public float movementSpeed = 10f;
    public LinkedList<Entity> LastBombs = new LinkedList<>();
    public boolean canMoveBombs = true;
}
