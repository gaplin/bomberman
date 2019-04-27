package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.components.*;

public class CollisionSystem extends IteratingSystem {

    ComponentMapper<CollisionComponent> cc;
    ComponentMapper<PlayerComponent> pc;
    ComponentMapper<FlameComponent> fc;
    ComponentMapper<BombComponent> bc;
    ComponentMapper<StateComponent> sc;


    public CollisionSystem(){
        super(Family.all(CollisionComponent.class).get());

        cc = ComponentMapper.getFor(CollisionComponent.class);
        pc = ComponentMapper.getFor(PlayerComponent.class);
        fc = ComponentMapper.getFor(FlameComponent.class);
        bc = ComponentMapper.getFor(BombComponent.class);
        sc = ComponentMapper.getFor(StateComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionComponent first = cc.get(entity);
        if(first.collisionEntity.isEmpty())
            return;
        Entity block = first.collisionEntity.removeFirst();


        // FLAME
        FlameComponent flame = fc.get(block);
        if(flame != null){
            flameCollision(block, entity);
        }
        flame = fc.get(entity);
        if(flame != null){
            flameCollision(entity, block);
        }


    }

    private void flameCollision(Entity flame, Entity entity){
        BombComponent bomb = bc.get(entity);
        if(bomb != null){
            flameBombCollision(flame, entity);
        }
    }

    private void flameBombCollision(Entity flame, Entity bomb){
        StateComponent stateComponent = sc.get(bomb);
        BombComponent bombComponent = bc.get(bomb);
        stateComponent.time = bombComponent.detonationTime;
    }


}