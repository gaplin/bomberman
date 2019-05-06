package com.mygdx.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.entity.components.BombComponent;
import com.mygdx.entity.components.PlayerComponent;
import com.mygdx.game.BomberMan;



public class BodyFactory {

    private static BodyFactory thisInstance = null;

    private static World world;

    private BodyFactory(World worldd){
        world = worldd;
    }

    public static BodyFactory getInstance(World worldd){
        world = worldd;
        if(thisInstance == null){
            thisInstance = new BodyFactory(world);
        }
        return thisInstance;
    }

    static public FixtureDef makeFixture(Shape shape){
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        return fixtureDef;
    }

    static public FixtureDef makeFixture(){
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        return fixtureDef;
    }


    public BodyDef makeBodyDef(float posX, float posY, BodyDef.BodyType type){
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = posX;
        boxBodyDef.position.y = posY;
        boxBodyDef.fixedRotation = true;
        return boxBodyDef;
    }

    public Body makeDestroyableBlock(float posX, float posY){
        BodyDef boxBodyDef = makeBodyDef(posX, posY, BodyDef.BodyType.StaticBody);

        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.DESTRUCTIBLE_BIT;
        fd.filter.maskBits = BomberMan.PLAYER_BIT | BomberMan.BOMB_BIT | BomberMan.FLAME_BIT;

        Body boxBody = world.createBody(boxBodyDef);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(BomberMan.TILE_WIDTH / 2f,BomberMan.TILE_HEIGHT / 2f);
        fd.shape = poly;
        boxBody.createFixture(fd);
        poly.dispose();
        return boxBody;
    }

    public Body makeWall(float posX, float posY){
        BodyDef boxBodyDef = makeBodyDef(posX, posY, BodyDef.BodyType.StaticBody);

        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.INDESTRUCTIBLE_BIT;
        fd.filter.maskBits = BomberMan.PLAYER_BIT | BomberMan.BOMB_BIT;

        Body boxBody = world.createBody(boxBodyDef);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(BomberMan.TILE_WIDTH / 2f,BomberMan.TILE_HEIGHT / 2f);
        fd.shape = poly;
        boxBody.createFixture(fd);
        poly.dispose();
        return boxBody;
    }

    public Body makePlayer(float posX, float posY){
        com.mygdx.BodyEditor.BodyEditorLoader loader = new com.mygdx.BodyEditor.BodyEditorLoader(Gdx.files.internal("bodies/bomberman.json"));

        BodyDef bdBody = makeBodyDef(posX, posY, BodyDef.BodyType.DynamicBody);

        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.PLAYER_BIT;
        fd.filter.maskBits = PlayerComponent.defaultMaskBits;

        Body playerModel = world.createBody(bdBody);

        loader.attachFixture(playerModel, "bman", fd, 2.0f * BomberMan.PLAYER_SCALE);

        return playerModel;
    }

    public Body makeBomb(float posX, float posY){
        BodyDef boxBodyDef = makeBodyDef(posX, posY, BodyDef.BodyType.DynamicBody);

        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.BOMB_BIT;
        fd.filter.maskBits = BombComponent.defaultMaskBits;


        Body boxBody = world.createBody(boxBodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(BomberMan.BOMB_RADIUS / 2f);
        fd.shape = circleShape;
        boxBody.createFixture(fd);
        boxBody.getFixtureList().first().setSensor(true);
        circleShape.dispose();

        return boxBody;
    }

    public Body makeFlame(float posX, float posY){
        BodyDef bodyDef = makeBodyDef(posX, posY, BodyDef.BodyType.DynamicBody);
        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.FLAME_BIT;
        fd.filter.maskBits = BomberMan.PLAYER_BIT | BomberMan.DESTRUCTIBLE_BIT | BomberMan.BOMB_BIT;

        Body boxBody = world.createBody(bodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(BomberMan.BOMB_RADIUS / 2f);
        fd.shape = circleShape;
        boxBody.createFixture(fd);
        circleShape.dispose();

        return boxBody;
    }

    public Body makePowerUp(float posX, float posY){
        BodyDef bodyDef = makeBodyDef(posX, posY, BodyDef.BodyType.DynamicBody);
        FixtureDef fd = makeFixture();
        fd.filter.categoryBits = BomberMan.POWER_UP_BIT;
        fd.filter.maskBits = BomberMan.PLAYER_BIT;

        Body boxBody = world.createBody(bodyDef);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(BomberMan.TILE_WIDTH / 2f,BomberMan.TILE_HEIGHT / 2f);
        fd.shape = poly;
        boxBody.createFixture(fd);
        poly.dispose();
        return boxBody;
    }
}
