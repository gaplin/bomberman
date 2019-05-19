package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;


public class EnemySystem extends IteratingSystem {

    private TextureAtlas atlas;
    private BodyFactory bodyFactory;
    private PooledEngine engine;

    public EnemySystem(TextureAtlas atlas , BodyFactory bodyFactory, PooledEngine engine){
        super(Family.all(EnemyComponent.class).get());

        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
        this.engine = engine;
        BomberMan.ENEMY_COUNT = 0;

        createEnemy(2f, 2.0f, Color.YELLOW);
        createEnemy(23.0f,2.0f, Color.RED);
        createEnemy(23.0f, 16.0f, Color.BLUE);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);

        if(enemy.correctingX){
            correctX(entity);
            return;
        }

        if(enemy.correctingY){
            correctY(entity);
            return;
        }

        if(!enemy.moving){
            calculateMove(entity);
        }
        else{
            move(entity);
        }

    }

    private static class node{
        MapSystem.MapObjs obj;
        node prev;

        public node(){}
        public node(MapSystem.MapObjs obj, node prev){
            this.obj = obj;
            this.prev = prev;
        }
    }



    private void calculateMove(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        MapSystem mapSystem = getEngine().getSystem(MapSystem.class);
        MapSystem.MapObjs[][] map = mapSystem.grid;

        Queue<node> Q = new Queue<>();
        Q.addLast(new node(map[(int)gridPosition.y][(int)gridPosition.x], null));

        boolean[][] visited = new boolean[MapSystem.height + 1][MapSystem.width + 1];
        visited[(int)gridPosition.y][(int)gridPosition.x] = true;


        node v = new node();

        while(!Q.isEmpty()){
            v = Q.removeFirst();
            Vector2 position = v.obj.position;
            //
            int up = map[(int)position.y + 1][(int)position.x].type;
            if(!visited[(int)position.y + 1][(int)position.x] && (up == TypeComponent.OTHER ||
            up == TypeComponent.POWER_UP || up == TypeComponent.PLAYER || up == TypeComponent.ENEMY)){
                Q.addLast(new node(map[(int)position.y + 1][(int)position.x], v));
                visited[(int)position.y + 1][(int)position.x] = true;
            }
            //
            int down = map[(int)position.y - 1][(int)position.x].type;
            if(!visited[(int)position.y - 1][(int)position.x] && (down == TypeComponent.OTHER ||
                    down == TypeComponent.POWER_UP || down == TypeComponent.PLAYER || down == TypeComponent.ENEMY)){
                Q.addLast(new node(map[(int)position.y - 1][(int)position.x], v));
                visited[(int)position.y - 1][(int)position.x] = true;
            }
            //
            int left = map[(int)position.y][(int)position.x - 1].type;
            if(!visited[(int)position.y][(int)position.x - 1] && (left == TypeComponent.OTHER ||
                    left == TypeComponent.POWER_UP || left == TypeComponent.PLAYER || left == TypeComponent.ENEMY)){
                Q.addLast(new node(map[(int)position.y][(int)position.x - 1], v));
                visited[(int)position.y][(int)position.x - 1] = true;
            }
            //
            int right = map[(int)position.y][(int)position.x + 1].type;
            if(!visited[(int)position.y][(int)position.x + 1] && (right == TypeComponent.OTHER ||
                    right == TypeComponent.POWER_UP || right == TypeComponent.PLAYER || right == TypeComponent.ENEMY)){
                Q.addLast(new node(map[(int)position.y][(int)position.x + 1], v));
                visited[(int)position.y][(int)position.x + 1] = true;
            }

        }
        while(!(v.prev == null)){
            enemy.move.addFirst(v.obj);
            v = v.prev;
        }
        System.out.println(">>>>>>>>>>>>>>>");
        for(MapSystem.MapObjs objs : enemy.move){
            System.out.println(objs.position);
        }
        System.out.println("<<<<<<<<<<<<<<<");
        if(!enemy.move.isEmpty())
            enemy.moving = true;
    }

    private void move(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        if(enemy.move.isEmpty()){
            enemy.moving = false;
            state.resetPresses();
            enemy.resetDirections();
            return;
        }
        TransformComponent transform = Mappers.transformMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        Vector2 position = new Vector2(transform.position.x, transform.position.y);
        Vector2 newGridPosition = enemy.move.first().position;
        Vector2 newPosition = new Vector2(newGridPosition.x * 2 + 1, newGridPosition.y * 2 + 0.9f);

        if(!enemy.processingMove){
            if(newGridPosition.y > gridPosition.y){
                enemy.up = true;
            }
            else if(newGridPosition.y < gridPosition.y){
                enemy.down = true;
            }
            else if(newGridPosition.x > gridPosition.x){
                enemy.right = true;
            }
            else if(newGridPosition.x < gridPosition.x){
                enemy.left = true;
            }
            else{
                enemy.move.clear();
                enemy.resetDirections();
                state.resetPresses();
                enemy.moving = false;
                return;
            }
            enemy.processingMove = true;
        }


        if(enemy.up){
            if(position.y >= newPosition.y){
                enemy.resetDirections();
                enemy.lastMove = enemy.move.removeFirst();
                state.resetPresses();
                move(entity);
            }
            else{
                state.upPressed = true;
            }
        }

        if(enemy.down){
            if(position.y <= newPosition.y){
                enemy.resetDirections();
                enemy.lastMove = enemy.move.removeFirst();
                state.resetPresses();
                move(entity);
            }
            else {
                state.downPressed = true;
            }
        }

        if(enemy.left){
            if(position.x <= newPosition.x){
                enemy.resetDirections();
                enemy.lastMove = enemy.move.removeFirst();
                state.resetPresses();
                move(entity);
            }
            else{
                state.leftPressed = true;
            }
        }

        if(enemy.right){
            if(position.x >= newPosition.x){
                enemy.resetDirections();
                enemy.lastMove = enemy.move.removeFirst();
                state.resetPresses();
                move(entity);
            }
            else{
                state.rightPressed = true;
            }
        }
    }

    private void correctX(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        Vector2 position = new Vector2(transform.position.x, transform.position.y);
        Vector2 perfectPosition = MapSystem.toGridPosition(position);
        perfectPosition.x = perfectPosition.x * 2 + 1.0f;
        perfectPosition.y =  perfectPosition.y * 2 + 0.95f;

        if(!enemy.processingMove){
            if(perfectPosition.x > position.x){
                enemy.right = true;
            }
            else{
                enemy.left = true;
            }
            enemy.processingMove = true;
        }

        if(enemy.right){
            if(perfectPosition.x <= position.x){
                enemy.resetDirections();
                state.resetPresses();
                enemy.correctingX = false;
                enemy.correctingY = true;
                correctY(entity);
            }
            else{
                state.rightPressed = true;
            }
        }
        else{
            if(perfectPosition.x >= position.x){
                enemy.resetDirections();
                state.resetPresses();
                enemy.correctingX = false;
                enemy.correctingY = true;
                correctY(entity);
            }
            else{
                state.leftPressed = true;
            }
        }
    }

    private void correctY(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        Vector2 position = new Vector2(transform.position.x, transform.position.y);
        Vector2 perfectPosition = MapSystem.toGridPosition(position);
        perfectPosition.x = perfectPosition.x * 2 + 1.0f;
        perfectPosition.y =  perfectPosition.y * 2 + 0.95f;

        if(!enemy.processingMove){
            if(perfectPosition.y > position.y){
                enemy.up = true;
            }
            else{
                enemy.down = true;
            }
            enemy.processingMove = true;
        }

        if(enemy.up){
            if(perfectPosition.y <= position.y){
                enemy.resetDirections();
                state.resetPresses();
                enemy.correctingY = false;
            }
            else{
                state.upPressed = true;
            }
        }
        else{
            if(perfectPosition.y >= position.y){
                enemy.resetDirections();
                state.resetPresses();
                enemy.correctingY = false;
            }
            else{
                state.downPressed = true;
            }
        }
    }


    public void notifyEnemies(){
        for(Entity entity : engine.getEntitiesFor(Family.one(EnemyComponent.class).get())){
            notifyEnemy(entity);
        }
    }

    public void notifyEnemy(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        enemy.move.clear();
        state.resetPresses();
        enemy.resetDirections();
        enemy.moving = false;
        enemy.correctingX = true;
    }

    public Entity createEnemy(float posX, float posY){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);
        StatsComponent playerStats = engine.createComponent(StatsComponent.class);
        EnemyComponent enemy = engine.createComponent(EnemyComponent.class);


        body.body = bodyFactory.makeEnemy(posX, posY);
        position.position.set(posX,posY,0);
        type.type = TypeComponent.ENEMY;
        body.body.setUserData(entity);
        stateCom.set(StateComponent.STATE_MOVING_DOWN);
        stateCom.isLooping = true;

        animCom.animations.put(1,
                new Animation<>(0.05f, atlas.findRegions("player/back/Bman_b")));
        animCom.animations.put(2,
                new Animation<>(0.05f, atlas.findRegions("player/side/Bman_s")));
        animCom.animations.put(3,
                new Animation<>(0.05f, atlas.findRegions("player/front/Bman_f")));
        animCom.animations.put(4,
                new Animation<>(0.05f, atlas.findRegions("player/side/Bman_s")));

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);
        entity.add(playerStats);
        entity.add(enemy);


        engine.addEntity(entity);

        BomberMan.ENEMY_COUNT++;

        return entity;
    }

    public void createEnemy(float posX, float posY, Color color){
        Entity ent = createEnemy(posX, posY);
        TextureComponent texture = Mappers.textureMapper.get(ent);
        texture.color.set(color);
    }
}
