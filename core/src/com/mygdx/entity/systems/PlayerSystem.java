package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;


public class PlayerSystem extends IteratingSystem {
    private ComponentMapper<PlayerComponent> pm;
    private ComponentMapper<BodyComponent> bodm;
    private ComponentMapper<StateComponent> sm;
    private ComponentMapper<TransformComponent> tm;
    private TextureAtlas atlas;

    private BodyFactory bodyFactory;
    private PooledEngine engine;

    public PlayerSystem(BodyFactory bodyFactory, TextureAtlas atlas, PooledEngine engine){
        super(Family.all(PlayerComponent.class).get());
        this.bodyFactory = bodyFactory;
        this.atlas = atlas;
        this.engine = engine;

        createPlayer(4.0f, 4.0f);

        pm = ComponentMapper.getFor(PlayerComponent.class);
        bodm = ComponentMapper.getFor(BodyComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
        tm = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BodyComponent body = bodm.get(entity);
        StateComponent state = sm.get(entity);
        PlayerComponent player = pm.get(entity);
        TransformComponent transform = tm.get(entity);
        if(player == null)
            return;

        state.isMoving = body.body.getLinearVelocity().y != 0 || body.body.getLinearVelocity().x != 0;

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            if(checkForCollision(body.body.getWorldCenter(), BomberMan.BOMB_RADIUS / 8f)){
                return;
            }
            getEngine().getSystem(BombSystem.class).createBomb(transform.position.x, transform.position.y, player);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            body.body.setLinearVelocity(0, player.movementSpeed);
            state.set(StateComponent.STATE_MOVING_UP);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            body.body.setLinearVelocity(-player.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_LEFT);

        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            body.body.setLinearVelocity(0, -player.movementSpeed);
            state.set(StateComponent.STATE_MOVING_DOWN);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            body.body.setLinearVelocity(player.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_RIGHT);
        }
        else{
            body.body.setLinearVelocity(0, 0);
        }

    }


    public void createPlayer(float posX, float posY){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        CollisionComponent colComp = engine.createComponent(CollisionComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);


        body.body = bodyFactory.makePlayer(posX, posY, BomberMan.PLAYER_RADIUS, BodyDef.BodyType.DynamicBody, true);
        position.position.set(posX,posY,0);
        type.type = TypeComponent.PLAYER;
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

        player.bombPower = BomberMan.STARTING_BOMB_POWER;
        player.movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(colComp);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);


        engine.addEntity(entity);
    }

    private boolean checkForCollision(Vector2 wh, float r){
        r /= 2f;
        for(Entity entity : getEngine().getEntities()){
            PlayerComponent pl = entity.getComponent(PlayerComponent.class);
            if(pl == null){
                BodyComponent body = entity.getComponent(BodyComponent.class);
                if(body != null){
                    float x = wh.x;
                    float y = wh.y;
                    float x2 = body.body.getWorldCenter().x;
                    float y2 = body.body.getWorldCenter().y;
                    float r2;
                    if(entity.getComponent(BombComponent.class) != null ||
                    entity.getComponent(FlameComponent.class) != null)
                        r2 = BomberMan.BOMB_RADIUS / 2f;
                    else
                        r2 = BomberMan.PLAYER_RADIUS / 2f;
                    float AB = (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
                    float dist = (r - r2) * (r - r2);
                    float sum = (r + r2) * (r + r2);
                    if(AB <= dist || AB < sum)
                        return true;
                }
            }
        }
        return false;
    }

}
