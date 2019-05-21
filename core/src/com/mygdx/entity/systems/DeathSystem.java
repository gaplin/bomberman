package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.game.BomberMan;
import com.mygdx.views.GameScreen;

public class DeathSystem extends IteratingSystem {

    public DeathSystem(){
        super(Family.all(StatsComponent.class).get());
    }
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = Mappers.statsMapper.get(entity);
        BodyComponent bodyComp = Mappers.bodyMapper.get(entity);
        Body body = bodyComp.body;

        if(stats.markedToDeath){
            StateComponent state = Mappers.stateMapper.get(entity);
            body.setLinearVelocity(0.0f, 0.0f);

            state.isMoving = false;
            state.set(StateComponent.STATE_MOVING_DOWN);

            Filter filter = new Filter();
            filter.maskBits = 0;

            for(Fixture fix : body.getFixtureList()){
                fix.setFilterData(filter);
            }

            stats.markedToDeath = false;
            stats.dead = true;
            return;
        }
        if(stats.dead){
            TextureComponent texture = Mappers.textureMapper.get(entity);
            if(stats.deathCountDown > 0.0f){
                stats.deathCountDown -= deltaTime;
                texture.color.a = Math.max(stats.deathCountDown / BomberMan.AGONY_TIME - 0.3f, 0.0f);
            }
            else{
                int type = Mappers.typeMapper.get(entity).type;
                TransformComponent transform = Mappers.transformMapper.get(entity);

                Vector2 gridPosition = MapSystem.toGridPosition(transform.position);
                MapSystem mapSystem = getEngine().getSystem(MapSystem.class);
                mapSystem.grid[(int)gridPosition.y][(int)gridPosition.x].type = TypeComponent.OTHER;

                body.getWorld().destroyBody(body);
                getEngine().removeEntity(entity);
                if(type == TypeComponent.PLAYER) {
                    BomberMan.PLAYER_COUNT--;
                    if (BomberMan.PLAYER_COUNT == 0) {
                        GameScreen.endGame();
                    }
                }
                else if(type == TypeComponent.ENEMY){
                    BomberMan.ENEMY_COUNT--;
                    if(BomberMan.ENEMY_COUNT == 0) {
                        GameScreen.endGame();
                    }
                }
            }
        }
    }
}
