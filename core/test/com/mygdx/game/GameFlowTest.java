package com.mygdx.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameFlowTest {

    @Test
    void coldLaunchShowsTheSplash() {
        GameFlow flow = new GameFlow();

        assertEquals(GameFlow.Phase.SPLASH, flow.phase());
    }

    @Test
    void skippingTheSplashOpensTheMainMenu() {
        GameFlow flow = new GameFlow();

        flow.skipSplash();

        assertEquals(GameFlow.Phase.MAIN_MENU, flow.phase());
    }

    @Test
    void splashOpensTheMainMenuAfterTwoSeconds() {
        GameFlow flow = new GameFlow();

        flow.advance(2f);

        assertEquals(GameFlow.Phase.MAIN_MENU, flow.phase());
    }

    @Test
    void splashStaysUpUntilTwoSecondsHavePassed() {
        GameFlow flow = new GameFlow();

        flow.advance(1.9f);

        assertEquals(GameFlow.Phase.SPLASH, flow.phase());
    }

    @Test
    void playerCanStartARunAsBruteAndRestartOntoSquire() {
        GameFlow flow = new GameFlow();

        flow.skipSplash();
        flow.play();
        flow.highlight(HeroKit.BRUTE);
        flow.begin();

        assertEquals(GameFlow.Phase.RUN, flow.phase());
        assertEquals(HeroKit.BRUTE, flow.runKit());

        flow.restart();

        assertEquals(GameFlow.Phase.HERO_SELECTION, flow.phase());
        assertEquals(HeroKit.SQUIRE, flow.highlightedKit());
        assertEquals(null, flow.runKit());
    }

    @Test
    void backFromHeroSelectionReturnsToTheMainMenu() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();
        flow.play();

        flow.back();

        assertEquals(GameFlow.Phase.MAIN_MENU, flow.phase());
        assertEquals(null, flow.runKit());
    }

    @Test
    void openingHeroSelectionHighlightsSquireAgain() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();
        flow.play();
        flow.highlight(HeroKit.BRUTE);
        flow.back();

        flow.play();

        assertEquals(HeroKit.SQUIRE, flow.highlightedKit());
    }

    @Test
    void quitFromTheMainMenuRequestsQuit() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();

        flow.quit();

        assertEquals(true, flow.isQuitRequested());
    }

    @Test
    void quitFromHeroSelectionDoesNotRequestQuit() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();
        flow.play();

        flow.quit();

        assertEquals(false, flow.isQuitRequested());
    }

    @Test
    void coldLaunchStartsAtmosphere() {
        GameFlow flow = new GameFlow();

        assertEquals(true, flow.isAtmosphereStarted());
    }

    @Test
    void beginAndRestartLeaveAtmosphereStarted() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();
        flow.play();
        flow.begin();
        flow.restart();

        assertEquals(true, flow.isAtmosphereStarted());
    }

    @Test
    void movingHighlightRightSelectsBruteThenStops() {
        GameFlow flow = new GameFlow();
        flow.skipSplash();
        flow.play();

        flow.moveHighlight(1);
        assertEquals(HeroKit.BRUTE, flow.highlightedKit());

        flow.moveHighlight(1);
        assertEquals(HeroKit.BRUTE, flow.highlightedKit());

        flow.moveHighlight(-1);
        assertEquals(HeroKit.SQUIRE, flow.highlightedKit());
    }

    @Test
    void squireAndBrutePublishTheirStartingLoadouts() {
        assertEquals("Sword", HeroKit.SQUIRE.weaponName());
        assertEquals(6, HeroKit.SQUIRE.attackBonus());
        assertEquals(1, HeroKit.SQUIRE.damage());
        assertEquals(8, HeroKit.SQUIRE.maxHp());
        assertEquals(14, HeroKit.SQUIRE.armorClass());
        assertEquals(8, HeroKit.SQUIRE.mov());
        assertEquals("hero.png", HeroKit.SQUIRE.portraitFile());

        assertEquals("Axe", HeroKit.BRUTE.weaponName());
        assertEquals(-1, HeroKit.BRUTE.attackBonus());
        assertEquals(2, HeroKit.BRUTE.damage());
        assertEquals(8, HeroKit.BRUTE.maxHp());
        assertEquals(14, HeroKit.BRUTE.armorClass());
        assertEquals(6, HeroKit.BRUTE.mov());
        assertEquals("hero.png", HeroKit.BRUTE.portraitFile());
    }
}
