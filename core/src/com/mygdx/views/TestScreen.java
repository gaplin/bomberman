package com.mygdx.views;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.entity.components.*;
import com.mygdx.entity.systems.*;
import com.mygdx.factory.BodyFactory;
import com.mygdx.game.BomberMan;
import com.mygdx.listeners.GameContactListener;



public class TestScreen implements Screen {

    private BomberMan parent;
    public static World world;
    private OrthographicCamera cam;
    private SpriteBatch sb;
    private PooledEngine engine;
    BodyFactory bodyFactory;
    TextureAtlas atlas;
    FitViewport viewport;

    public TestScreen(BomberMan parent){
        super();
        this.parent = parent;
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new GameContactListener());
        sb = new SpriteBatch();
        bodyFactory = BodyFactory.getInstance(world);
        atlas = parent.assMan.manager.get("core/assets/game/game.atlas");

        RenderingSystem renderingSystem = new RenderingSystem(sb);
        cam = renderingSystem.getCamera();
        viewport = renderingSystem.getViewport();
        sb.setProjectionMatrix(cam.combined);

        engine = new PooledEngine();

        engine.addSystem(renderingSystem);
        engine.addSystem(new PhysicsDebugSystem(world, cam));
        engine.addSystem(new PlayerControlSystem(bodyFactory, atlas));
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new BombSystem(atlas, bodyFactory,
                parent.assMan.manager.get("core/assets/sounds/bombSound.mp3")));
        engine.addSystem(new FlameSystem());
        engine.addSystem(new CollisionSystem());
        engine.addSystem(new MapSystem(bodyFactory, engine));

    }

    @Override
    public void show() {
        createPlayer();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        engine.update(delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !world.isLocked()){
            parent.changeScreen(BomberMan.LEVELS);
            engine.removeAllEntities();
            parent.testScreen = null;
        }
    }

    public void createPlayer(){
        Entity entity = engine.createEntity();
        BodyComponent body = engine.createComponent(BodyComponent.class);
        TransformComponent position = engine.createComponent(TransformComponent.class);
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        CollisionComponent colComp = engine.createComponent(CollisionComponent.class);
        TypeComponent type = engine.createComponent(TypeComponent.class);
        StateComponent stateCom = engine.createComponent(StateComponent.class);
        AnimationComponent animCom = engine.createComponent(AnimationComponent.class);


        body.body = bodyFactory.makePlayer(4,4 , 1.4f, BodyDef.BodyType.DynamicBody, true);
        position.position.set(4,4,0);
        type.type = TypeComponent.PLAYER;
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

        player.bombPower = BomberMan.STARTING_BOMB_POWER;
        player.movementSpeed = BomberMan.STARTING_MOVEMENT_SPEED;

        entity.add(body);
        entity.add(position);
        entity.add(texture);
        entity.add(player);
        entity.add(colComp);
        entity.add(type);
        entity.add(stateCom);
        entity.add(animCom);


        engine.addEntity(entity);
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
