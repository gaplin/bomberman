package com.mygdx.loader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CustomAssetManager {
    public final AssetManager manager = new AssetManager();

    public final String skin = "flat/flat-earth-ui.json";

    public final String loadingImages = "loading/loading.atlas";

    public void queueAddSkin(){
        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("flat/flat-earth-ui.atlas");
        manager.load(skin, Skin.class, params);
    }

    public void queueAddLoadingImages() {
        manager.load(loadingImages, TextureAtlas.class);
    }
}
