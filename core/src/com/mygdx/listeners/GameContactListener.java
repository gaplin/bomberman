package com.mygdx.listeners;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.game.BomberMan;

public class GameContactListener implements ContactListener {

    public GameContactListener(){
        super();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        //FLAME
        if(fA.getFilterData().categoryBits == BomberMan.FLAME_BIT){
            flameContact((Entity)fA.getBody().getUserData(), fB);
        }
        if(fB.getFilterData().categoryBits == BomberMan.FLAME_BIT){
            flameContact((Entity)fB.getBody().getUserData(), fA);
        }
        //POWERUP
        if(fA.getFilterData().categoryBits == BomberMan.POWER_UP_BIT){
            powerUpContact((Entity)fA.getBody().getUserData(), fB);
        }
        if(fB.getFilterData().categoryBits == BomberMan.POWER_UP_BIT){
            powerUpContact((Entity)fB.getBody().getUserData(), fA);
        }
    }

    private void flameContact(Entity flame, Fixture second){
        if(!(second.getBody().getUserData() instanceof Entity))
            return;

        Entity entity = (Entity)second.getBody().getUserData();
        switch(second.getFilterData().categoryBits){
            case BomberMan.DESTRUCTIBLE_BIT:
                BlockComponent block = Mappers.blockMapper.get(entity);
                block.toDestroy = true;
                break;
            case BomberMan.BOMB_BIT:
                BombComponent bomb = Mappers.bombMapper.get(entity);
                StateComponent state = Mappers.stateMapper.get(entity);
                state.time = bomb.detonationTime;
                break;
            case BomberMan.PLAYER_BIT:
                PlayerComponent player = Mappers.playerMapper.get(entity);
                if(!player.gotHit) {
                    player.resetCountDown();
                    player.gotHit = true;
                }
                break;
        }
    }

    private void powerUpContact(Entity powerUp, Fixture second){
        if(!(second.getBody().getUserData() instanceof Entity))
        return;


        Entity ent = (Entity) second.getBody().getUserData();

        PowerUpComponent upgrade = Mappers.powerUpMapper.get(powerUp);
        StatsComponent stats = Mappers.statsMapper.get(ent);
        if(upgrade.time <= 0.0f)
            return;


        switch(upgrade.type){
            case PowerUpComponent.bombPowerUp:
                stats.bombs++;
                break;
            case PowerUpComponent.speedPowerUp:
                if(stats.movementSpeed < 15.0f)
                    stats.movementSpeed += 1.0f;
                break;
            case PowerUpComponent.damagePowerUp:
                stats.bombPower++;
                break;
            case PowerUpComponent.kickPowerUp:
                stats.canMoveBombs = true;
                break;
        }

        upgrade.time = 0.0f;
    }

    @Override
    public void endContact(Contact contact) {
    }



    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
