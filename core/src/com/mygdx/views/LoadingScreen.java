package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.BomberMan;

public class LoadingScreen implements Screen {
    private BomberMan parent;
    private Stage stage;
    private Table table, loadingTable;
    private TextureAtlas atlas;
    private TextureAtlas.AtlasRegion background, loadingBomb;
    private int currentLoadingStage = 0;
    private final int Bombs = 16;

    public final int IMAGE = 1;
    public final int SOUNDS = 2;

    public float countDown = 2f;

    public LoadingScreen(BomberMan parent){
        super();
        this.parent = parent;
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        loadAssets();
    }
    @Override
    public void show() {
        table = new Table();
        table.setFillParent(true);
        table.setBackground(new TiledDrawable(background));

        loadingTable = new Table();
        for(int i = 0; i < Bombs; i++) {
            loadingTable.add(new LoadingBar(loadingBomb));
        }

        table.add(loadingTable);

        stage.addActor(table);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        if (parent.assMan.manager.update()) {
            currentLoadingStage+= 1;
            if(currentLoadingStage <= Bombs / 2){
                loadingTable.getCells().get((currentLoadingStage-1)*2).getActor().setVisible(true);
                loadingTable.getCells().get((currentLoadingStage-1)*2+1).getActor().setVisible(true);
            }
            switch(currentLoadingStage){
                case IMAGE:
                    System.out.println("Loaded Images");
                    break;
                case SOUNDS:
                    System.out.println("Loaded Sounds");
                    break;
                case 3:
                    System.out.println("Finished");
                    break;
            }
            if (currentLoadingStage >10){
                countDown -= delta;
                currentLoadingStage = 10;
                if(countDown < 0){
                    parent.changeScreen(BomberMan.MENU);
                }
            }
        }

        stage.act();
        stage.draw();
    }

    private void loadAssets(){
        parent.assMan.queueAddLoadingImages();
        parent.assMan.queueAddSounds();
        parent.assMan.queueAddGameImages();
        parent.assMan.queueAddSkin();
        parent.assMan.manager.finishLoading();

        atlas = parent.assMan.manager.get("loading/loading.atlas");
        background = atlas.findRegion("BackgroundTile");
        loadingBomb = atlas.findRegion("loading-bomb");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        stage.clear();
    }

    @Override
    public void dispose() {
        stage.dispose();
        atlas.dispose();
    }
}
