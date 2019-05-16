package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;

import java.util.Random;

public class MapSystem extends IteratingSystem {


    private static TiledMap map;
    private BodyFactory bodyFactory;
    private PooledEngine engine;
    Random generator = new Random();
    public static int width;
    public static int height;
    public MapObjs[][] grid;
    private float time = 2.0f;

    public MapSystem(BodyFactory bf, PooledEngine eng) {
        super(Family.all(BlockComponent.class).get());


        bodyFactory = bf;
        engine = eng;
        map = RenderingSystem.getMap();
        width = 12;
        height = 9;
        grid = new MapObjs[height + 1][width + 1];
        for(int i = 0; i <= height; i++)
            for(int j = 0; j <= width; j++)
                grid[i][j] = new MapObjs(new Vector2(j, i));
        createMap();
        createBlocks();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        time -= deltaTime;
        if(time < 0){
            time = 2.0f;
          //  printMap();
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BlockComponent block = Mappers.blockMapper.get(entity);
        if(block.toDestroy){
            Vector3 pos = Mappers.transformMapper.get(entity).position;
            BodyComponent body = Mappers.bodyMapper.get(entity);
            body.body.getWorld().destroyBody(body.body);
            getEngine().removeEntity(entity);


            float drop = generator.nextFloat();
            if(drop <= 0.4f) {
                int type = generator.nextInt(4);
                getEngine().getSystem(PowerUpSystem.class).createPowerUp(pos.x, pos.y, type);
            }
        }

    }

    private void printMap(){
        for(int i = 0; i <= width + 5; i++)
            System.out.print("-");
        System.out.println();
        for(int i = height; i >= 0; i--){
            for(int j = 0; j <= width; j++){
                System.out.print(grid[i][j].type + " ");
            }
            System.out.println();
        }
        for(int i = 0; i <= width + 5; i++)
            System.out.print("-");
        System.out.println();
    }



    private void createMap() {
        MapObjects objs = map.getLayers().get("wall").getObjects();
        Texture text = new Texture("map/SolidBlock.png");
        TextureRegion texreg = new TextureRegion();
        texreg.setTexture(text);
        placeBlocks(objs, texreg, BlockComponent.WALL);
    }

    private void createBlocks(){
        MapObjects objs = map.getLayers().get("destroyable").getObjects();
        Texture text = new Texture("map/ExplodableBlock.png");
        TextureRegion texreg = new TextureRegion();
        texreg.setTexture(text);
        placeBlocks(objs, texreg, BlockComponent.DESTROYABLE);
    }

    private void placeBlocks(MapObjects objs, TextureRegion texreg, int blockType){
        for (MapObject o : objs) {
            TiledMapTileMapObject tile = (TiledMapTileMapObject) o;

            Entity ent = engine.createEntity();
            BodyComponent body = engine.createComponent(BodyComponent.class);
            TypeComponent type = engine.createComponent(TypeComponent.class);
            TextureComponent texture = engine.createComponent(TextureComponent.class);
            TransformComponent tranComp = engine.createComponent(TransformComponent.class);
            BlockComponent mapComp = engine.createComponent(BlockComponent.class);
            StateComponent stateComp = engine.createComponent(StateComponent.class);

            mapComp.type = blockType;

            if(blockType == BlockComponent.DESTROYABLE){
                body.body = bodyFactory.makeDestroyableBlock(tile.getX() / 32 + 1f, tile.getY() / 32 + 1f);
            }
            else{
                body.body = bodyFactory.makeWall(tile.getX() / 32f + 1f , tile.getY() / 32f + 1f);
            }
            body.body.setUserData(ent);

            texture.region = texreg;
            texture.region.setRegionX(64);
            texture.region.setRegionY(64);

            type.type = blockType == BlockComponent.WALL ? TypeComponent.INDESTRUCTIBLE_BLOCK : TypeComponent.DESTRUCTIBLE_BLOCK;

            tranComp.position.set(tile.getX() / 32 + 1f, tile.getY() / 32 + 1f, -2);

            Vector2 gridPosition = toGridPosition(tranComp.position);

            grid[(int)gridPosition.y][(int)gridPosition.x].type = type.type;


            ent.add(body);
            ent.add(type);
            ent.add(texture);
            ent.add(tranComp);
            ent.add(mapComp);
            ent.add(stateComp);

            engine.addEntity(ent);
        }
    }

    public static Vector2 toGridPosition(float posX, float posY){
        Vector2 position = new Vector2(MathUtils.floor(posX), MathUtils.floor(posY));
        if(position.x % 2 == 0)
            position.x++;
        if(position.y % 2 == 0)
            position.y++;
        position.x = (position.x - 3) / 2 + 1.0f;
        position.y = (position.y - 3) / 2 + 1.0f;
        return position;
    }

    public static Vector2 toGridPosition(Vector3 pos){
        return toGridPosition(pos.x, pos.y);
    }

    public static Vector2 toGridPosition(Vector2 pos){
        return toGridPosition(pos.x, pos.y);
    }


    public boolean checkBlock(float posX, float posY, int type){
        for(Entity entity : engine.getEntitiesFor(Family.one(BlockComponent.class).get())){
            BlockComponent block = Mappers.blockMapper.get(entity);
            if(block.type != type)
                continue;
            for(Fixture fix : Mappers.bodyMapper.get(entity).body.getFixtureList()){
                if(fix.testPoint(posX, posY)){
                    return false;
                }
            }
        }
        return true;
    }

    public static class MapObjs{
        int type;
        float time;
        Vector2 position;

        public MapObjs(){
            type = TypeComponent.OTHER;
            time = 0;
            position = new Vector2();
        }
        public MapObjs(Vector2 position){
            this();
            this.position = position;
        }

        public Vector2 reverse(){
            return new Vector2(position.y, position.x);
        }
    }

}