package com.mygdx.loader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CustomAssetManager {
    public final AssetManager manager = new AssetManager();

    public final String skin = "flat/flat-earth-ui.json";

    public final String loadingImages = "loading/loading.atlas";

    public final String buttonSound = "sounds/buttonSound.wav";

    public final String bombMenu = "sounds/bombSound.mp3";

    public final String gameImages = "game/game.atlas";

    public void queueAddSkin(){
        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("flat/flat-earth-ui.atlas");
        manager.load(skin, Skin.class, params);
    }

    public void queueAddLoadingImages() {
        manager.load(loadingImages, TextureAtlas.class);
    }
    public void queueAddGameImages(){
        manager.load(gameImages, TextureAtlas.class);
    }

    public void queueAddSounds(){
        manager.load(buttonSound, Sound.class);
        manager.load(bombMenu, Sound.class);
    }


}
