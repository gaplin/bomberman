package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;


public class PlayerSystem extends IteratingSystem {
    private TextureAtlas atlas;

    private BodyFactory bodyFactory;
    private PooledEngine engine;
    private boolean move = false;

    public PlayerSystem(BodyFactory bodyFactory, TextureAtlas atlas, PooledEngine engine){
        super(Family.all(PlayerComponent.class).get());
        this.bodyFactory = bodyFactory;
        this.atlas = atlas;
        this.engine = engine;

        BomberMan.PLAYER_COUNT = 0;

        createPlayer(1.0f, 15.0f);

    /*    createPlayer(1.0f, 1.0f,
                Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.F, Color.BLUE);*/

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        TextureComponent texture = Mappers.textureMapper.get(entity);
        BodyComponent body = Mappers.bodyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        PlayerComponent player = Mappers.playerMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        StatsComponent playerStats = Mappers.statsMapper.get(entity);
        TypeComponent typeComp = Mappers.typeMapper.get(entity);

        float posX = MathUtils.floor(transform.position.x);
        float posY = MathUtils.floor(transform.position.y);
        if(posX % 2 == 0)
            posX++;
        if(posY % 2 == 0)
            posY++;

        if(playerStats.dead) {
            return;
        }


        if(playerStats.gotHit){
            playerStats.gotHit = false;
            playerStats.afterHit = true;
            playerStats.HP--;
            if(playerStats.HP == 0){
                playerStats.markedToDeath = true;
                return;
            }
            Filter filter = new Filter();
            filter.categoryBits = BomberMan.PLAYER_BIT;
            filter.maskBits = PlayerComponent.hitMaskBits;
            for(Fixture fix : body.body.getFixtureList()){
                fix.setFilterData(filter);
            }
        }

        if(playerStats.hitCountDown > 0.0f){
            texture.color.a = 0.05f + Math.abs(MathUtils.sin(playerStats.hitCountDown * 6));
            playerStats.hitCountDown -= deltaTime;
            if(playerStats.hitCountDown <= 0.0f){
                Filter filter = new Filter();
                filter.categoryBits = BomberMan.PLAYER_BIT;
                filter.maskBits = PlayerComponent.defaultMaskBits;
                for(Fixture fix : body.body.getFixtureList()){
                    fix.setFilterData(filter);
                }
                playerStats.afterHit = false;
                texture.color.a = 1;
            }
        }


        state.isMoving = body.body.getLinearVelocity().y != 0 || body.body.getLinearVelocity().x != 0;

        if((playerStats.bombs > 0 || player.cheat) && state.placeBombJustPressed){
            if(checkForCollision(new Vector2(posX, posY))){
                return;
            }
            Entity ent = getEngine().getSystem(BombSystem.class).createBomb(posX, posY, entity);
            getEngine().getSystem(PhysicsSystem.class).setBomb(ent, MapSystem.toGridPosition(transform.position), TypeComponent.FLAME);
            getEngine().getSystem(EnemySystem.class).notifyEnemies();
            playerStats.bombs--;
        }

        if(state.upPressed && !verticalHit(entity, 1)){
            body.body.setLinearVelocity(0, playerStats.movementSpeed);
            state.set(StateComponent.STATE_MOVING_UP);
        }
        else if(state.leftPressed && !horizontalHit(entity, -1)){
            body.body.setLinearVelocity(-playerStats.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_LEFT);

        }
        else if(state.downPressed && !verticalHit(entity, -1)){
            body.body.setLinearVelocity(0, -playerStats.movementSpeed);
            state.set(StateComponent.STATE_MOVING_DOWN);
        }
        else if(state.rightPressed && !horizontalHit(entity, 1)){
            body.body.setLinearVelocity(playerStats.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_RIGHT);
        }
        else{
            body.body.setLinearVelocity(0, 0);
        }

        if(BomberMan.CHEATS && Gdx.input.isKeyJustPressed(Input.Keys.C)){
            int type = Mappers.typeMapper.get(entity).type;
            if(type == TypeComponent.ENEMY)
                return;
            player.cheat = !player.cheat;
            Filter filter = new Filter();
            if(player.cheat) {
                filter.maskBits = PlayerComponent.cheatMaskBits;
            }
            else{
                filter.maskBits = PlayerComponent.defaultMaskBits;
            }
            filter.categoryBits = BomberMan.PLAYER_BIT;
            for(Fixture fix : body.body.getFixtureList()){
                fix.setFilterData(filter);
            }
        }

    }



