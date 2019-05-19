package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.BodyComponent;
import com.mygdx.entity.components.BombComponent;
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
                    if(type.type == TypeComponent.BOMB){
                        setBomb(entity, oldGridPosition, TypeComponent.OTHER);
                    }
                    mapSystem.grid[(int)oldGridPosition.y]
                            [(int)oldGridPosition.x].type = TypeComponent.OTHER;
                    Vector2 newGridPosition = MapSystem.toGridPosition(tfm.position);
                    if(type.type == TypeComponent.BOMB){
                        setBomb(entity, newGridPosition, TypeComponent.FLAME);
                        if(!oldGridPosition.equals(newGridPosition)) {
                            getEngine().getSystem(EnemySystem.class).notifyEnemies();
                        }
                    }
                    mapSystem.grid[(int) newGridPosition.y][(int) newGridPosition.x].type = type.type;
                }
            }
            bodiesQueue.clear();
        }

    }

    public void setBomb(Entity entity, Vector2 position, int newType){
        BombComponent bomb = Mappers.bombMapper.get(entity);
        int range = bomb.range;
        int posX = (int)position.x;
        int posY = (int)position.y;
        setX(posX, posY, range, 1, newType);
        setX(posX, posY, range, -1, newType);
        setY(posX, posY, range, 1, newType);
        setY(posX, posY, range, -1, newType);
    }

    private void setX(int posX, int posY, int range, int mod, int newType){
        MapSystem.MapObjs[][] grid = getEngine().getSystem(MapSystem.class).grid;
        for(int i = 1; i <= range; i++){
            int type = grid[posY][posX + i * mod].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK ||
                    type == TypeComponent.DESTRUCTIBLE_BLOCK){
                break;
            }
            grid[posY][posX + i * mod].type = newType;
        }
    }

    private void setY(int posX, int posY, int range, int mod, int newType){
        MapSystem.MapObjs[][] grid = getEngine().getSystem(MapSystem.class).grid;
        for(int i = 1; i <= range; i++){
            int type = grid[posY + i * mod][posX].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK ||
                    type == TypeComponent.DESTRUCTIBLE_BLOCK){
                break;
            }
            grid[posY + i * mod][posX].type = newType;
        }
    }

}
