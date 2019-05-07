package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;

import java.util.Random;

public class EnemySystem extends IteratingSystem {

    private TextureAtlas atlas;
    private BodyFactory bodyFactory;
    private PooledEngine engine;
    private Random random = new Random();

    public EnemySystem(TextureAtlas atlas , BodyFactory bodyFactory, PooledEngine engine){
        super(Family.all(EnemyComponent.class).get());

        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
        this.engine = engine;

        createEnemy(22.0f ,1.0f, new Color(1.0f, 0.1f, 0.3f, 1));
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        enemy.time -= deltaTime;
        if(enemy.time <= 0){
            int direction = random.nextInt(4);
            state.resetPresses();
            enemy.time = 0.2f;
            switch (direction){
                case 0:
                    state.upPressed = true;
                    break;
                case 1:
                    state.leftPressed = true;
                    break;
                case 2:
                    state.downPressed = true;
                    break;
                case 3:
                    state.rightPressed = true;
                    break;
            }
        }

        float placeBomb = random.nextFloat();
        if(placeBomb < 0.01f){
            state.placeBombJustPressed = true;
        }
        else{
            state.placeBombJustPressed = false;
        }

    }

    public Entity createEnemy(float posX, float posY){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        StatsComponent playerStats = engine.createComponent(StatsComponent.class);
        EnemyComponent enemy = engine.createComponent(EnemyComponent.class);


        body.body = bodyFactory.makeEnemy(posX, posY);
        position.position.set(posX,posY,0);
        type.type = TypeComponent.ENEMY;
        body.body.setUserData(entity);
        stateCom.set(StateComponent.STATE_MOVING_DOWN);
        stateCom.isLooping = true;

        animCom.animations.put(1,
                new Animation<>(0.05f, atlas.findRegions("player/back/Bman_b")));
        animCom.animations.put(2,
                new Animation<>(0.05f, atlas.findRegions("player/side/Bman_s")));
        animCom.animations.put(3,
                new Animation<>(0.05f, atlas.findRegions("player/front/Bman_f")));
        animCom.animations.put(4,
                new Animation<>(0.05f, atlas.findRegions("player/side/Bman_s")));

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);
        entity.add(playerStats);
        entity.add(enemy);


        engine.addEntity(entity);

        return entity;
    }

    public void createEnemy(float posX, float posY, Color color){
        Entity ent = createEnemy(posX, posY);
        TextureComponent texture = Mappers.textureMapper.get(ent);
        texture.color.set(color);
    }
}
