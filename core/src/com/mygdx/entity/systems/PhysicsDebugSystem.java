package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsDebugSystem extends IteratingSystem {
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera cam;
    private boolean enabled = false;

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
        if(enabled)
            debugRenderer.render(world, cam.combined);
    }
}
