package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsDebugSystem extends IteratingSystem {
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera cam;
    private boolean enabled = false;

    public static Vector2 start = new Vector2();
    public static Vector2 end = new Vector2();
    public static Vector2 start2 = new Vector2();
    public static Vector2 end2 = new Vector2();
    public static Vector2 start3 = new Vector2();
    public static Vector2 end3 = new Vector2();
    public static Vector2 start4 = new Vector2();
    public static Vector2 end4 = new Vector2();
    public static Vector2 start5 = new Vector2();
    public static Vector2 end5 = new Vector2();


    private ShapeRenderer shapeRenderer = new ShapeRenderer();


    public PhysicsDebugSystem(World world, OrthographicCamera cam){
        super(Family.all().get());
        debugRenderer = new Box2DDebugRenderer();
        this.world = world;
        this.cam = cam;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        if(Gdx.input.isKeyJustPressed(Input.Keys.K))
            enabled = !enabled;
        if(enabled) {
            debugRenderer.render(world, cam.combined);
            shapeRenderer.setProjectionMatrix(cam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.line(start, end);
            shapeRenderer.line(start2, end2);
            shapeRenderer.line(start3, end3);
            shapeRenderer.line(start4, end4);
            shapeRenderer.line(start5, end5);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.end();
        }
    }
}
