package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.ControlsComponent;
import com.mygdx.entity.components.StateComponent;

public class PlayerControlSystem extends IteratingSystem {

    public PlayerControlSystem(){
        super(Family.all(ControlsComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ControlsComponent controls = Mappers.controlsMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        if(Gdx.input.isKeyPressed(controls.UP)){
            state.upPressed = true;
        }
        else{
            state.upPressed = false;
        }
        if(Gdx.input.isKeyPressed(controls.DOWN)){
            state.downPressed = true;
        }
        else{
            state.downPressed = false;
        }
        if(Gdx.input.isKeyPressed(controls.LEFT)){
            state.leftPressed = true;
        }
        else{
            state.leftPressed = false;
        }
        if(Gdx.input.isKeyPressed(controls.RIGHT)){
            state.rightPressed = true;
        }
        else{
            state.rightPressed = false;
        }
        if(Gdx.input.isKeyJustPressed(controls.PLACE_BOMB)){
            state.placeBombJustPressed = true;
        }
        else{
            state.placeBombJustPressed = false;
        }
    }
}
