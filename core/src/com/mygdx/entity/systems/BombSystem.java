package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;


public class BombSystem extends IteratingSystem {

    private ComponentMapper<BombComponent> bm;
    private ComponentMapper<StateComponent> sc;
    private ComponentMapper<BodyComponent> bc;

    private BodyFactory bodyFactory;

    private TextureAtlas atlas;

    private Sound explosionSound;

    public BombSystem(TextureAtlas atlas, BodyFactory bodyFactory, Sound explosionSound){
        super(Family.all(BombComponent.class, StateComponent.class).get());

        bm = ComponentMapper.getFor(BombComponent.class);
        sc = ComponentMapper.getFor(StateComponent.class);
        bc = ComponentMapper.getFor(BodyComponent.class);

        this.bodyFactory = bodyFactory;
        this.atlas = atlas;
        this.explosionSound = explosionSound;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateComponent bombState = sc.get(entity);
        BombComponent bomb = bm.get(entity);
        if(bombState.time >= bomb.detonationTime) {
            BodyComponent bombBody = bc.get(entity);
            Vector2 pos = bombBody.body.getWorldCenter();
            while(!bombBody.body.getFixtureList().isEmpty()){
                bombBody.body.destroyFixture(bombBody.body.getFixtureList().first());
            }

            entity.removeAll();
            getEngine().removeEntity(entity);

            explosionSound.play(BomberMan.GAME_VOLUME);

            FlameSystem flames = getEngine().getSystem(FlameSystem.class);

            try {
                flames.createFlame(pos.x, pos.y);
            } catch(Exception ignored){}

            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(pos.x - (BomberMan.BOMB_RADIUS) * i, pos.y);
                } catch(Exception e) {
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++) {
                try {
                    flames.createFlame(pos.x + (BomberMan.BOMB_RADIUS) * i, pos.y);
                } catch (Exception e) {
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(pos.x, pos.y - (BomberMan.BOMB_RADIUS) * i);
                } catch(Exception e){
                    break;
                }
            }
            for(int i = 1; i <= bomb.range; i++){
                try {
                    flames.createFlame(pos.x, pos.y + (BomberMan.BOMB_RADIUS) * i);
                } catch(Exception e){
                    break;
                }
            }
        }

    }

    public void createBomb(float posX, float posY, PlayerComponent player){
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
}
