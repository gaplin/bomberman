package com.mygdx.listeners;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.components.BodyComponent;
import com.mygdx.entity.components.CollisionComponent;
import com.mygdx.entity.components.PlayerComponent;
import com.mygdx.entity.components.TypeComponent;

public class GameContactListener implements ContactListener {
    private ComponentMapper<TypeComponent> typeComponentComponentMapper;

    public GameContactListener(){
        super();
        typeComponentComponentMapper = ComponentMapper.getFor(TypeComponent.class);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fa.getBody().getUserData();
            entityCollision(ent, fb);
            return;
        }
        else if(fb.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fb.getBody().getUserData();
            entityCollision(ent, fa);
            return;
        }
    }

    private void entityCollision(Entity ent, Fixture fb) {
        if(fb.getBody().getUserData() instanceof Entity){
            Entity colEnt = (Entity) fb.getBody().getUserData();

            CollisionComponent col = ent.getComponent(CollisionComponent.class);
            CollisionComponent colb = colEnt.getComponent(CollisionComponent.class);

            if(col != null){
                col.collisionEntity.addLast(colEnt);
            }else if(colb != null){
                colb.collisionEntity.addLast(ent);
            }
        }
    }

    private void entityStopCollision(Entity ent, Fixture fb){
        if(fb.getBody().getUserData() instanceof Entity){
            Entity colEnt = (Entity) fb.getBody().getUserData();
            PlayerComponent player = ent.getComponent(PlayerComponent.class);
            if(player != null)
                player.LastBombs.remove(colEnt);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fa.getBody().getUserData();
            entityStopCollision(ent, fb);
            return;
        }
        else if(fb.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fb.getBody().getUserData();
            entityStopCollision(ent, fa);
            return;
        }
    }

    public boolean beforeContact(Entity ent, Fixture fb){
        if(fb.getBody().getUserData() instanceof Entity){
            Entity colEnt = (Entity) fb.getBody().getUserData();

            BodyComponent bd;

            PlayerComponent player = ent.getComponent(PlayerComponent.class);
            if(player != null) {
                bd = colEnt.getComponent(BodyComponent.class);
            }
            else{
                player = colEnt.getComponent(PlayerComponent.class);
                bd = ent.getComponent(BodyComponent.class);
            }

            if(player == null || bd == null)
                return true;

            if(player.LastBombs.contains(colEnt)) {
                bd.toStatic = true;
                return false;
            }
            if(player.canMoveBombs){
                bd.toDynamic = true;
            }
            else{
                bd.toStatic = true;
            }
        }
        return true;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fa.getBody().getUserData();
            contact.setEnabled(beforeContact(ent, fb));
        }
        else if(fb.getBody().getUserData() instanceof Entity){
            Entity ent = (Entity)fb.getBody().getUserData();
            contact.setEnabled(beforeContact(ent, fa));
        }

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
