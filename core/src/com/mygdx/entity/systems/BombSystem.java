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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;
import com.mygdx.manager.SoundManager;


public class BombSystem extends IteratingSystem {


    private BodyFactory bodyFactory;

    private TextureAtlas atlas;

    private SoundManager soundManager;

    private boolean moving = true;

    public BombSystem(TextureAtlas atlas, BodyFactory bodyFactory, SoundManager soundManager){
        super(Family.all(BombComponent.class, StateComponent.class).get());

        this.bodyFactory = bodyFactory;
        this.atlas = atlas;
        this.soundManager = soundManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BodyComponent bombBody = Mappers.bodyMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);

        Body body = bombBody.body;
        Vector2 speed = body.getLinearVelocity();
        if(speed.x < 0.0f && horizontalHit(entity, -1)){
            body.setLinearVelocity(0.0f, 0.0f);
        } else if(speed.x > 0.0f && horizontalHit(entity, 1)){
            body.setLinearVelocity(0.0f, 0.0f);
        } else if(speed.y < 0.0f && verticalHit(entity, -1)){
            body.setLinearVelocity(0.0f, 0.0f);
        } else if(speed.y > 0.0f && verticalHit(entity, 1)){
            body.setLinearVelocity(0.0f, 0.0f);
        }

        BombComponent bomb = Mappers.bombMapper.get(entity);
        bomb.detonationTime -= deltaTime;

        if(BomberMan.CHEATS && Gdx.input.isKeyPressed(Input.Keys.L)){
            bomb.detonationTime = 0.0f;
        }

        if(bomb.detonationTime <= 0.0f) {
            Vector2 pos = new Vector2(transform.position.x, transform.position.y);
            float posX = pos.x = MathUtils.floor(pos.x);
            float posY = MathUtils.floor(pos.y);
            if(posX % 2 == 0)
                posX++;
            if(posY % 2 == 0)
                posY++;


            bombBody.body.getWorld().destroyBody(bombBody.body);
            getEngine().removeEntity(entity);

            Entity player = bomb.owner;
            StatsComponent stats = Mappers.statsMapper.get(player);
            if(stats != null)
                stats.bombs++;

            soundManager.playSound("bombSound.mp3", "gameVol");

            FlameSystem flames = getEngine().getSystem(FlameSystem.class);

            try {
                flames.createFlame(posX, posY);
            } catch(Exception ignored){}

            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(posX - 2.0f * i, posY);
                } catch(Exception e) {
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++) {
                try {
                    flames.createFlame(posX + 2.0f * i, posY);
                } catch (Exception e) {
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(posX, posY - 2.0f * i);
                } catch(Exception e){
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(posX, posY + 2.0f * i);
                } catch(Exception e){
                    break;
                }
            }
        }

    }



    private boolean canMove(Entity entity, Vector2 from, Vector2 to){
        BodyComponent bodyComp = Mappers.bodyMapper.get(entity);
        Body body = bodyComp.body;
        World world = body.getWorld();
        moving = true;
        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if(fixture.getBody() == body)
                    return 1;
                if(fixture.getFilterData().categoryBits == BomberMan.INDESTRUCTIBLE_BIT ||
                 fixture.getFilterData().categoryBits == BomberMan.DESTRUCTIBLE_BIT ||
                fixture.getFilterData().categoryBits == BomberMan.BOMB_BIT
                ){
                    moving = false;
                    return 0;
                }
                return 0;
            }
        };
        world.rayCast(rayCastCallback, from, to);
        return moving;
    }

    public boolean verticalHit(Entity entity, float mod){
        BodyComponent bodyCom = Mappers.bodyMapper.get(entity);
        Body body = bodyCom.body;
        float distance = (BomberMan.BOMB_RADIUS / 2f + 0.5f) * mod;
        float radius = BomberMan.BOMB_RADIUS / 2f;
        float posX = body.getPosition().x;
        float posY = body.getPosition().y;
        Vector2 newPosition = new Vector2(posX, posY + distance);
        return !canMove(entity, body.getPosition(), newPosition) ||
                !canMove(entity, new Vector2(posX + radius, posY),
                        new Vector2(posX + radius, newPosition.y)) ||
                !canMove(entity, new Vector2(posX - radius, posY),
                        new Vector2(posX - radius, newPosition.y));
    }

    public boolean horizontalHit(Entity entity, float mod){
        BodyComponent bodyCom = Mappers.bodyMapper.get(entity);
        Body body = bodyCom.body;
        float distance = (BomberMan.BOMB_RADIUS / 2f + 0.5f) * mod;
        float radius = BomberMan.BOMB_RADIUS / 2f;
        float posX = body.getPosition().x;
        float posY = body.getPosition().y;
        Vector2 newPosition = new Vector2(posX + distance, posY);
        return !canMove(entity, body.getPosition(), newPosition) ||
                !canMove(entity, new Vector2(posX, posY + radius),
                        new Vector2(newPosition.x, posY + radius)) ||
                !canMove(entity, new Vector2(posX, posY - radius),
                        new Vector2(newPosition.x, posY - radius));
    }


    public Entity createBomb(float posX, float posY, Entity player){
        PooledEngine engine = (PooledEngine) getEngine();

        Entity ent = engine.createEntity();
        BodyComponent bodyCom = engine.createComponent(BodyComponent.class);
        TransformComponent positionCom = engine.createComponent(TransformComponent.class);
        TextureComponent textureCom = engine.createComponent(TextureComponent.class);
        TypeComponent typeCom = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        BombComponent bombCom = engine.createComponent(BombComponent.class);

        StatsComponent playerStats = Mappers.statsMapper.get(player);

        bombCom.range = playerStats.bombPower;
        bombCom.owner = player;



        bodyCom.body = bodyFactory.makeBomb(posX, posY);
        bodyCom.body.setUserData(ent);

        positionCom.position.set(posX, posY, 1);

        typeCom.type = TypeComponent.BOMB;

        stateCom.set(StateComponent.STATE_NORMAL);
        stateCom.isMoving = true;


        animCom.animations.put(0,
                new Animation<>(1.0f, atlas.findRegions("bomb/Bomb")));

        ent.add(bodyCom);
        ent.add(positionCom);
        ent.add(textureCom);
        ent.add(typeCom);
        ent.add(stateCom);
        ent.add(animCom);
        ent.add(bombCom);

        engine.addEntity(ent);
        return ent;
    }
}
