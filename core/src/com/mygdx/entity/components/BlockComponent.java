package com.mygdx.entity.components;

import com.badlogic.ashley.core.Component;

public class BlockComponent implements Component {
    public static final int WALL = 0;
    public static final int DESTROYABLE = 1;

    public int type = 0;
}
