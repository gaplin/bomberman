package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.entity.components.BodyComponent;
import com.mygdx.entity.components.FlameComponent;
import com.mygdx.entity.components.StateComponent;


public class FlameSystem extends IteratingSystem {

    ComponentMapper<StateComponent> sc;
    ComponentMapper<FlameComponent> fc;
    ComponentMapper<BodyComponent> bc;

    public FlameSystem() {
        super(Family.all(FlameComponent.class).get());

        sc = ComponentMapper.getFor(StateComponent.class);
        fc = ComponentMapper.getFor(FlameComponent.class);
        bc = ComponentMapper.getFor(BodyComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        FlameComponent flameCom = fc.get(entity);
        StateComponent stateCom = sc.get(entity);

        if(flameCom.duration <= stateCom.time){
            BodyComponent bodyCom = bc.get(entity);
            while(!bodyCom.body.getFixtureList().isEmpty())
                bodyCom.body.destroyFixture(bodyCom.body.getFixtureList().first());
            entity.removeAll();
            getEngine().removeEntity(entity);
        }
    }
}
