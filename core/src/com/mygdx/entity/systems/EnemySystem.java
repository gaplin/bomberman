package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;

import java.util.Random;

public class EnemySystem extends IteratingSystem {

    private TextureAtlas atlas;
    private BodyFactory bodyFactory;
    private PooledEngine engine;
    private Random random = new Random();
    private short hitBit;

    public EnemySystem(TextureAtlas atlas , BodyFactory bodyFactory, PooledEngine engine){
        super(Family.all(EnemyComponent.class).get());

        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
        this.engine = engine;
        BomberMan.ENEMY_COUNT = 0;

        createEnemy(1.0f, 1.0f, Color.YELLOW);
        createEnemy(22.0f ,1.0f, Color.RED);
        createEnemy(22.0f, 15.0f, Color.BLUE);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        if(enemy.time == 0){
            enemy.time = 5f;
            setDirection(state, random.nextInt(4));
        }

        enemy.damageUpTimer -= deltaTime;
        enemy.newBombTimer -= deltaTime;

        if(enemy.damageUpTimer <= 0 && stats.bombPower <= 2){
            stats.bombPower++;
            enemy.resetDamageUpTimer();
        }

        if(enemy.newBombTimer <= 0 && stats.bombs <= 2){
            stats.bombs++;
            enemy.resetNewBombTimer();
        }


        if(state.state == StateComponent.STATE_MOVING_RIGHT){
            horizontalScan(entity, 1);
        }
        else if(state.state == StateComponent.STATE_MOVING_LEFT){
            horizontalScan(entity, -1);
        }
        else if(state.state == StateComponent.STATE_MOVING_UP){
            verticalScan(entity, 1);
        }
        else if(state.state == StateComponent.STATE_MOVING_DOWN){
            verticalScan(entity, -1);
        }

        boolean placeBomb = false;
        boolean changeDirection = false;


        switch(hitBit){
            case BomberMan.PLAYER_BIT:
                placeBomb = true;
                changeDirection = true;
                break;
            case BomberMan.DESTRUCTIBLE_BIT:
                placeBomb = true;
                changeDirection = true;
                break;
            case BomberMan.FLAME_BIT:
                changeDirection = true;
                break;
            case BomberMan.INDESTRUCTIBLE_BIT:
                changeDirection = true;
                break;
            case BomberMan.BOMB_BIT:
                changeDirection = true;
                break;
        }

        hitBit = 0;


        state.placeBombJustPressed = placeBomb;

        if(changeDirection)
            setDirection(state, random.nextInt(4));


    }


    private void setDirection(StateComponent state, int direction){
        switch (direction){
            case 0:
                if(state.upPressed) {
                    setDirection(state, random.nextInt(4));
                    return;
                }
                state.resetPresses();
                state.upPressed = true;
                break;
            case 1:
                if(state.leftPressed) {
                    setDirection(state, random.nextInt(4));
                    return;
                }
                state.resetPresses();
                state.leftPressed = true;
                break;
            case 2:
                if(state.downPressed) {
                    setDirection(state, random.nextInt(4));
                    return;
                }
                state.resetPresses();
                state.downPressed = true;
                break;
            case 3:
                if(state.downPressed) {
                    setDirection(state, random.nextInt(4));
                    return;
                }
                state.resetPresses();
                state.rightPressed = true;
                break;
        }
    }

    private void scan(Entity player, Vector2 from, Vector2 to){
        BodyComponent bd = Mappers.bodyMapper.get(player);
        Body body = bd.body;
        World world = body.getWorld();
        Filter bodyFilter = bd.body.getFixtureList().first().getFilterData();
        hitBit = 0;
        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if(fixture.getBody() == body)
                    return 1;

                if((bodyFilter.maskBits & fixture.getFilterData().categoryBits) != 0) {
                    hitBit = fixture.getFilterData().categoryBits;
                    return 0;
                }
                return 1;
            }
        };
        world.rayCast(rayCastCallback, from, to);
    }


    private void verticalScan(Entity entity, float mod){
        TransformComponent transform = Mappers.transformMapper.get(entity);

        float distance = 0.7f * mod;
        float goodPosY = transform.position.y + BomberMan.PLAYER_RADIUS * mod;
        if(mod < 0)
            goodPosY += 0.3f * BomberMan.PLAYER_SCALE;
        float posX = transform.position.x;
        float posY = goodPosY;
        Vector2 newPosition = new Vector2(posX, posY + distance + 0.2f * mod);


        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posX = transform.position.x + 0.7f * BomberMan.PLAYER_SCALE;
        posY = goodPosY - BomberMan.PLAYER_RADIUS / 3f * mod;
        newPosition.set(posX, posY + distance + 0.2f * mod);
        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posX = transform.position.x - 0.7f * BomberMan.PLAYER_SCALE;
        posY = goodPosY - BomberMan.PLAYER_RADIUS / 3f * mod;
        newPosition.set(posX, posY + distance + 0.2f * mod);
        scan(entity, new Vector2(posX, posY), newPosition);
    }

    private void horizontalScan(Entity entity, float mod){
        TransformComponent transform = Mappers.transformMapper.get(entity);

        float distance = 0.7f * mod;
        float posX = transform.position.x + 0.5f * mod;
        float posY = transform.position.y;
        Vector2 newPosition = new Vector2(posX + distance, posY);


        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posY = transform.position.y + BomberMan.PLAYER_RADIUS;
        posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
        newPosition.set(posX + distance, posY);
        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posY = transform.position.y + BomberMan.PLAYER_RADIUS / 2f;
        posX = transform.position.x + 0.9f * BomberMan.PLAYER_SCALE * mod;
        newPosition.set(posX + distance, posY);
        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posY = transform.position.y - (BomberMan.PLAYER_RADIUS + 0.2f * BomberMan.PLAYER_SCALE) / 3f;
        posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
        newPosition.set(posX + distance, posY);
        scan(entity, new Vector2(posX, posY), newPosition);
        if(hitBit != 0)
            return;

        posY = transform.position.y - BomberMan.PLAYER_RADIUS + 0.2f * BomberMan.PLAYER_SCALE;
        posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
        newPosition.set(posX + distance, posY);
        scan(entity, new Vector2(posX, posY), newPosition);
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

        BomberMan.ENEMY_COUNT++;

        return entity;
    }

    public void createEnemy(float posX, float posY, Color color){
        Entity ent = createEnemy(posX, posY);
        TextureComponent texture = Mappers.textureMapper.get(ent);
        texture.color.set(color);
    }
}
