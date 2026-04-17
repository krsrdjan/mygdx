package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class MyInputAdapter extends InputAdapter {

    public static final int MOVE_UP_KEY = Input.Keys.W;
    public static final int MOVE_RIGHT_KEY = Input.Keys.D;
    public static final int MOVE_DOWN_KEY = Input.Keys.S;
    public static final int MOVE_LEFT_KEY = Input.Keys.A;
    public static final int ATTACK_KEY = Input.Keys.U;
    public static final int END_TURN_KEY = Input.Keys.SPACE;
    public static final int SWITCH_WEAPON_KEY = Input.Keys.I;

    public interface HudClickHandler {
        boolean handleHudTouch(int screenX, int screenY);
    }

    private GameBoard gameBoard;
    private OrthographicCamera camera;
    private HudClickHandler hudClickHandler;
    private final Vector3 worldCoords = new Vector3();

    public MyInputAdapter(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void setHudClickHandler(HudClickHandler hudClickHandler) {
        this.hudClickHandler = hudClickHandler;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (camera == null) {
            return false;
        }
        if (!gameBoard.getHero().isAlive()) {
            return false;
        }
        if (hudClickHandler != null && hudClickHandler.handleHudTouch(screenX, screenY)) {
            return true;
        }
        if (!gameBoard.isHeroTurn()) {
            return true;
        }

        worldCoords.set(screenX, screenY, 0);
        camera.unproject(worldCoords);

        int tileX = (int) Math.floor(worldCoords.x / GameBoard.SQUARE_SIZE);
        int tileY = (int) Math.floor(worldCoords.y / GameBoard.SQUARE_SIZE);

        gameBoard.heroClickOnTile(tileX, tileY);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        //System.out.println("touchDown called " + keycode);
        if (!gameBoard.getHero().isAlive()) {
            return true; // swallow gameplay input when game is over
        }
        if (!gameBoard.isHeroTurn()) {
            return true; // swallow input during monster turn
        }

        if (isMoveKey(keycode) && gameBoard.getHero().getSpeed() <= 0) {
            gameBoard.showToast("No moves left. End your turn.");
            return true;
        }

        switch (keycode) {
            case MOVE_UP_KEY:
                gameBoard.moveHeroUp();
                break;
            case MOVE_RIGHT_KEY:
                gameBoard.moveHeroRight();
                break;
            case MOVE_DOWN_KEY:
                gameBoard.moveHeroDown();
                break;
            case MOVE_LEFT_KEY:
                gameBoard.moveHeroLeft();
                break;
            case ATTACK_KEY:
                gameBoard.heroAttack();
                break;
            case END_TURN_KEY:
                gameBoard.endHeroTurn();
                break;
            case SWITCH_WEAPON_KEY:
                gameBoard.getHero().switchWeapon();
                break;
        }
        
        return true;
    }

    private boolean isMoveKey(int keycode) {
        return keycode == MOVE_UP_KEY
                || keycode == MOVE_RIGHT_KEY
                || keycode == MOVE_DOWN_KEY
                || keycode == MOVE_LEFT_KEY;
    }

    public String getMoveKeysLabel() {
        return Input.Keys.toString(MOVE_UP_KEY)
                + "/"
                + Input.Keys.toString(MOVE_LEFT_KEY)
                + "/"
                + Input.Keys.toString(MOVE_DOWN_KEY)
                + "/"
                + Input.Keys.toString(MOVE_RIGHT_KEY);
    }

    public String getAttackKeyLabel() {
        return Input.Keys.toString(ATTACK_KEY);
    }

    public String getSwitchWeaponKeyLabel() {
        return Input.Keys.toString(SWITCH_WEAPON_KEY);
    }

    public String getEndTurnKeyLabel() {
        return Input.Keys.toString(END_TURN_KEY);
    }
}
