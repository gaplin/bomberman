package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;
import com.mygdx.views.GameScreen;


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

        createPlayer(1.0f, 1.0f);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        TextureComponent texture = Mappers.textureMapper.get(entity);
        BodyComponent body = Mappers.bodyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        PlayerComponent player = Mappers.playerMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);


        if(player.gotHit){
            player.gotHit = false;
            player.HP--;
            if(player.HP == 0){
                GameScreen.endGame();
            }
            Filter filter = new Filter();
            filter.categoryBits = BomberMan.PLAYER_BIT;
            filter.maskBits = PlayerComponent.hitMaskBits;
            for(Fixture fix : body.body.getFixtureList()){
                fix.setFilterData(filter);
            }
        }

        if(player.hitCountDown > 0.0f){
            texture.color.set(1, 1, 1, 0.05f + Math.abs(MathUtils.sin(player.hitCountDown * 6)));
            player.hitCountDown -= deltaTime;
            if(player.hitCountDown <= 0.0f){
                Filter filter = new Filter();
                filter.categoryBits = BomberMan.PLAYER_BIT;
                filter.maskBits = PlayerComponent.defaultMaskBits;
                for(Fixture fix : body.body.getFixtureList()){
                    fix.setFilterData(filter);
                }
                texture.color.set(1, 1, 1, 1);
            }
        }


        state.isMoving = body.body.getLinearVelocity().y != 0 || body.body.getLinearVelocity().x != 0;

        if(player.bombs > 0 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            if(checkForCollision(new Vector2(transform.position.x, transform.position.y), BomberMan.BOMB_RADIUS / 2f)){
                return;
            }
            getEngine().getSystem(BombSystem.class).createBomb(transform.position.x, transform.position.y, player);
            player.bombs--;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.UP) && !verticalHit(entity, 1)){
            body.body.setLinearVelocity(0, player.movementSpeed);
            state.set(StateComponent.STATE_MOVING_UP);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && !horizontalHit(entity, -1)){
            body.body.setLinearVelocity(-player.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_LEFT);

        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && !verticalHit(entity, -1)){
            body.body.setLinearVelocity(0, -player.movementSpeed);
            state.set(StateComponent.STATE_MOVING_DOWN);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !horizontalHit(entity, 1)){
            body.body.setLinearVelocity(player.movementSpeed, 0);
            state.set(StateComponent.STATE_MOVING_RIGHT);
        }
        else{
            body.body.setLinearVelocity(0, 0);
        }

        if(BomberMan.CHEATS && Gdx.input.isKeyJustPressed(Input.Keys.C)){
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
        PlayerComponent playerComp = Mappers.playerMapper.get(player);
        BodyComponent bd = Mappers.bodyMapper.get(player);
        Body body = bd.body;
        World world = body.getWorld();
        move = true;
        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if(fixture.getBody() == body)
                    return 1;
                if(fixture.getFilterData().categoryBits == BomberMan.BOMB_BIT){
                    move = false;
                    if(playerComp.canMoveBombs){
                        BombSystem bombSystem = getEngine().getSystem(BombSystem.class);
                        Entity bomb = (Entity)fixture.getBody().getUserData();
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
        TransformComponent transform = Mappers.transformMapper.get(entity);
        if(player.cheat)
            return false;
        float distance = BomberMan.PLAYER_RADIUS * mod;
        float posX = transform.position.x;
        float posY = transform.position.y;
        Vector2 newPosition = new Vector2(posX, posY + distance);
        Vector2 bombSpeed = new Vector2(0.0f, (player.movementSpeed + 2.0f) * mod);
        return !canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed);
    }

    public boolean horizontalHit(Entity entity, float mod){
        PlayerComponent player = Mappers.playerMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        if(player.cheat)
            return false;
        float distance = BomberMan.PLAYER_RADIUS * mod;
        float posX = transform.position.x;
        float posY = transform.position.y;
        Vector2 newPosition = new Vector2(posX + distance, posY);
        Vector2 bombSpeed = new Vector2((player.movementSpeed + 2.0f) * mod, 0.0f);
        return !canMove(entity, new Vector2(posX, posY), newPosition, bombSpeed);
    }

    public void createPlayer(float posX, float posY){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);


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

        player.bombPower = BomberMan.STARTING_BOMB_POWER;
        player.movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);


        engine.addEntity(entity);
    }

    private boolean checkForCollision(Vector2 wh, float r){
        r /= 2f;
        for(Entity entity : getEngine().getEntities()){
            PlayerComponent pl = Mappers.playerMapper.get(entity);
            if(pl == null){
                BodyComponent body = Mappers.bodyMapper.get(entity);
                if(body != null && Mappers.bombMapper.get(entity) != null){
                    float x = wh.x;
                    float y = wh.y;
                    float x2 = body.body.getWorldCenter().x;
                    float y2 = body.body.getWorldCenter().y;
                    float r2;
                    if(Mappers.bodyMapper.get(entity) != null ||
                    Mappers.flameMapper.get(entity) != null)
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
