package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.entity.systems.MapSystem;


public class EnemyComponent implements Component, Pool.Poolable {

    public EnemyComponent(){
        reset();
    }

    public float time;
    public float newBombTimer;
    public float damageUpTimer;
    public boolean moving;
    public boolean correctingX;
    public boolean correctingY;
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;
    public boolean processingMove;
    public boolean isProcessingBomb;
    private int mapCounter;
    public Queue<Array<Array<MapSystem.MapObjs>>> maps;

    public Queue<MapSystem.MapObjs> move;
    public MapSystem.MapObjs lastMove;

    public void resetNewBombTimer(){
        newBombTimer = 10.0f;
    }

    public void resetDamageUpTimer(){
        damageUpTimer = 15.0f;
    }

    public void resetDirections(){
        up = false;
        down = false;
        left = false;
        right = false;
        processingMove = false;
    }

    @Override
    public void reset() {
        mapCounter = 3;
        time = 0.0f;
        resetNewBombTimer();
        resetDamageUpTimer();
        resetDirections();
        move = new Queue<>();
        lastMove = null;
        correctingX = false;
        correctingY = false;
        isProcessingBomb = false;
        if(maps == null){
            maps = new Queue<>();
            for(int i = 0; i < mapCounter; i++){
                Array<Array<MapSystem.MapObjs>> map = new Array<>();
                for(int j = 0; j <= MapSystem.height; j++){
                    map.add(new Array<>());
                    for(int k = 0; k <= MapSystem.width; k++){
                        map.get(j).add(new MapSystem.MapObjs());
                    }
                }
                maps.addLast(map);
            }
        }
    }
}