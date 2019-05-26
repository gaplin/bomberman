package com.mygdx.manager;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.BomberMan;

import java.util.concurrent.ConcurrentHashMap;

public class SoundManager {
    private BomberMan parent;
    private ConcurrentHashMap<String, Sound> hashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Float> defaultVolume;
    private Preferences prefs;
    private boolean muted = false;
    public SoundManager(BomberMan parent){
        this.parent = parent;
        this.prefs = parent.prefs;
        defaultVolume = new ConcurrentHashMap<>();

        defaultVolume.put("menuVol", BomberMan.MENU_VOLUME);
        defaultVolume.put("gameVol", BomberMan.GAME_VOLUME);
    }

    public void setMuted(boolean mute){
        muted = mute;
        if(muted){
            for(Sound sound : hashMap.values()){
                if(sound != null){
                    sound.stop();
                }
            }
        }
    }

    public boolean isMuted(){
        return muted;
    }

    public long playSound(String soundName, String volumeName){
        if(muted)
            return -1;
        if(hashMap.get(soundName) == null){
            hashMap.put(soundName, parent.assMan.manager.get("sounds/" + soundName));
        }
        return hashMap.get(soundName).play(prefs.getFloat(volumeName, defaultVolume.get(volumeName)));
    }

    public void decreaseVolume(String volumeName){
        float prev = prefs.getFloat(volumeName, defaultVolume.get(volumeName));
        float current = MathUtils.round(Math.max(0.0f, prev - 0.1f) * 10.0f) / 10.0f;
        prefs.putFloat(volumeName, current);
        prefs.flush();
    }

    public void increaseVolume(String volumeName){
        float prev = prefs.getFloat(volumeName, defaultVolume.get(volumeName));
        float current = MathUtils.round(Math.min(1.0f, prev + 0.1f) * 10.0f) / 10.0f;
        prefs.putFloat(volumeName, current);
        prefs.flush();
    }
}
