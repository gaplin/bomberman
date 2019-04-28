package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;


public class FlameSystem extends IteratingSystem {

    ComponentMapper<StateComponent> sc;
    ComponentMapper<FlameComponent> fc;
    ComponentMapper<BodyComponent> bc;

    BodyFactory bodyFactory;
    TextureAtlas atlas;

    public FlameSystem(TextureAtlas atlas, BodyFactory bodyFactory) {
        super(Family.all(FlameComponent.class).get());
        this.atlas = atlas;
        this.bodyFactory = bodyFactory;

        sc = ComponentMapper.getFor(StateComponent.class);
        fc = ComponentMapper.getFor(FlameComponent.class);
        bc = ComponentMapper.getFor(BodyComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        FlameComponent flameCom = fc.get(entity);
        StateComponent stateCom = sc.get(entity);

        if(flameCom.duration <= stateCom.time){
            BodyComponent bodyCom = bc.get(entity);
            while(!bodyCom.body.getFixtureList().isEmpty())
                bodyCom.body.destroyFixture(bodyCom.body.getFixtureList().first());
            entity.removeAll();
            getEngine().removeEntity(entity);
        }
    }

    public void createFlame(float posX, float posY) throws Exception{
        if(!MapSystem.checkBlock(posX, posY, "wall"))
            throw new Exception();

        PooledEngine engine = (PooledEngine) getEngine();

        Entity ent = engine.createEntity();

        BodyComponent bodyCom = engine.createComponent(BodyComponent.class);
        TransformComponent positionCom = engine.createComponent(TransformComponent.class);
        TextureComponent textureCom = engine.createComponent(TextureComponent.class);
        CollisionComponent colComp = engine.createComponent(CollisionComponent.class);
        TypeComponent typeCom = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        FlameComponent flameCom = engine.createComponent(FlameComponent.class);

        bodyCom.body = bodyFactory.makeCirclePolyBody(posX, posY, BomberMan.BOMB_RADIUS, BodyDef.BodyType.DynamicBody, true);
        for(Fixture fix : bodyCom.body.getFixtureList())
            fix.setSensor(true);

        bodyCom.body.setUserData(ent);

        positionCom.position.set(posX, posY, -2);

        typeCom.type = TypeComponent.FLAME;

        stateCom.set(StateComponent.STATE_NORMAL);
        stateCom.isMoving = true;
        stateCom.isLooping = true;
        stateCom.time = 0.0f;

        animCom.animations.put(0,
                new Animation<>(0.15f, atlas.findRegions("flame/flame")));

        ent.add(bodyCom);
        ent.add(positionCom);
        ent.add(textureCom);
        ent.add(colComp);
        ent.add(typeCom);
        ent.add(stateCom);
        ent.add(animCom);
        ent.add(flameCom);

        engine.addEntity(ent);
    }
}
