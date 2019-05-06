package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.TransformComponent;

import java.util.Comparator;

public class ZComparator implements Comparator<Entity> {
    public ZComparator(){
    }
    @Override
    public int compare(Entity entityA, Entity entityB){
        TransformComponent posA = Mappers.transformMapper.get(entityA);
        TransformComponent posB = Mappers.transformMapper.get(entityB);
        if(posA.position.z == posB.position.z){
            if(posA.position.x == posB.position.x){
                if(posA.position.y == posB.position.y)
                    return 0;
                else if(posA.position.y > posB.position.y)
                    return 1;
                else
                    return -1;
            }
            else if(posA.position.x > posB.position.x)
                return 1;
            else
                return -1;
        }
        else if(posA.position.z > posB.position.z)
            return 1;
        else
            return -1;
    }
}
