package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class MyInputAdapter extends InputAdapter {

    private GameBoard gameBoard;

    public MyInputAdapter(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean keyDown(int keycode) {
        //System.out.println("touchDown called " + keycode);
        if (!gameBoard.isHeroTurn()) {
            return true; // swallow input during monster turn
        }
        switch (keycode) {
            case Input.Keys.W:
                gameBoard.moveHeroUp();
                break;
            case Input.Keys.D:
                gameBoard.moveHeroRight();
                break;
            case Input.Keys.S:
                gameBoard.moveHeroDown();
                break;
            case Input.Keys.A:
                gameBoard.moveHeroLeft();
                break;
            case Input.Keys.U:
                gameBoard.heroAttack();
                break;
            case Input.Keys.SPACE:
                gameBoard.endHeroTurn();
                break;
            case Input.Keys.I:
                gameBoard.getHero().switchWeapon();
                break;
        }
        
        return true;
    }
}
