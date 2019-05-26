package com.mygdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class CustomTextButton extends TextButton {
    private ButtonsCount parent;
    private final int number;
    private boolean pressable = true;
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



    public void setPressable(boolean pressable){
        this.pressable = pressable;
    }


    @Override
    public boolean isOver(){
        if(parent == null)
            return false;
        if(super.isOver() &&
                Gdx.input.getX() != posx && Gdx.input.getY() != posy) {
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
