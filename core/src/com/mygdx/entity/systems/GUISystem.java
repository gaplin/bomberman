package com.mygdx.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.entity.Mappers;
import com.mygdx.entity.components.PlayerComponent;
import com.mygdx.entity.components.StatsComponent;
import com.mygdx.entity.components.TextureComponent;

import java.util.Comparator;

public class GUISystem extends IteratingSystem {
    private Stage stage;
    private Array<Entity> array;
    private ImageButton[] buttons = new ImageButton[5];
    private Label[] labels = new Label[5];
    private Comparator<Entity> playerComparator;
    public GUISystem(Skin skin, TextureAtlas gameAtlas, TextureAtlas backgroundAtlas){
        super(Family.all(StatsComponent.class).get());
        TextureAtlas.AtlasRegion background = backgroundAtlas.findRegion("BackgroundTile");
        array = new Array<>();
        playerComparator = new PlayerComparator();

        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Table table = new Table();
        stage.addActor(table);
        table.setPosition(Gdx.graphics.getWidth() - 100f, Gdx.graphics.getHeight() / 2f);
        table.setBackground(new TiledDrawable(background));
        table.setWidth(170f);
        table.setHeight(Gdx.graphics.getHeight());
        table.setPosition(Gdx.graphics.getWidth() - 170f, 0.0f);
        table.left().top();
        for(int i = 1; i <= 4; i++){
            buttons[i] = new ImageButton(new TiledDrawable(gameAtlas.findRegion("player/Bman_head")));
            labels[i] = new Label("3", skin, "title", "white");
            table.row().pad(0.0f, 20.0f, 45.0f, 0.0f);
            table.add(buttons[i]);
            table.add(labels[i]);
        }
    }

    private static class PlayerComparator implements Comparator<Entity>{

        @Override
        public int compare(Entity o1, Entity o2) {
            PlayerComponent p1 = Mappers.playerMapper.get(o1);
            PlayerComponent p2 = Mappers.playerMapper.get(o2);
            return Integer.compare(p1.ID, p2.ID);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        array.add(entity);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        array.sort(playerComparator);
        boolean[] present = new boolean[5];
        for(Entity entity : array){
            PlayerComponent player = Mappers.playerMapper.get(entity);
            present[player.ID] = true;
            TextureComponent texture = Mappers.textureMapper.get(entity);
            StatsComponent stats = Mappers.statsMapper.get(entity);
            buttons[player.ID].getImage().setColor(texture.color);
            labels[player.ID].setColor(texture.color.r, texture.color.g, texture.color.b, 1.0f);
            labels[player.ID].setText(Integer.toString(stats.HP));
            if(stats.HP == 0)
                labels[player.ID].setVisible(false);
        }
        for(int i = 1; i <= 4; i++){
            if(!present[i]){
                buttons[i].setVisible(false);
                labels[i].setVisible(false);
            }
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
        array.clear();
    }
}
