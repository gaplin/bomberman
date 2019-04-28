package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.components.*;
import com.mygdx.views.TestScreen;

public class CollisionSystem extends IteratingSystem {

    ComponentMapper<CollisionComponent> cc;
    ComponentMapper<PlayerComponent> pc;
    ComponentMapper<FlameComponent> fc;
    ComponentMapper<BombComponent> bc;
    ComponentMapper<StateComponent> sc;
    ComponentMapper<BodyComponent> bd;
    ComponentMapper<BlockComponent> bcc;


    public CollisionSystem(){
        super(Family.all(CollisionComponent.class).get());

        cc = ComponentMapper.getFor(CollisionComponent.class);
        pc = ComponentMapper.getFor(PlayerComponent.class);
        fc = ComponentMapper.getFor(FlameComponent.class);
        bc = ComponentMapper.getFor(BombComponent.class);
        sc = ComponentMapper.getFor(StateComponent.class);
        bcc = ComponentMapper.getFor(BlockComponent.class);
        bd = ComponentMapper.getFor(BodyComponent.class);
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

        BlockComponent tile = bcc.get(entity);
        if(tile != null && tile.type == BlockComponent.DESTROYABLE){
            BodyComponent body = bd.get(entity);
            body.body.destroyFixture(body.body.getFixtureList().first());
            TestScreen.world.destroyBody(body.body);
            getEngine().removeEntity(entity);
        }

    }

    private void flameBombCollision(Entity flame, Entity bomb){
        StateComponent stateComponent = sc.get(bomb);
        BombComponent bombComponent = bc.get(bomb);
        stateComponent.time = bombComponent.detonationTime;
    }


}
