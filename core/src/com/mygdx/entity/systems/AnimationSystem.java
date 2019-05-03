package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.AnimationComponent;
import com.mygdx.entity.components.StateComponent;
import com.mygdx.entity.components.TextureComponent;

public class AnimationSystem extends IteratingSystem {

    public AnimationSystem(){
        super(Family.all(TextureComponent.class, AnimationComponent.class,
                StateComponent.class).get());
    }


    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimationComponent ani = Mappers.animationMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);

        if(ani.animations.containsKey(state.get())){
            TextureComponent tex = Mappers.textureMapper.get(entity);
            if(state.get() == StateComponent.STATE_MOVING_LEFT)
                tex.mirror = true;
            else
                tex.mirror = false;
            if(!state.isMoving)
                state.time = 0.0f;
            tex.region = ani.animations.get(state.get()).getKeyFrame(state.time, state.isLooping);
        }

        state.time += deltaTime;
    }
}
