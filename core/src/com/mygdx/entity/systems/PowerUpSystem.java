package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;

public class PowerUpSystem extends IteratingSystem {

    private TextureAtlas atlas;
    private BodyFactory bodyFactory;

    public PowerUpSystem(TextureAtlas atlas, BodyFactory bodyFactory){
        super(Family.all(PowerUpComponent.class).get());

        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PowerUpComponent powerUp = Mappers.powerUpMapper.get(entity);
        powerUp.time -= deltaTime;
        if(powerUp.time <= 0) {
            getEngine().removeEntity(entity);
            return;
        }
        if(powerUp.time <= 3.0f){
            TextureComponent texture = Mappers.textureMapper.get(entity);
            texture.color.a = Math.abs(MathUtils.sin(powerUp.time * 6));
        }
    }

    public void createPowerUp(float posX, float posY, int upType){
        PooledEngine engine = (PooledEngine) getEngine();

        Entity ent = engine.createEntity();

        TypeComponent type = engine.createComponent(TypeComponent.class);
        PowerUpComponent powerUp = engine.createComponent(PowerUpComponent.class);
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        StateComponent state = engine.createComponent(StateComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        BodyComponent body = engine.createComponent(BodyComponent.class);

        type.type = TypeComponent.POWER_UP;

        powerUp.type = upType;

        transform.position.set(posX, posY, -2);

        state.set(StateComponent.STATE_NORMAL);

        switch(upType){
            case PowerUpComponent.bombPowerUp:
                texture.region = atlas.findRegion("powerups/BombPowerup");
                break;
            case PowerUpComponent.speedPowerUp:
                texture.region = atlas.findRegion("powerups/SpeedPowerup");
                break;
            case PowerUpComponent.damagePowerUp:
                texture.region = atlas.findRegion("powerups/FlamePowerup");
                break;
            case PowerUpComponent.kickPowerUp:
                texture.region = atlas.findRegion("powerups/KickPowerup");
                transform.scale.set(2.0f, 2.0f);
                break;
            case PowerUpComponent.hpPowerUp:
                texture.region = atlas.findRegion("powerups/HPPowerup");
                transform.scale.set(2.0f, 2.0f);
                break;
            case PowerUpComponent.godModePowerUp:
                texture.region = atlas.findRegion("powerups/godModePowerup");
                break;
        }

        body.body = bodyFactory.makePowerUp(posX, posY);
        body.body.setUserData(ent);

        ent.add(type);
        ent.add(powerUp);
        ent.add(transform);
        ent.add(state);
        ent.add(texture);
        ent.add(body);

        engine.addEntity(ent);
    }
}
