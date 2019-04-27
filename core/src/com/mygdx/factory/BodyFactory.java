package com.mygdx.factory;

import com.badlogic.gdx.math.Vector2;
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

    public Body makePlayer(float posx, float posy, float radius, BodyDef.BodyType bodyType, boolean fixedRotation){
        BodyDef bd = new BodyDef();
        bd.type = bodyType;
        bd.position.set(posx, posy);
        bd.fixedRotation = fixedRotation;

        Body bdBody = world.createBody(bd);

        CircleShape circle1 = new CircleShape();
        circle1.setRadius(radius);
        Vector2 offset1 = new Vector2(0,  - 0.10f *radius);
        circle1.setPosition(offset1);

        CircleShape circle2 = new CircleShape();
        circle2.setRadius(radius);
        Vector2 offset2 = new Vector2(0, - 0.2f * radius);
        circle2.setPosition(offset2);

        CircleShape circle3 = new CircleShape();
        circle3.setRadius(radius);

        bdBody.createFixture(circle1, 1.0f);
        bdBody.createFixture(circle2, 1.0f);
        bdBody.createFixture(circle3, 1.0f);
        return bdBody;
    }

}
