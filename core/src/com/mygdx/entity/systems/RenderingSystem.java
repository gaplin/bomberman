package com.mygdx.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.entity.components.TextureComponent;
import com.mygdx.entity.components.TransformComponent;
import com.mygdx.entity.components.TypeComponent;

import java.util.Comparator;

public class RenderingSystem extends SortedIteratingSystem {
    public static final  float PPM = 32.0f; // pixels per meter

    static final float FRUSTUM_WIDTH = Gdx.graphics.getWidth() / PPM;
    static final float FRUSTUM_HEIGHT = Gdx.graphics.getHeight() / PPM;


    private static Vector2 meterDimensions = new Vector2();
    private static Vector2 pixelDimensions = new Vector2();

    private FitViewport viewport;

    public static final float PIXELS_TO_METRES = 1.0f / PPM;

    public static Vector2 getScreenSizeInMeters() {
        meterDimensions.set(Gdx.graphics.getWidth() * PIXELS_TO_METRES,
                Gdx.graphics.getHeight() * PIXELS_TO_METRES);
        return meterDimensions;
    }
    public static Vector2 getScreenSizeInPixesl() {
        pixelDimensions.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return pixelDimensions;
    }

    public static float PixelsToMeters(float pixelValue) {
        return pixelValue * PIXELS_TO_METRES;
    }

    private SpriteBatch batch;
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera cam;

    private ComponentMapper<TextureComponent> textureM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<TypeComponent> typeM;

    public RenderingSystem(SpriteBatch batch){
        super(Family.all(TransformComponent.class, TextureComponent.class).get(), new ZComparator());
        comparator = new ZComparator();

        textureM = ComponentMapper.getFor(TextureComponent.class);
        transformM = ComponentMapper.getFor(TransformComponent.class);
        typeM = ComponentMapper.getFor(TypeComponent.class);

        renderQueue = new Array<>();

        this.batch = batch;

        cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
        viewport = new FitViewport(FRUSTUM_WIDTH, FRUSTUM_HEIGHT, cam);
        cam.position.set(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        renderQueue.add(entity);
    }

    public OrthographicCamera getCamera(){ return cam; }

    public FitViewport getViewport(){ return viewport; }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        renderQueue.sort(comparator);

        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.enableBlending();
        batch.begin();

        for (Entity entity : renderQueue) {
            TextureComponent tex = textureM.get(entity);
            TransformComponent t = transformM.get(entity);
            TypeComponent type = typeM.get(entity);

            if (tex.region == null || t.isHidden) {
                continue;
            }

            float width = tex.region.getRegionWidth();
            float height = tex.region.getRegionHeight();

            float originX = width / 2f;
            float originY = height / (type.type == TypeComponent.BOMB ? 2f : 2.8f);

            batch.draw(tex.region,
                    t.position.x - originX, t.position.y - originY,
                    originX, originY,
                    width, height,
                    (tex.mirror ? -1 : 1) * PixelsToMeters(t.scale.x), PixelsToMeters(t.scale.y),
                    t.rotation);
        }

        batch.end();
        renderQueue.clear();
    }
}
