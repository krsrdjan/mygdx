package com.mygdx.game;

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
        switch (keycode) {
            case 51:
                //move up
                gameBoard.moveHeroUp();
                break;
            case 32:
                //move right
                gameBoard.moveHeroRight();
                break;
            case 47:
                //move down
                gameBoard.moveHeroDown();
                break;
            case 29:
                //move left
                gameBoard.moveHeroLeft();
                break;
            case 43:
                //attack
                gameBoard.heroAttack();
                break;
            case 62:
                //attack
                gameBoard.endHeroTurn();
                break;
        }
        return super.keyDown(keycode);
    }
}
