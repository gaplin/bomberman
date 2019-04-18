package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mygdx.entity.components.BodyComponent;
import com.mygdx.entity.components.PlayerComponent;
import com.mygdx.entity.components.StateComponent;

public class PlayerControlSystem extends IteratingSystem {
    ComponentMapper<PlayerComponent> pm;
    ComponentMapper<BodyComponent> bodm;
    ComponentMapper<StateComponent> sm;

    public PlayerControlSystem(){
        super(Family.all(PlayerComponent.class).get());
        pm = ComponentMapper.getFor(PlayerComponent.class);
        bodm = ComponentMapper.getFor(BodyComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BodyComponent body = bodm.get(entity);
        StateComponent state = sm.get(entity);
        PlayerComponent player = pm.get(entity);

        if(body.body.getLinearVelocity().y != 0 || body.body.getLinearVelocity().x != 0)
            state.set(StateComponent.STATE_MOVING);
        else
            state.set(StateComponent.STATE_NORMAL);


        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            body.body.setLinearVelocity(0, player.movementSpeed);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            body.body.setLinearVelocity(-player.movementSpeed, 0);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            body.body.setLinearVelocity(0, -player.movementSpeed);
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            body.body.setLinearVelocity(player.movementSpeed, 0);
        }
        else{
            body.body.setLinearVelocity(0, 0);
        }

    }
}
