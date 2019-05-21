package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;

import java.util.Comparator;
import java.util.Random;

public class EnemySystem extends IteratingSystem {

    private TextureAtlas atlas;
    private BodyFactory bodyFactory;
    private PooledEngine engine;
    private BombNodeComparator comparator = new BombNodeComparator();
    private Random random = new Random();

    public EnemySystem(TextureAtlas atlas , BodyFactory bodyFactory, PooledEngine engine){
        super(Family.all(EnemyComponent.class).get());

        this.atlas = atlas;
        this.bodyFactory = bodyFactory;
        this.engine = engine;
        BomberMan.ENEMY_COUNT = 0;

        createEnemy(23.0f,2.0f, Color.RED);
        createEnemy(2f, 2.0f, Color.YELLOW);
        createEnemy(23.0f, 16.0f, Color.BLUE);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if(!scanMove(entity))
            return;
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        int posX = (int)gridPosition.x;
        int posY = (int)gridPosition.y;
        MapSystem.MapObjs[][] map = getEngine().getSystem(MapSystem.class).grid;

        if(enemy.correctingX){
            correctX(entity);
            return;
        }

        if(enemy.correctingY){
            correctY(entity);
            return;
        }

        if(!enemy.moving){
            if(!bombPlant(entity)){
                calculateMove(entity, posX, posY, map, false);
            }
        }
        else{
            move(entity);
        }

    }

    private static class Node{
        MapSystem.MapObjs obj;
        Node prev;
        int distance = 0;

        Node(){}
        Node(MapSystem.MapObjs obj, Node prev){
            this.obj = obj;
            this.prev = prev;
            if(prev != null){
                this.distance = prev.distance + 1;
            }
        }
    }

    private static class DetonationInfo{
        int destroyedBlocks = 0;
        int flames = 1;
        int distanceToPlayer = 99;
        DetonationInfo(){}
        DetonationInfo(int destroyedBlocks){
            this.destroyedBlocks = destroyedBlocks;
        }
        DetonationInfo(int destroyedBlocks, int flames){
            this(destroyedBlocks);
            this.flames = flames;
        }
        DetonationInfo(int destroyedBlocks, int flames, int distanceToPlayer){
            this(destroyedBlocks, flames);
            this.distanceToPlayer = distanceToPlayer;
        }
        DetonationInfo merge(DetonationInfo second){
            this.destroyedBlocks += second.destroyedBlocks;
            this.flames += second.flames;
            this.distanceToPlayer = Math.min(this.distanceToPlayer, second.distanceToPlayer);
            return this;
        }
    }

    private static class BombNode extends Node{
        boolean safe = true;
        DetonationInfo info = new DetonationInfo();
        BombNode(){
            super();
        }
        BombNode(MapSystem.MapObjs obj, Node prev){
            super(obj, prev);
        }
        BombNode(MapSystem.MapObjs obj, Node prev, boolean safe){
            super(obj, prev);
            this.safe = safe;
        }
        BombNode(MapSystem.MapObjs obj, Node prev, boolean safe, DetonationInfo info){
            this(obj, prev, safe);
            this.info = info;
        }
    }

    private static class BombNodeComparator implements Comparator<BombNode> {
        BombNodeComparator(){}

        @Override
        public int compare(BombNode o1, BombNode o2) {
            DetonationInfo a = o1.info;
            DetonationInfo b = o2.info;
            if(o1.safe && !o2.safe)
                return -1;
            if(!o1.safe && o2.safe)
                return 1;
            if(o1.safe){
                if(a.destroyedBlocks < b.destroyedBlocks)
                    return 1;
                if(a.destroyedBlocks == b.destroyedBlocks){
                    if(a.flames < b.flames)
                        return 1;
                    if(a.flames == b.flames){
                        return Integer.compare(o1.distance, o2.distance);
                    }
                    return -1;
                }
                return -1;
            }
            return 0;
        }
    }



    private boolean calculateMove(Entity entity, int posX, int posY, MapSystem.MapObjs[][] map, boolean fakeMove){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);

        Queue<Node> Q = new Queue<>();

        boolean[][] visited = new boolean[MapSystem.height + 1][MapSystem.width + 1];
        Q.addLast(new Node(map[posY][posX], null));
        visited[posY][posX] = true;


        Node v = new Node();

        boolean escape = false;
        int tmp = map[posY][posX].type;
        if(!fakeMove && (tmp == TypeComponent.BOMB || tmp == TypeComponent.FLAME)) {
            escape = true;
        }


