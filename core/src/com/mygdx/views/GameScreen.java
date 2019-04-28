package com.mygdx.views;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.entity.systems.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;
import com.mygdx.listeners.GameContactListener;



public class GameScreen implements Screen {

    private BomberMan parent;
    public static World world;
    private OrthographicCamera cam;
    private SpriteBatch sb;
    private PooledEngine engine;
    BodyFactory bodyFactory;
    TextureAtlas atlas;
    FitViewport viewport;

    public GameScreen(BomberMan parent){
        super();
        this.parent = parent;
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new GameContactListener());
        sb = new SpriteBatch();
        bodyFactory = BodyFactory.getInstance(world);
        atlas = parent.assMan.manager.get("game/game.atlas");

        RenderingSystem renderingSystem = new RenderingSystem(sb);
        cam = renderingSystem.getCamera();
        viewport = renderingSystem.getViewport();
        sb.setProjectionMatrix(cam.combined);

        engine = new PooledEngine();

        engine.addSystem(renderingSystem);
        engine.addSystem(new PhysicsDebugSystem(world, cam));
        engine.addSystem(new PlayerSystem(bodyFactory, atlas, engine));
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new BombSystem(atlas, bodyFactory,
                parent.assMan.manager.get("sounds/bombSound.mp3")));
        engine.addSystem(new FlameSystem(atlas, bodyFactory));
        engine.addSystem(new CollisionSystem());
        engine.addSystem(new MapSystem(bodyFactory, engine));

    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        engine.update(delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !world.isLocked()){
            parent.changeScreen(BomberMan.LEVELS);
            engine.removeAllEntities();
            parent.gameScreen = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
