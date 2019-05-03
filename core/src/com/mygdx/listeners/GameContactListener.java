package com.mygdx.listeners;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.BlockComponent;
import com.mygdx.entity.components.BombComponent;
import com.mygdx.entity.components.StateComponent;
import com.mygdx.game.BomberMan;
import com.mygdx.views.GameScreen;

public class GameContactListener implements ContactListener {

    public GameContactListener(){
        super();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        if(fA.getFilterData().categoryBits == BomberMan.FLAME_BIT){
            flameContact((Entity)fA.getBody().getUserData(), fB);
        }
        if(fB.getFilterData().categoryBits == BomberMan.FLAME_BIT){
            flameContact((Entity)fB.getBody().getUserData(), fA);
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
                GameScreen.endGame();
                break;
        }
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
