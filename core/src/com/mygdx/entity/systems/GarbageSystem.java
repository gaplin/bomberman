package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.BlockComponent;
import com.mygdx.entity.components.BodyComponent;

import java.util.Random;

public class GarbageSystem extends IteratingSystem {


    Random generator = new Random();



    public GarbageSystem(){
        super(Family.all(BlockComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BlockComponent block = Mappers.blockMapper.get(entity);
        if(block.toDestroy){
            Vector3 pos = Mappers.transformMapper.get(entity).position;
            BodyComponent body = Mappers.bodyMapper.get(entity);
            body.body.getWorld().destroyBody(body.body);
            getEngine().removeEntity(entity);


            float drop = generator.nextFloat();
            if(drop <= 0.4f) {
                int type = generator.nextInt(4);
                getEngine().getSystem(PowerUpSystem.class).createPowerUp(pos.x, pos.y, type);
            }
        }
    }
}
