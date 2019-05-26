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

    private static BomberMan parent;
    public static World world;
    private OrthographicCamera cam;
    private SpriteBatch sb;
    private static PooledEngine engine;
    BodyFactory bodyFactory;
    TextureAtlas atlas;
    FitViewport viewport;
    private boolean pause = false;
    RenderingSystem renderingSystem;

    public GameScreen(BomberMan parent){
        super();
        this.parent = parent;
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new GameContactListener());
        sb = new SpriteBatch();
        bodyFactory = BodyFactory.getInstance(world);
        atlas = parent.assMan.manager.get("game/game.atlas");

        renderingSystem = new RenderingSystem(sb);
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
                parent.soundManager));
        engine.addSystem(new FlameSystem(atlas, bodyFactory));
        engine.addSystem(new MapSystem(bodyFactory, engine));
        engine.addSystem(new PowerUpSystem(atlas, bodyFactory));
        engine.addSystem(new DeathSystem());
        engine.addSystem(new PlayerControlSystem());
        engine.addSystem(new EnemySystem(atlas, bodyFactory, engine));
        engine.addSystem(new GUISystem(parent.assMan.manager.get("flat/flat-earth-ui.json"), atlas, parent.assMan.manager.get("loading/loading.atlas")));
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        engine.update(delta);
        if((Gdx.input.isKeyJustPressed(Input.Keys.P) && !world.isLocked())){
            manageSystems(pause);
            pause = !pause;
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !world.isLocked()){
            parent.changeScreen(BomberMan.LEVELS);
            parent.gameScreen = null;
        }
    }

    private void manageSystems(boolean processing){
        engine.getSystem(PlayerSystem.class).setProcessing(processing);
        engine.getSystem(EnemySystem.class).setProcessing(processing);
        engine.getSystem(FlameSystem.class).setProcessing(processing);
        engine.getSystem(BombSystem.class).setProcessing(processing);
        engine.getSystem(PlayerControlSystem.class).setProcessing(processing);
        engine.getSystem(AnimationSystem.class).setProcessing(processing);
        engine.getSystem(PhysicsSystem.class).setProcessing(processing);
        renderingSystem.pause = !processing;
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
        world.dispose();
        sb.dispose();
        atlas.dispose();
    }


    // Temporary solution, changed engine and parent to static for this
    public static void endGame(){
        parent.changeScreen(BomberMan.ENDGAME);
        parent.gameScreen = null;
    }
}
