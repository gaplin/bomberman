package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.components.AnimationComponent;
import com.mygdx.entity.components.StateComponent;
import com.mygdx.entity.components.TextureComponent;

public class AnimationSystem extends IteratingSystem {
    ComponentMapper<TextureComponent> tm;
    ComponentMapper<AnimationComponent> am;
    ComponentMapper<StateComponent> sm;

    public AnimationSystem(){
        super(Family.all(TextureComponent.class, AnimationComponent.class,
                StateComponent.class).get());

        tm = ComponentMapper.getFor(TextureComponent.class);
        am = ComponentMapper.getFor(AnimationComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
    }


    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimationComponent ani = am.get(entity);
        StateComponent state = sm.get(entity);

        if(ani.animations.containsKey(state.get())){
            TextureComponent tex = tm.get(entity);
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