    private boolean canMove(Entity player, Vector2 from, Vector2 to, Vector2 bombSpeed){
        StatsComponent playerStats = Mappers.statsMapper.get(player);
        BodyComponent bd = Mappers.bodyMapper.get(player);
        int type = Mappers.typeMapper.get(player).type;
        Body body = bd.body;
        World world = body.getWorld();
        move = true;
        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if(fixture.getBody() == body)
                    return 1;
                if(fixture.getFilterData().categoryBits == BomberMan.BOMB_BIT){
                    Entity bomb = (Entity)fixture.getBody().getUserData();
                    Body body = Mappers.bodyMapper.get(bomb).body;
                    if(!body.getLinearVelocity().equals(new Vector2(0, 0))){
                        return 0;
                    }
                    move = false;
                    if(playerStats.canMoveBombs){
                        BombSystem bombSystem = getEngine().getSystem(BombSystem.class);
                        if(bombSpeed.x < 0 && bombSystem.horizontalHit(bomb, -1)){
                            return 0;
                        }
                        else if(bombSpeed.x > 0 && bombSystem.horizontalHit(bomb, 1)){
                            return 0;
                        }
                        else if(bombSpeed.y < 0 && bombSystem.verticalHit(bomb, -1)){
                            return 0;
                        }
                        else if(bombSpeed.y > 0 && bombSystem.verticalHit(bomb, 1)){
                            return 0;
                        }
                        fixture.getBody().setLinearVelocity(bombSpeed);
                    }
                    return 0;
                }
                return 1;
            }
        };
        world.rayCast(rayCastCallback, from, to);
        return move;
    }


    public boolean verticalHit(Entity entity, float mod){
        PlayerComponent player = Mappers.playerMapper.get(entity);
        StatsComponent playerStats = Mappers.statsMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        if(player.cheat)
            return false;
        float distance = 0.2f * mod;
        float goodPosY = transform.position.y + BomberMan.PLAYER_RADIUS * mod;
        if(mod < 0)
            goodPosY += 0.3f * BomberMan.PLAYER_SCALE;
        float posX = transform.position.x;
        float posY = goodPosY;

        Vector2 newPosition = new Vector2(posX, posY + distance + 0.2f * mod);
        Vector2 bombSpeed = new Vector2(0.0f, (playerStats.movementSpeed + 2.0f) * mod);

        boolean result = false;

        PhysicsDebugSystem.start4.setZero();
        PhysicsDebugSystem.end4.setZero();
        PhysicsDebugSystem.start5.setZero();
        PhysicsDebugSystem.end5.setZero();
        PhysicsDebugSystem.start.set(posX, posY);
        PhysicsDebugSystem.end.set(newPosition);
        if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
            result = true;

        posX = transform.position.x + 0.7f * BomberMan.PLAYER_SCALE;
        posY = goodPosY - BomberMan.PLAYER_RADIUS / 3f * mod;
        newPosition.set(posX, posY + distance + 0.2f * mod);
        PhysicsDebugSystem.start2.set(posX, posY);
        PhysicsDebugSystem.end2.set(newPosition);
        if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
            result = true;

        posX = transform.position.x - 0.7f * BomberMan.PLAYER_SCALE;
        posY = goodPosY - BomberMan.PLAYER_RADIUS / 3f * mod;
        newPosition.set(posX, posY + distance + 0.2f * mod);
        PhysicsDebugSystem.start3.set(posX, posY);
        PhysicsDebugSystem.end3.set(newPosition);
        if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
            result = true;

        return result;
    }

    public boolean horizontalHit(Entity entity, float mod){
        StatsComponent playerStats = Mappers.statsMapper.get(entity);
        PlayerComponent player = Mappers.playerMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        if(player.cheat)
            return false;
        float distance = 0.2f * mod;
        float posX = transform.position.x + 0.5f * mod;
        float posY = transform.position.y;
        Vector2 newPosition = new Vector2(posX + distance, posY);
        Vector2 bombSpeed = new Vector2((playerStats.movementSpeed + 2.0f) * mod, 0.0f);

        boolean result = false;

        PhysicsDebugSystem.start.set(posX, posY);
        PhysicsDebugSystem.end.set(newPosition);
        if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
            result = true;
         posY = transform.position.y + BomberMan.PLAYER_RADIUS;
         posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
         newPosition.set(posX + distance, posY);
         PhysicsDebugSystem.start2.set(posX, posY);
         PhysicsDebugSystem.end2.set(newPosition);
         if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
             result = true;

         posY = transform.position.y + BomberMan.PLAYER_RADIUS / 2f;
         posX = transform.position.x + 0.9f * BomberMan.PLAYER_SCALE * mod;
         newPosition.set(posX + distance, posY);
         PhysicsDebugSystem.start5.set(posX, posY);
         PhysicsDebugSystem.end5.set(newPosition);
         if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
             result = true;

         posY = transform.position.y - (BomberMan.PLAYER_RADIUS + 0.2f * BomberMan.PLAYER_SCALE) / 3f;
         posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
         newPosition.set(posX + distance, posY);
         PhysicsDebugSystem.start3.set(posX, posY);
         PhysicsDebugSystem.end3.set(newPosition);
         if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
             result = true;

         posY = transform.position.y - BomberMan.PLAYER_RADIUS + 0.2f * BomberMan.PLAYER_SCALE;
         posX = transform.position.x + distance - (distance - 0.4f * BomberMan.PLAYER_SCALE * mod);
         newPosition.set(posX + distance, posY);
         PhysicsDebugSystem.start4.set(posX, posY);
         PhysicsDebugSystem.end4.set(newPosition);
         if(!canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed))
             result = true;

        return result;
    }

    public Entity createPlayer(float posX, float posY){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        StatsComponent playerStats = engine.createComponent(StatsComponent.class);
        ControlsComponent controls = engine.createComponent(ControlsComponent.class);


        body.body = bodyFactory.makePlayer(posX, posY);
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

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);
        entity.add(playerStats);
        entity.add(controls);


        engine.addEntity(entity);

        BomberMan.PLAYER_COUNT++;

        return entity;
    }

    public void createPlayer(float posX, float posY, int UP, int DOWN, int LEFT, int RIGHT, int PLACE_BOMB, Color color){
        Entity entity = createPlayer(posX, posY);
        ControlsComponent controls = Mappers.controlsMapper.get(entity);
        TextureComponent texture = Mappers.textureMapper.get(entity);
        controls.setControls(UP, DOWN, LEFT, RIGHT, PLACE_BOMB);
        texture.color.set(color);
    }

    private boolean checkForCollision(Vector2 wh){
        for(Entity entity : engine.getEntitiesFor(Family.one(BombComponent.class, FlameComponent.class).get())){
            for(Fixture fix : Mappers.bodyMapper.get(entity).body.getFixtureList()){
                if(fix.testPoint(wh.x, wh.y))
                    return true;
            }
        }
        return false;
    }
}
