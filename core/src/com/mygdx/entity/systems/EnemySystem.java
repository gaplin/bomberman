package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.msg.PriorityQueue;
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

        if(BomberMan.BOTS > 0)
            createEnemy(23.0f,2.0f, Color.RED);
        if(BomberMan.BOTS > 1)
            createEnemy(2f, 2.0f, Color.YELLOW);
        if(BomberMan.BOTS > 2)
            createEnemy(23.0f, 16.0f, Color.BLUE);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
        int posX = (int)gridPosition.x;
        int posY = (int)gridPosition.y;
        MapSystem.MapObjs[][] map = copyMap(getEngine().getSystem(MapSystem.class).grid);
        updateMap(map);
        if(!scanMove(entity, map))
            return;


        if(enemy.correctingX){
            correctX(entity);
            return;
        }

        if(enemy.correctingY){
            correctY(entity);
            return;
        }

        if(!enemy.moving){
            if(!bombPlant(entity, copyMap(map))) {
                if(map[posY][posX].type == TypeComponent.BOMB || map[posY][posX].type == TypeComponent.FLAME) {
                    calculateMove(entity, posX, posY, map, false, 0.0f);
                }
            }
        }
        else{
            move(entity);
        }
    }

    private MapSystem.MapObjs[][] copyMap(MapSystem.MapObjs[][] map){
        MapSystem.MapObjs[][] result = new MapSystem.MapObjs[MapSystem.height + 1][MapSystem.width + 1];
        for(int i = 0; i <= MapSystem.height; i++){
            for(int j = 0; j <= MapSystem.width; j++){
                result[i][j] = new MapSystem.MapObjs(map[i][j]);
            }
        }
        return result;
    }

    private void updateMap(MapSystem.MapObjs[][] map){
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.one(FlameComponent.class, BombComponent.class).get());
        for(Entity entity : entities){
            int type = Mappers.typeMapper.get(entity).type;
            Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);
            int posX = (int)gridPosition.x;
            int posY = (int)gridPosition.y;
            if(type == TypeComponent.BOMB){
                BombComponent bomb = Mappers.bombMapper.get(entity);
                int range = Mappers.bombMapper.get(entity).range;
                putBomb(posX, posY, range, map, TypeComponent.FLAME, Math.max(bomb.detonationTime, 0.0f));
            }
            else{
                FlameComponent flame = Mappers.flameMapper.get(entity);
                if(map[posY][posX].type != TypeComponent.FLAME) {
                    map[posY][posX].time = flame.duration;
                }
                else{
                    map[posY][posX].time = Math.max(map[posY][posX].time, flame.duration);
                }
                map[posY][posX].type = TypeComponent.FLAME;
            }
        }
    }

    private void printMap(MapSystem.MapObjs[][] map, char symbol){
        for(int i = 0; i < 15; i++)
            System.out.print(symbol);
        System.out.println();
        for(int i = MapSystem.height - 1; i > 0; i--){
            for(int j = 1; j < MapSystem.width; j++){
                System.out.printf("(%d,%.2f) ", map[i][j].type, map[i][j].time);
            }
            System.out.println();
        }
        for(int i = 0; i < 15; i++)
            System.out.print(symbol);
        System.out.println();
    }

    private static class Node implements Comparable<Node>{
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

        @Override
        public int compareTo(Node o) {
            if(o.distance == this.distance) {
                return Float.compare(o.obj.time, this.obj.time);
            }
            if(this.distance > o.distance){
                return 1;
            }
            return -1;
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



    private boolean calculateMove(Entity entity, int posX, int posY, MapSystem.MapObjs[][] map, boolean fakeMove, float delay){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        float timePerUnit = 1.0f / (stats.movementSpeed / 4.0f);

        PriorityQueue<Node> Q = new PriorityQueue<>();
        Array<Node> possibleMoves = new Array<>();

        boolean[][] visited = new boolean[MapSystem.height + 1][MapSystem.width + 1];
        Q.add(new Node(map[posY][posX], null));
        visited[posY][posX] = true;


        Node v;

        boolean escape = false;
        int tmp = map[posY][posX].type;
        if(tmp == TypeComponent.BOMB || tmp == TypeComponent.FLAME) {
            escape = true;
        }


        while(!(Q.size() == 0)){
            v = Q.poll();
            Vector2 position = v.obj.position;
            //
            posY = (int)position.y;
            posX = (int)position.x;
            tmp = map[posY][posX].type;
            if(tmp != TypeComponent.FLAME && tmp != TypeComponent.BOMB) {
                escape = false;
                if(fakeMove){
                    return true;
                }
                possibleMoves.add(v);
            }

            posY++;
            int up = map[posY][posX].type;
            if(!visited[posY][posX] && up != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    up != TypeComponent.INDESTRUCTIBLE_BLOCK && up != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(up != TypeComponent.FLAME)
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                else if(checkRange(map[posY][posX].time, v.distance, timePerUnit, delay)){
                        Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                    }
            }
            //
            posY = (int)position.y - 1;
            int down = map[posY][posX].type;
            if(!visited[posY][posX] && down != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    down != TypeComponent.INDESTRUCTIBLE_BLOCK && down != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(down != TypeComponent.FLAME)
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                else if(checkRange(map[posY][posX].time, v.distance, timePerUnit, delay)){
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
            }
            //
            posY = (int)position.y;
            posX = (int)position.x - 1;
            int left = map[posY][posX].type;
            if(!visited[posY][posX] && left != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    left != TypeComponent.INDESTRUCTIBLE_BLOCK && left != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(left != TypeComponent.FLAME)
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                else if(checkRange(map[posY][posX].time, v.distance, timePerUnit, delay)){
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
            }
            //
            posX = (int)position.x + 1;
            int right = map[posY][posX].type;
            if(!visited[posY][posX] && right != TypeComponent.DESTRUCTIBLE_BLOCK &&
                    right != TypeComponent.INDESTRUCTIBLE_BLOCK && right != TypeComponent.BOMB){
                visited[posY][posX] = true;
                if(right != TypeComponent.FLAME)
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                else if(checkRange(map[posY][posX].time, v.distance, timePerUnit, delay)){
                    Q.add(new Node(new MapSystem.MapObjs(map[posY][posX]), v));
                }
            }
        }
        if(fakeMove || escape) {
            return false;
        }
        int movesRange = Math.min(possibleMoves.size, 5);
        Node p = possibleMoves.get(random.nextInt(movesRange));
        while(p.prev != null){
            enemy.move.addFirst(p.obj);
            p = p.prev;
        }

        if(!enemy.move.isEmpty())
            enemy.moving = true;
        return enemy.moving;
    }

private boolean bombPlant(Entity entity, MapSystem.MapObjs[][] map){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        float timePerUnit = 1.0f / (stats.movementSpeed / 4.0f);
        if(stats.bombs == 0){
            return false;
        }
        int range = stats.bombPower;
        Vector2 gridPosition = MapSystem.toGridPosition(Mappers.transformMapper.get(entity).position);

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

            float delay = v.distance * timePerUnit;
            DetonationInfo info = safePlant(entity, posX, posY, range, copyMap(map), delay);
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
        int movesRange = 10;
        Node p = null;
        if(!possibleDestructibleMoves.isEmpty()){
            p = possibleDestructibleMoves.get(Math.min(random.nextInt(possibleDestructibleMoves.size), movesRange));
        }
        else if(!possibleNotDestructibleMoves.isEmpty()){
            p = possibleNotDestructibleMoves.get(Math.min(random.nextInt(possibleNotDestructibleMoves.size), movesRange));
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

    private boolean checkRange(float flameTime, int distance, float timePerUnit, float delay){
        float time = flameTime - distance * timePerUnit - delay;
        float lim = 0.6f;
        if(time >= -lim && time <= FlameComponent.flameTime + lim)
            return false;
        time = flameTime - (distance + 1) * timePerUnit - delay;
        return !(time >= -lim) || !(time <= FlameComponent.flameTime + lim);
    }

    private DetonationInfo safePlant(Entity entity, int posX, int posY, int range, MapSystem.MapObjs[][] mapCopy, float delay){
        DetonationInfo result = putBomb(posX, posY, range, mapCopy, TypeComponent.FLAME, BombComponent.bombTime + delay);
        if(calculateMove(entity, posX, posY, mapCopy, true, delay))
            return result;
        return null;
    }

    private DetonationInfo putBomb(int posX, int posY, int range, MapSystem.MapObjs[][] mapCopy, int flameType, float time){
        return setX(posX, posY, range, 1, mapCopy, flameType, time).merge(
                setX(posX, posY, range, -1, mapCopy, flameType, time)).merge(
                setY(posX, posY, range, 1, mapCopy, flameType, time)).merge(
                setY(posX, posY, range, -1, mapCopy, flameType, time));
    }

    private DetonationInfo setX(int posX, int posY, int range, int mod, MapSystem.MapObjs[][] mapCopy, int flameType, float time){
        DetonationInfo result = new DetonationInfo();
        if(mapCopy[posY][posX].type != TypeComponent.FLAME) {
            mapCopy[posY][posX].type = TypeComponent.BOMB;
            mapCopy[posY][posX].time = time;
        }
        for(int i = 1; i <= range; i++){
            int type = mapCopy[posY][posX + i * mod].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK){
                break;
            }

            result.flames++;
            if(type == TypeComponent.DESTRUCTIBLE_BLOCK){
                result.destroyedBlocks++;
                break;
            }

            if(type != TypeComponent.BOMB) {
                mapCopy[posY][posX + i * mod].type = flameType;
                if(type != TypeComponent.FLAME){
                    mapCopy[posY][posX + i * mod].time = time + FlameComponent.flameTime;
                }
            }
        }
        return result;
    }

    private DetonationInfo setY(int posX, int posY, int range, int mod, MapSystem.MapObjs[][] mapCopy, int flameType, float time){
        DetonationInfo result = new DetonationInfo();
        if(mapCopy[posY][posX].type != TypeComponent.FLAME) {
            mapCopy[posY][posX].type = TypeComponent.BOMB;
            mapCopy[posY][posX].time = time;
        }
        for(int i = 1; i <= range; i++){
            int type = mapCopy[posY + i * mod][posX].type;
            if(type == TypeComponent.INDESTRUCTIBLE_BLOCK){
                break;
            }

            result.flames++;
            if(type == TypeComponent.DESTRUCTIBLE_BLOCK){
                result.destroyedBlocks++;
                break;
            }

            if(type != TypeComponent.BOMB) {
                mapCopy[posY + i * mod][posX].type = flameType;
                if(type != TypeComponent.FLAME){
                    mapCopy[posY + i * mod][posX].time = time + FlameComponent.flameTime;
                }
            }
        }
        return result;
    }

    private void move(Entity entity){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        StateComponent state = Mappers.stateMapper.get(entity);
        StatsComponent stats = Mappers.statsMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        Vector2 gridPosition = MapSystem.toGridPosition(transform.position);
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
        Vector2 newPosition = new Vector2(newGridPosition.x * 2 + 1.0f, newGridPosition.y * 2 + 0.9f);

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
                return;
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
                return;
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
                return;
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
                return;
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


    private boolean scanMove(Entity entity, MapSystem.MapObjs[][] grid){
        EnemyComponent enemy = Mappers.enemyMapper.get(entity);
        for(MapSystem.MapObjs obj : enemy.move){
            int type = grid[(int)obj.position.y][(int)obj.position.x].type;
            if(type == TypeComponent.BOMB){
                resetMove(entity);
                return false;
            }
            if(type != obj.type){
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
