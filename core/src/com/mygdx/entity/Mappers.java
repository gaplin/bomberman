package com.mygdx.entity;

import com.badlogic.ashley.core.ComponentMapper;
import com.mygdx.entity.components.*;

public class Mappers {
    public static final ComponentMapper<AnimationComponent> animationMapper =
            ComponentMapper.getFor(AnimationComponent.class);

    public static final ComponentMapper<BlockComponent> blockMapper =
            ComponentMapper.getFor(BlockComponent.class);

    public static final ComponentMapper<BodyComponent> bodyMapper =
            ComponentMapper.getFor(BodyComponent.class);

    public static final ComponentMapper<BombComponent> bombMapper =
            ComponentMapper.getFor(BombComponent.class);

    public static final ComponentMapper<FlameComponent> flameMapper =
            ComponentMapper.getFor(FlameComponent.class);

    public static final ComponentMapper<PlayerComponent> playerMapper =
            ComponentMapper.getFor(PlayerComponent.class);

    public static final ComponentMapper<StateComponent> stateMapper =
            ComponentMapper.getFor(StateComponent.class);

    public static final ComponentMapper<TextureComponent> textureMapper =
            ComponentMapper.getFor(TextureComponent.class);

    public static final ComponentMapper<TransformComponent> transformMapper =
            ComponentMapper.getFor(TransformComponent.class);

    public static final ComponentMapper<TypeComponent> typeMapper =
            ComponentMapper.getFor(TypeComponent.class);
}
