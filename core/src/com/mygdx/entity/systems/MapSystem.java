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
import com.badlogic.gdx.math.Vector3;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;

import java.util.Random;

public class MapSystem extends IteratingSystem {


    private static TiledMap map;
    private BodyFactory bodyFactory;
    private PooledEngine engine;
    Random generator = new Random();

    public MapSystem(BodyFactory bf, PooledEngine eng) {
        super(Family.all(BlockComponent.class).get());


        bodyFactory = bf;
        engine = eng;
        map = RenderingSystem.getMap();
        createMap();
        createBlocks();
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
                body.body = bodyFactory.makeDestroyableBlock(tile.getX() / 32 + 0.45f, tile.getY() / 32 + 0.2f);
            }
            else{
                body.body = bodyFactory.makeWall(tile.getX() / 32 + 0.45f, tile.getY() / 32 + 0.2f);
            }
            body.body.setUserData(ent);

            texture.region = texreg;
            texture.region.setRegionX(35);
            texture.region.setRegionY(35);

            type.type = TypeComponent.SCENERY;

            tranComp.position.set(tile.getX() / 32 + 0.45f, tile.getY() / 32 + 0.2f, -1);


            ent.add(body);
            ent.add(type);
            ent.add(texture);
            ent.add(tranComp);
            ent.add(mapComp);
            ent.add(stateComp);

            engine.addEntity(ent);
        }
    }


    public static boolean checkBlock(float posx,float posy, String name){
        MapObjects objects = map.getLayers().get(name).getObjects();
        float width = BomberMan.TILE_WIDTH;
        float height = BomberMan.TILE_HEIGHT;

        for(MapObject obj : objects){
            TiledMapTileMapObject tile = (TiledMapTileMapObject) obj;
            float x = tile.getX() / 32 + 0.45f;
            float y = tile.getY() / 32 + 0.2f;
            if(intersects(x+width/2, y+height/2, width, height, posx, posy, BomberMan.BOMB_RADIUS / 3))
                return false;
        }
        return true;
    }

    private static boolean intersects(float rectX, float rectY, float rectWidth, float rectHeight, float circleX, float circleY, float circleR){
        float circleDistX = circleX - rectX;
        circleDistX = circleDistX < 0 ? -1 * circleDistX : circleDistX;
        float circleDistY = circleY - rectY;
        circleDistY = circleDistY < 0 ? -1 * circleDistY : circleDistY;

        if(circleDistX > (rectWidth/2 + circleR))
            return false;
        if(circleDistY > (rectHeight/2 + circleR))
            return false;

        if(circleDistX <= (rectWidth/2))
            return true;
        if(circleDistY <= (rectHeight/2))
            return true;

        float cornerDist = (circleDistX - rectWidth/2)*(circleDistX - rectWidth/2) + (circleDistY - rectHeight/2)*(circleDistY - rectHeight/2);
        return cornerDist <= circleR * circleR;
    }
}
