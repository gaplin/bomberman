package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.BodyComponent;
import com.mygdx.entity.components.TransformComponent;
import com.mygdx.entity.components.TypeComponent;
import com.mygdx.game.BomberMan;

public class PhysicsSystem extends IteratingSystem {

    private static final float MAX_STEP_TIME = 1/60f;
    private static float accumulator = 0f;

    private World world;
    private Array<Entity> bodiesQueue;

    public PhysicsSystem(World world){
        super(Family.all(BodyComponent.class, TransformComponent.class).get());
        this.world = world;
        this.bodiesQueue = new Array<>();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        bodiesQueue.add(entity);
    }

    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        if(accumulator >= MAX_STEP_TIME) {
            world.step(MAX_STEP_TIME, 6, 2);
            accumulator -= MAX_STEP_TIME;


            for (Entity entity : bodiesQueue) {
                TransformComponent tfm = Mappers.transformMapper.get(entity);
                BodyComponent bodyComp = Mappers.bodyMapper.get(entity);
                if(bodyComp == null || tfm == null )
                    continue;
                TypeComponent type = Mappers.typeMapper.get(entity);
                Vector2 position = bodyComp.body.getPosition();

                Vector2 oldPosition = new Vector2(tfm.position.x, tfm.position.y);
                Vector2 oldGridPosition = MapSystem.toGridPosition(oldPosition);

                MapSystem mapSystem = getEngine().getSystem(MapSystem.class);


                tfm.position.x = position.x;
                tfm.position.y = position.y;

                if(type.type == TypeComponent.PLAYER || type.type == TypeComponent.ENEMY){
                    tfm.position.x += 1f * BomberMan.PLAYER_SCALE;
                    tfm.position.y += 1.45f * BomberMan.PLAYER_SCALE;
                }

                if(type.type != TypeComponent.PLAYER && type.type != TypeComponent.ENEMY) {
                    mapSystem.grid[(int)oldGridPosition.y]
                            [(int)oldGridPosition.x].type = TypeComponent.OTHER;
                    Vector2 newGridPosition = MapSystem.toGridPosition(tfm.position);
                    if(!newGridPosition.equals(oldGridPosition)){
                        getEngine().getSystem(EnemySystem.class).notifyEnemies();
                    }
                    mapSystem.grid[(int) newGridPosition.y][(int) newGridPosition.x].type = type.type;
                }
            }
            bodiesQueue.clear();
        }

    }
}
