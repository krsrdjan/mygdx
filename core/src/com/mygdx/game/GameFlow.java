package com.mygdx.game;

public final class GameFlow {

    public enum Phase {
        SPLASH,
        MAIN_MENU,
        HERO_SELECTION,
        RUN
    }

    private Phase phase = Phase.SPLASH;
    private float splashSeconds = 0f;
    private HeroKit highlightedKit = HeroKit.SQUIRE;
    private HeroKit runKit;
    private boolean quitRequested = false;

    public Phase phase() {
        return phase;
    }

    public HeroKit highlightedKit() {
        return highlightedKit;
    }

    public HeroKit runKit() {
        return runKit;
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    public boolean isAtmosphereStarted() {
        return true;
    }

    public void skipSplash() {
        if (phase == Phase.SPLASH) {
            phase = Phase.MAIN_MENU;
        }
    }

    public void advance(float seconds) {
        if (phase != Phase.SPLASH) {
            return;
        }
        splashSeconds += seconds;
        if (splashSeconds >= 2f) {
            phase = Phase.MAIN_MENU;
        }
    }

    public void play() {
        if (phase != Phase.MAIN_MENU) {
            return;
        }
        highlightedKit = HeroKit.SQUIRE;
        phase = Phase.HERO_SELECTION;
    }

    public void highlight(HeroKit kit) {
        if (phase != Phase.HERO_SELECTION || kit == null) {
            return;
        }
        highlightedKit = kit;
    }

    public void begin() {
        if (phase != Phase.HERO_SELECTION) {
            return;
        }
        runKit = highlightedKit;
        phase = Phase.RUN;
    }

    public void restart() {
        if (phase != Phase.RUN) {
            return;
        }
        runKit = null;
        highlightedKit = HeroKit.SQUIRE;
        phase = Phase.HERO_SELECTION;
    }

    public void back() {
        if (phase != Phase.HERO_SELECTION) {
            return;
        }
        phase = Phase.MAIN_MENU;
    }

    public void quit() {
        if (phase != Phase.MAIN_MENU) {
            return;
        }
        quitRequested = true;
    }

    public void moveHighlight(int direction) {
        if (phase != Phase.HERO_SELECTION || direction == 0) {
            return;
        }
        if (direction > 0) {
            highlightedKit = HeroKit.BRUTE;
        } else {
            highlightedKit = HeroKit.SQUIRE;
        }
    }
}
