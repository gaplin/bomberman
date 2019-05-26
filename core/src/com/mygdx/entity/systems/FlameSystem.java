package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;


public class FlameSystem extends IteratingSystem {


    BodyFactory bodyFactory;
    TextureAtlas atlas;

    public FlameSystem(TextureAtlas atlas, BodyFactory bodyFactory) {
        super(Family.all(FlameComponent.class).get());
        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        FlameComponent flameCom = Mappers.flameMapper.get(entity);
        flameCom.duration -= deltaTime;

        if(flameCom.duration <= 0.0f){

            getEngine().removeEntity(entity);
        }
    }

    public void createFlame(float posX, float posY) throws Exception{
        PooledEngine engine = (PooledEngine) getEngine();

        if(!engine.getSystem(MapSystem.class).checkBlock(posX, posY, BlockComponent.WALL))
            throw new Exception();

        Entity ent = engine.createEntity();

        BodyComponent bodyCom = engine.createComponent(BodyComponent.class);
        TransformComponent positionCom = engine.createComponent(TransformComponent.class);
        TextureComponent textureCom = engine.createComponent(TextureComponent.class);
        TypeComponent typeCom = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        FlameComponent flameCom = engine.createComponent(FlameComponent.class);

        bodyCom.body = bodyFactory.makeFlame(posX, posY);
        for(Fixture fix : bodyCom.body.getFixtureList())
            fix.setSensor(true);

        bodyCom.body.setUserData(ent);

        positionCom.position.set(posX, posY, -2);

        typeCom.type = TypeComponent.FLAME;


        stateCom.set(StateComponent.STATE_NORMAL);
        stateCom.isMoving = true;
        stateCom.isLooping = true;

        animCom.animations.put(0,
                new Animation<>(0.15f, atlas.findRegions("flame/flame")));

        ent.add(bodyCom);
        ent.add(positionCom);
        ent.add(textureCom);
        ent.add(typeCom);
        ent.add(stateCom);
        ent.add(animCom);
        ent.add(flameCom);

        engine.addEntity(ent);

        if(!engine.getSystem(MapSystem.class).checkBlock(posX, posY, BlockComponent.DESTROYABLE))
            throw new Exception();
    }
}
