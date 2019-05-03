package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.BlockComponent;
import com.mygdx.entity.components.BodyComponent;

public class GarbageSystem extends IteratingSystem {

    public GarbageSystem(){
        super(Family.all(BlockComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BlockComponent block = Mappers.blockMapper.get(entity);
        if(block.toDestroy){
            BodyComponent body = Mappers.bodyMapper.get(entity);
            body.body.getWorld().destroyBody(body.body);
            getEngine().removeEntity(entity);
        }
    }
}
