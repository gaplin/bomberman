package com.mygdx.factory;

import com.badlogic.gdx.physics.box2d.*;

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
        fixtureDef.density = 0.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        return fixtureDef;
    }

    public Body makeCirclePolyBody(float posx, float posy, float radius, BodyDef.BodyType bodyType, boolean fixedRotation){
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = bodyType;
        boxBodyDef.position.x = posx;
        boxBodyDef.position.y = posy;
        boxBodyDef.fixedRotation = fixedRotation;

        Body boxBody = world.createBody(boxBodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius /2);
        boxBody.createFixture(makeFixture(circleShape));
        circleShape.dispose();
        return boxBody;
    }


    public Body makeBoxPolyBody(float posx, float posy, float width, float height, BodyDef.BodyType bodyType, boolean fixedRotation){
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = bodyType;
        boxBodyDef.position.x = posx;
        boxBodyDef.position.y = posy;
        boxBodyDef.fixedRotation = fixedRotation;

        Body boxBody = world.createBody(boxBodyDef);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(width / 2,height / 2);
        boxBody.createFixture(makeFixture(poly));
        poly.dispose();

        return boxBody;
    }

}
