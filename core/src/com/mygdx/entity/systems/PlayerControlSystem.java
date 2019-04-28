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


public class PlayerControlSystem extends IteratingSystem {
    ComponentMapper<PlayerComponent> pm;
    ComponentMapper<BodyComponent> bodm;
    ComponentMapper<StateComponent> sm;
    ComponentMapper<TransformComponent> tm;
    TextureAtlas atlas;

    BodyFactory bodyFactory;

    public PlayerControlSystem(BodyFactory bodyFactory, TextureAtlas atlas){
        super(Family.all(PlayerComponent.class).get());
        this.bodyFactory = bodyFactory;
        this.atlas = atlas;
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
            if(checkForCollision(body.body.getWorldCenter(), 0.01f)){
                return;
            }
            createBomb(transform.position.x, transform.position.y, player);
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

    private void createBomb(float posX, float posY, PlayerComponent player){
        PooledEngine engine = (PooledEngine) getEngine();

        Entity ent = engine.createEntity();
        BodyComponent bodyCom = engine.createComponent(BodyComponent.class);
        TransformComponent positionCom = engine.createComponent(TransformComponent.class);
        TextureComponent textureCom = engine.createComponent(TextureComponent.class);
        CollisionComponent colComp = engine.createComponent(CollisionComponent.class);
        TypeComponent typeCom = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        BombComponent bombCom = engine.createComponent(BombComponent.class);

        bombCom.forSomeone = true;

        bombCom.range = player.bombPower;



        bodyCom.body = bodyFactory.makeCirclePolyBody(posX, posY, BomberMan.BOMB_RADIUS, BodyDef.BodyType.DynamicBody, true);
        bodyCom.body.setUserData(ent);

        positionCom.position.set(posX, posY, 1);


        typeCom.type = TypeComponent.BOMB;

        stateCom.set(StateComponent.STATE_NORMAL);
        stateCom.isMoving = true;
        stateCom.time = 0.0f;
        stateCom.isLooping = false;

        animCom.animations.put(0,
                new Animation<>(1.0f, atlas.findRegions("bomb/Bomb")));
        player.LastBombs.add(ent);

        ent.add(bodyCom);
        ent.add(positionCom);
        ent.add(textureCom);
        ent.add(colComp);
        ent.add(typeCom);
        ent.add(stateCom);
        ent.add(animCom);
        ent.add(bombCom);

        engine.addEntity(ent);
    }

    private boolean checkForCollision(Vector2 wh, float r){
        r /= 2;
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
                    if(entity.getComponent(BombComponent.class) != null)
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
