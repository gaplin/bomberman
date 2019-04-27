package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Queue;

public class CollisionComponent implements Component {
    public Queue<Entity> collisionEntity = new Queue<>();
}