        while(!Q.isEmpty()){
            v = Q.removeFirst();
            Vector2 position = v.obj.position;
            //
            posY = (int)position.y;
            posX = (int)position.x;
            tmp = map[posY][posX].type;
            if(escape && tmp != TypeComponent.FLAME && tmp != TypeComponent.BOMB) {
                escape = false;
                break;
            }
            if(fakeMove && tmp != TypeComponent.FAKE_FLAME && tmp != TypeComponent.BOMB && tmp != TypeComponent.FLAME){
                return true;
            }

            posY++;
            int up = map[posY][posX].type;
            if(!visited[posY][posX] && up != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    up != TypeComponent.INDESTRUCTIBLE_BLOCK && up != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(escape) {
                    Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
                else{
                    if(up != TypeComponent.FLAME){
                        Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                    }
                }
            }
            //
            posY = (int)position.y - 1;
            int down = map[posY][posX].type;
            if(!visited[posY][posX] && down != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    down != TypeComponent.INDESTRUCTIBLE_BLOCK && down != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(escape) {
                    Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
                else{
                    if(down != TypeComponent.FLAME){
                        Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                    }
                }
            }
            //
            posY = (int)position.y;
            posX = (int)position.x - 1;
            int left = map[posY][posX].type;
            if(!visited[posY][posX] && left != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    left != TypeComponent.INDESTRUCTIBLE_BLOCK && left != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(escape) {
                    Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
                else{
                    if(left != TypeComponent.FLAME){
                        Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                    }
                }
            }
            //
            posX = (int)position.x + 1;
            int right = map[posY][posX].type;
            if(!visited[posY][posX] && right != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    right != TypeComponent.INDESTRUCTIBLE_BLOCK && right != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(escape) {
                    Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
                else{
                    if(right != TypeComponent.FLAME){
                        Q.addLast(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                    }
                }
            }

        }
        if(fakeMove) {
            return false;
        }
        if(escape){
            return false;
        }
        while(!(v.prev == null)){
            enemy.move.addFirst(v.obj);
            v = v.prev;
        }

        if(!enemy.move.isEmpty())
            enemy.moving = true;
        return true;
    }

    private boolean bombPlant(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        if(stats.bombs == 0){
            return false;
        }
        int range = stats.bombPower;
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        MapSystem mapSystem = getEngine().getSystem(MapSystem.class);
        MapSystem.MapObjs[][] map = mapSystem.grid;

        Queue<BombNode> Q = new Queue<>();
        Array<BombNode> possibleNotDestructibleMoves = new Array<>();
        Array<BombNode> possibleDestructibleMoves = new Array<>();
        Q.addLast(new BombNode(map[(int)gridPosition.y][(int)gridPosition.x], null));

        boolean[][] visited = new boolean[MapSystem.height + 1][MapSystem.width + 1];
        int posX = (int)gridPosition.x;
        int posY = (int)gridPosition.y;
        visited[posY][posX] = true;

        BombNode v;

        while(!Q.isEmpty()){
            v = Q.removeFirst();
            Vector2 position = v.obj.position;
            //
            posY = (int)position.y;
            posX = (int)position.x;

            DetonationInfo info = safePlant(entity, posX, posY, range);
            BombNode next = new BombNode(new MapSystem.MapObjs(map[posY][posX]), v.prev, true, info);
            if(info != null && map[posY][posX].type != TypeComponent.BOMB && map[posY][posX].type != TypeComponent.FLAME){
                if(info.destroyedBlocks > 0) {
                    possibleDestructibleMoves.add(next);
                }
                else{
                    possibleNotDestructibleMoves.add(next);
                }
            }

            posY += 1;
            int up = map[posY][posX].type;
            if(!visited[posY][posX] && up != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    up != TypeComponent.INDESTRUCTIBLE_BLOCK && up != TypeComponent.BOMB &&
                    up != TypeComponent.FLAME){
                visited[posY][posX] = true;
                next = new BombNode(map[posY][posX], v, true);
                Q.addLast(next);
            }
            //
            posY = (int)position.y - 1;
            int down = map[posY][posX].type;
            if(!visited[posY][posX] && down != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    down != TypeComponent.INDESTRUCTIBLE_BLOCK && down != TypeComponent.BOMB &&
                    down != TypeComponent.FLAME){
                visited[posY][posX] = true;
                next = new BombNode(map[posY][posX], v, true);
                Q.addLast(next);
            }
            //
            posY = (int)position.y;
            posX = (int)position.x - 1;
            int left = map[posY][posX].type;
            if(!visited[posY][posX] && left != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    left != TypeComponent.INDESTRUCTIBLE_BLOCK && left != TypeComponent.BOMB &&
                    left != TypeComponent.FLAME){
                visited[posY][posX] = true;
                next = new BombNode(map[posY][posX], v, true);
                Q.addLast(next);
            }
            //
            posX = (int)position.x + 1;
            int right = map[posY][posX].type;
            if(!visited[posY][posX] && right != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    right != TypeComponent.INDESTRUCTIBLE_BLOCK && right != TypeComponent.BOMB &&
                    right != TypeComponent.FLAME){
                visited[posY][posX] = true;
                next = new BombNode(map[posY][posX], v, true);
                Q.addLast(next);
            }
        }

        possibleDestructibleMoves.sort(comparator);
        possibleNotDestructibleMoves.sort(comparator);
        Node p = null;
        if(!possibleDestructibleMoves.isEmpty()){
            p = possibleDestructibleMoves.get(Math.min(random.nextInt(possibleDestructibleMoves.size), 3));
        }
        else if(!possibleNotDestructibleMoves.isEmpty()){
            p = possibleNotDestructibleMoves.get(Math.min(random.nextInt(possibleNotDestructibleMoves.size), 3));
        }
        if(p != null){
            while(p.prev != null){
                enemy.move.addFirst(p.obj);
                p = p.prev;
            }
            enemy.moving = true;
            enemy.isProcessingBomb = true;
            return true;
        }
        return false;
    }

    private DetonationInfo safePlant(Entity entity, int posX, int posY, int range){
        MapSystem.MapObjs[][] mapCopy = new MapSystem.MapObjs[MapSystem.height + 1][MapSystem.width + 1];
        MapSystem.MapObjs[][] grid = getEngine().getSystem(MapSystem.class).grid;
        for(int i = 0; i <= MapSystem.height; i++){
            for(int j = 0; j <= MapSystem.width; j++){
                mapCopy[i][j] = new MapSystem.MapObjs(grid[i][j]);
            }
        }
        DetonationInfo result = putBomb(posX, posY, range, mapCopy);
        if(calculateMove(entity, posX, posY, mapCopy, true))
            return result;
        return null;
    }

    private DetonationInfo putBomb(int posX, int posY, int range, MapSystem.MapObjs[][] mapCopy){
        return setX(posX, posY, range, 1, mapCopy).merge(
                setX(posX, posY, range, -1, mapCopy)).merge(
                setY(posX, posY, range, 1, mapCopy)).merge(
                setY(posX, posY, range, -1, mapCopy));
    }

    private DetonationInfo setX(int posX, int posY, int range, int mod, MapSystem.MapObjs[][] mapCopy){
        DetonationInfo result = new DetonationInfo();
        mapCopy[posY][posX].type = TypeComponent.BOMB;
        for(int i = 1; i <= range; i++){
            int type = mapCopy[posY][posX + i * mod].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK){
                break;
            }

            if(mapCopy[posY][posX + i * mod].type != TypeComponent.BOMB &&
                    mapCopy[posY][posX + i * mod].type != TypeComponent.FLAME) {
                mapCopy[posY][posX + i * mod].type = TypeComponent.FAKE_FLAME;
            }
            result.flames++;
            if(type == TypeComponent.DESTRUCTIBLE_BLOCK){
                result.destroyedBlocks++;
                break;
            }
        }
        return result;
    }

    private DetonationInfo setY(int posX, int posY, int range, int mod, MapSystem.MapObjs[][] mapCopy){
        DetonationInfo result = new DetonationInfo();
        mapCopy[posY][posX].type = TypeComponent.BOMB;
        for(int i = 1; i <= range; i++){
            int type = mapCopy[posY + i * mod][posX].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK){
                break;
            }

            if(mapCopy[posY + i * mod][posX].type != TypeComponent.BOMB &&
                    mapCopy[posY + i * mod][posX].type != TypeComponent.FLAME) {
                mapCopy[posY + i * mod][posX].type = TypeComponent.FAKE_FLAME;
            }
            result.flames++;
            if(type == TypeComponent.DESTRUCTIBLE_BLOCK){
                result.destroyedBlocks++;
                break;
            }
        }
        return result;
    }

    private void move(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        Vector2 position = new Vector2(transform.position.x, transform.position.y);
        if(enemy.move.isEmpty()){
            enemy.moving = false;
            state.resetPresses();
            enemy.resetDirections();
            if(enemy.isProcessingBomb){
                getEngine().getSystem(BombSystem.class).createBomb((gridPosition.x - 1) * 2 + 3, (gridPosition.y - 1) * 2 + 3, entity);
                stats.bombs--;
                enemy.isProcessingBomb = false;
            }
            return;
        }
        Vector2 newGridPosition = enemy.move.first().position;
        Vector2 newPosition = new Vector2(newGridPosition.x * 2 + 1, newGridPosition.y * 2 + 0.85f);

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
                enemy.isProcessingBomb = false;
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


    private boolean scanMove(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        MapSystem.MapObjs[][] grid = getEngine().getSystem(MapSystem.class).grid;
        for(MapSystem.MapObjs obj : enemy.move){
            int type = grid[(int)obj.position.y][(int)obj.position.x].type;
            if(type == TypeComponent.BOMB){
                resetMove(entity);
                return false;
            }
            if(type != obj.type && type == TypeComponent.FLAME){
                resetMove(entity);
                return false;
            }
        }
        return true;
    }


    private void resetMove(Entity entity){
        StateComponent state = Mappers.stateMapper.get(entity);
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        enemy.move.clear();
        state.resetPresses();
        enemy.resetDirections();
        enemy.moving = false;
        enemy.isProcessingBomb = false;
        enemy.correctingX = true;
    }

    private Entity createEnemy(float posX, float posY){
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

    private void createEnemy(float posX, float posY, Color color){
        Entity ent = createEnemy(posX, posY);
        TextureComponent texture = Mappers.textureMapper.get(ent);
        texture.color.set(color);
    }
}
