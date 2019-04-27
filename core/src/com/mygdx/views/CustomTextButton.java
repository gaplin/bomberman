package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mygdx.game.BomberMan;

public class CustomTextButton extends TextButton {
    private ButtonsCount parent;
    private final int number;
    private boolean pressable = true;
    Sound click;
    private int posx = Gdx.input.getX(), posy = Gdx.input.getY();
    CustomTextButton(String text, Skin skin, int number, ButtonsCount parent){
        super(text, skin);
        this.number = number;
        this.parent = parent;
    }

    CustomTextButton(String text, Skin skin, String styleName, int number, ButtonsCount parent){
        super(text, skin, styleName);
        this.number = number;
        this.parent = parent;
    }

    CustomTextButton(String text, Skin skin, String styleName, int number, ButtonsCount parent, Sound click){
        this(text, skin, styleName, number, parent);
        this.click = click;
    }

    CustomTextButton(String text, Skin skin, int number, ButtonsCount parent, Sound click){
        this(text, skin, number, parent);
        this.click = click;
    }


    public void setPressable(boolean pressable){
        this.pressable = pressable;
    }
    public void setClick(Sound click){
        this.click = click;
    }


    @Override
    public boolean isOver(){
        if(parent == null)
            return false;
        if(super.isOver() &&
                Gdx.input.getX() != posx && Gdx.input.getY() != posy) {
            if(parent.pointer != number && click != null)
                click.play(BomberMan.MENU_VOLUME);
            parent.pointer = number;
            posx = Gdx.input.getX();
            posy = Gdx.input.getY();
        }
        return parent.pointer == number;
    }

    @Override
    public boolean isPressed(){
        if(parent == null || !pressable)
            return false;
        boolean isPressed = super.isPressed();
        if(isPressed) {
            parent.pressed = number;
            return true;
        }
        isPressed = isOver() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        if(isPressed) {
            setChecked(true);
        }
        if(!isPressed && parent.pressed == number)
            parent.pressed = -1;
        return isPressed;
    }
}
