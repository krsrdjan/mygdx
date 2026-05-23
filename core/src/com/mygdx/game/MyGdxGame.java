package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter {
	GameBoard gameBoard;
	SpriteBatch batch;
	Texture tile;
	TiledDrawable tiledDrawable;
	TextureRegion textureRegion;
	Music music;
	OrthographicCamera camera;
	OrthographicCamera hudCamera;
	Viewport worldViewport;
	Viewport hudViewport;
	// Minimum logical world size (16:9 HD). ExtendViewport grows width or height
	// to match the screen aspect, so tall phones (portrait or 20:9 landscape) fill
	// the screen with no letterboxing; extra strips show more dungeon / world.
	// 16:9 multiples (720p, 1080p, etc.) still map 1:1 at this minimum size.
	private static final float WORLD_VIEW_WIDTH = 1280f;
	private static final float WORLD_VIEW_HEIGHT = 720f;
	BitmapFont font;
	ShapeRenderer shapeRenderer;
	MyInputAdapter inputAdapter;
	private List<Toast> activeToasts = new ArrayList<>();
	private final GlyphLayout glyphLayout = new GlyphLayout();
	private final Rectangle restartButtonBounds = new Rectangle();
	private final Vector3 touchPoint = new Vector3();
	private final Vector3 mouseWorldCoords = new Vector3();
	private final Vector3 mouseHudCoords = new Vector3();

	// HUD layout anchored to the bottom; minimum width 1280 (matches world min).
	// With ExtendViewport, wider screens stretch the bar; taller screens add empty
	// space above the HUD. Panel columns are equal thirds of the current HUD width.
	private static final float LOG_STRIP_H = 22f;
	private static final float HUD_PANEL_Y_BOTTOM = LOG_STRIP_H;
	private static final float HUD_PANEL_H = 160f;
	private static final float HUD_PANEL_Y_TOP = HUD_PANEL_Y_BOTTOM + HUD_PANEL_H;
	// Play area logical height (screen above HUD strip); see docs/adr/0002-play-area-viewport.md
	private static final float WORLD_PLAY_HEIGHT = WORLD_VIEW_HEIGHT - HUD_PANEL_Y_TOP;
	private static final float MOBILE_DUNGEON_ZOOM = 2f;
	private final float worldPlayMinWidth;
	private final float worldPlayMinHeight;

	public MyGdxGame() {
		this(false);
	}

	public MyGdxGame(boolean mobileWebPlay) {
		if (mobileWebPlay) {
			worldPlayMinWidth = WORLD_VIEW_WIDTH / MOBILE_DUNGEON_ZOOM;
			worldPlayMinHeight = WORLD_PLAY_HEIGHT / MOBILE_DUNGEON_ZOOM;
		} else {
			worldPlayMinWidth = WORLD_VIEW_WIDTH;
			worldPlayMinHeight = WORLD_PLAY_HEIGHT;
		}
	}
	private static final float LEFT_X0 = 0f;
	private static final float TOAST_Y = 195f;
	private static final float WEAPON_CARD_W = 114f;
	private static final float WEAPON_CARD_H = 44f;
	private static final float WEAPON_CARD_GAP = 8f;
	// Recomputed on resize — ExtendViewport widens the HUD on ultrawide / mobile landscape.
	private float hudLeftX1;
	private float hudCenterX0;
	private float hudCenterX1;
	private float hudRightX0;

	// HUD colors
	private static final Color HUD_BG = new Color(0.08f, 0.06f, 0.05f, 1f);
	private static final Color LOG_BG = new Color(0.05f, 0.04f, 0.03f, 1f);
	private static final Color BORDER = new Color(0.30f, 0.25f, 0.18f, 1f);
	private static final Color GOLD = new Color(0.85f, 0.68f, 0.28f, 1f);
	private static final Color GOLD_PILL_TEXT = new Color(0.12f, 0.10f, 0.06f, 1f);
	private static final Color HP_RED = new Color(0.85f, 0.25f, 0.25f, 1f);
	private static final Color MOV_BLUE = new Color(0.30f, 0.55f, 0.90f, 1f);
	private static final Color MUTED = new Color(0.55f, 0.52f, 0.48f, 1f);
	private static final Color DIM_TRACK = new Color(0.20f, 0.17f, 0.14f, 1f);
	private static final Color LOG_DIM = new Color(0.45f, 0.45f, 0.45f, 1f);
	private static final Color LOG_HIGHLIGHT = new Color(0.95f, 0.55f, 0.30f, 1f);

	// Weapon card click zones (populated in create())
	private final Rectangle weaponCard1Bounds = new Rectangle();
	private final Rectangle weaponCard2Bounds = new Rectangle();
	private final Rectangle weaponCard3Bounds = new Rectangle();
	private final Rectangle endTurnButtonBounds = new Rectangle();
	private int hudScreenPx;
	@Override
	public void create () {	// this is done once
		gameBoard = new GameBoard();
		batch = new SpriteBatch();

		tile = new Texture("tile-small.jpeg");
		textureRegion = new TextureRegion(tile);
		tiledDrawable = new TiledDrawable(textureRegion);
		
		// World viewport: play area only. HUD viewport: full screen (see ADR 0002).
		// Mobile web play uses a smaller world minimum (×2 dungeon zoom); HUD unchanged (ADR 0003).
		camera = new OrthographicCamera();
		camera.setToOrtho(false, worldPlayMinWidth, worldPlayMinHeight);
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, WORLD_VIEW_WIDTH, WORLD_VIEW_HEIGHT);
		worldViewport = new ExtendViewport(worldPlayMinWidth, worldPlayMinHeight, camera);
		hudViewport = new ExtendViewport(WORLD_VIEW_WIDTH, WORLD_VIEW_HEIGHT, hudCamera);

		font = new BitmapFont();
		shapeRenderer = new ShapeRenderer();
		
		updateHudPanelLayout();

		inputAdapter = new MyInputAdapter(gameBoard);
		inputAdapter.setViewport(worldViewport);
		inputAdapter.setHudClickHandler(new MyInputAdapter.HudClickHandler() {
			public boolean handleHudTouch(int sx, int sy) {
				return MyGdxGame.this.handleHudClick(sx, sy);
			}
		});
		Gdx.input.setInputProcessor(inputAdapter);

		// Web (TeaVM): first resize can be 0x0 before the canvas has layout. Skip
		// viewport updates until dimensions are positive to avoid glViewport(0,0,0,0).
		updateViewportsIfSized(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

		// Set toast notifier for game board
		gameBoard.setToastNotifier(new StringCallback() {
			public void call(String value) {
				showToast(value);
			}
		});

        music = Gdx.audio.newMusic(Gdx.files.internal("atmosphere.mp3"));
        music.setLooping(true);
        music.setVolume(AudioConfig.VOLUME);
        music.play();
	}

	@Override
	public void render () { // this is loop rendered 60 FPS
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Update toasts
		updateToasts(deltaTime);
		
		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

		// Recover if we skipped an initial 0x0 resize (TeaVM / mobile WebView).
		if (worldViewport.getScreenWidth() <= 0 || worldViewport.getScreenHeight() <= 0) {
			updateViewportsIfSized(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		}
		if (worldViewport.getScreenWidth() <= 0 || worldViewport.getScreenHeight() <= 0) {
			return;
		}

		// Update camera to follow hero
		updateCamera();
		worldViewport.apply();
		batch.setProjectionMatrix(camera.combined);

		HdpiUtils.glScissor(
				worldViewport.getScreenX(),
				worldViewport.getScreenY(),
				worldViewport.getScreenWidth(),
				worldViewport.getScreenHeight());
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

		batch.begin();

		//draw tiles with repeat - use world coordinates
		float worldWidth = GameBoard.BOARD_SQUARE_WIDTH * GameBoard.SQUARE_SIZE;
		float worldHeight = GameBoard.BOARD_SQUARE_HEIGHT * GameBoard.SQUARE_SIZE;
		tiledDrawable.draw(batch, 0, 0, worldWidth, worldHeight);

		//draw each texture from the board
		for (int i = 0; i < GameBoard.BOARD_SQUARE_WIDTH; i++) {
			for (int j = 0; j < GameBoard.BOARD_SQUARE_HEIGHT; j++) {
				// Draw square texture (walls, floors, etc.)
				Texture tex = gameBoard.getTexture(i,j);
				if (tex != null) {
					batch.draw(tex,
							i * GameBoard.SQUARE_SIZE,
							j * GameBoard.SQUARE_SIZE,
							GameBoard.SQUARE_SIZE,
							GameBoard.SQUARE_SIZE);
				}
				
				// Draw floor item, then actor (hero/monster) so actors render on top
				Square square = gameBoard.getSquare(i, j);
				if (square != null && (square.isExplored() || gameBoard.exploredAll)) {
					Creature actor = gameBoard.getActorAt(i, j);
					Item floorItem = gameBoard.getItemAt(i, j);
					if (floorItem != null && actor == null) {
						drawCreature(floorItem, i, j);
					}
					if (actor != null) {
						drawCreature(actor, i, j);
					}
				}
			}
		}

		batch.end();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

		// HUD rendering (screen space)
		hudViewport.apply(true);
		renderHud();

		// Render floor item / monster hover popup
		renderPlayAreaHoverPopup();

		// Render toasts above HUD
		renderToasts();
		renderVictoryDialog();
		renderGameOverDialog();
		handleEndRunInput();
	}

	private void drawCreature(Creature creature, int tileX, int tileY) {
		Texture creatureTex = creature.getTexture();
		if (creatureTex != null) {
			batch.draw(creatureTex,
					tileX * GameBoard.SQUARE_SIZE,
					tileY * GameBoard.SQUARE_SIZE,
					GameBoard.SQUARE_SIZE,
					GameBoard.SQUARE_SIZE);
		}
	}
	
	private void updateCamera() {
		Position heroPos = gameBoard.getHero().getPosition();

		float heroWorldX = heroPos.x * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;
		float heroWorldY = heroPos.y * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;

		float worldWidth = GameBoard.BOARD_SQUARE_WIDTH * GameBoard.SQUARE_SIZE;
		float worldHeight = GameBoard.BOARD_SQUARE_HEIGHT * GameBoard.SQUARE_SIZE;
		float halfViewportWidth = camera.viewportWidth / 2f;
		float halfViewportHeight = camera.viewportHeight / 2f;
		float maxCameraY = worldHeight - halfViewportHeight;

		float clampedX = Math.max(halfViewportWidth, Math.min(heroWorldX, worldWidth - halfViewportWidth));
		float clampedY = Math.max(halfViewportHeight, Math.min(heroWorldY, maxCameraY));

		camera.position.set(clampedX, clampedY, 0);
		camera.update();
	}
	
	@Override
	public void resize(int width, int height) {
		updateViewportsIfSized(width, height);
	}

	/** No-op if width or height are zero (avoids broken GL viewport on web before layout). */
	private void updateViewportsIfSized(int width, int height) {
		if (width <= 0 || height <= 0) {
			return;
		}
		hudViewport.update(width, height, true);
		updateHudPanelLayout();
		hudScreenPx = Math.round(HUD_PANEL_Y_TOP / hudCamera.viewportHeight * height);
		int worldScreenH = height - hudScreenPx;
		if (worldScreenH <= 0) {
			return;
		}
		// ExtendViewport.update() resets screen bounds; pass play-area height for scaling,
		// then shift the glViewport above the HUD strip.
		worldViewport.update(width, worldScreenH, false);
		worldViewport.setScreenY(worldViewport.getScreenY() + hudScreenPx);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		tile.dispose();
		font.dispose();
		shapeRenderer.dispose();
		if (music != null) {
			music.stop();
			music.dispose();
		}
		SoundCache.dispose();
		TextureCache.dispose();
		gameBoard = null;
	}
	
	private void showToast(String message) {
		activeToasts.add(new Toast(message));
	}
	
	private void updateToasts(float deltaTime) {
		Iterator<Toast> iterator = activeToasts.iterator();
		while (iterator.hasNext()) {
			Toast toast = iterator.next();
			toast.update(deltaTime);
			if (toast.isExpired()) {
				iterator.remove();
			}
		}
	}
	
	private void renderHud() {
		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		batch.setProjectionMatrix(hudCamera.combined);

		Hero hero = gameBoard.getHero();
		Monster adjacent = gameBoard.getAdjacentMonsterToHero();

		renderHudFilled(hero, adjacent);
		renderHudLines(hero, adjacent);
		renderHudText(hero, adjacent);
	}

	private void renderHudFilled(Hero hero, Monster adjacent) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Main panel background
		shapeRenderer.setColor(HUD_BG);
		shapeRenderer.rect(0, HUD_PANEL_Y_BOTTOM, hudCamera.viewportWidth, HUD_PANEL_H);

		// Combat log strip background
		shapeRenderer.setColor(LOG_BG);
		shapeRenderer.rect(0, 0, hudCamera.viewportWidth, LOG_STRIP_H);

		// Hero HP bar
		float hpBarX = LEFT_X0 + 52f;
		float hpBarY = HUD_PANEL_Y_BOTTOM + 96f;
		float hpBarW = 160f;
		float hpBarH = 10f;
		shapeRenderer.setColor(DIM_TRACK);
		shapeRenderer.rect(hpBarX, hpBarY, hpBarW, hpBarH);
		int maxHp = Math.max(1, hero.getMaxHealth());
		float hpFill = Math.max(0f, Math.min(1f, hero.getHealth() / (float) maxHp));
		shapeRenderer.setColor(HP_RED);
		shapeRenderer.rect(hpBarX, hpBarY, hpBarW * hpFill, hpBarH);

		// Hero MOV bar
		float movBarX = hpBarX;
		float movBarY = HUD_PANEL_Y_BOTTOM + 74f;
		float movBarW = hpBarW;
		float movBarH = hpBarH;
		shapeRenderer.setColor(DIM_TRACK);
		shapeRenderer.rect(movBarX, movBarY, movBarW, movBarH);
		int maxSpeed = Math.max(1, hero.getMaxSpeed());
		float movFill = Math.max(0f, Math.min(1f, hero.getSpeed() / (float) maxSpeed));
		shapeRenderer.setColor(MOV_BLUE);
		shapeRenderer.rect(movBarX, movBarY, movBarW * movFill, movBarH);

		// Weapon cards: dark card backgrounds + gold underline on active
		Weapon activeWeapon = hero.getCurrentWeapon();
		List<Weapon> inv = new ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = weaponCardBounds();
		for (int i = 0; i < cardRects.length; i++) {
			if (i >= inv.size()) break;
			Rectangle r = cardRects[i];
			shapeRenderer.setColor(DIM_TRACK);
			shapeRenderer.rect(r.x, r.y, r.width, r.height);
			if (inv.get(i) == activeWeapon) {
				shapeRenderer.setColor(GOLD);
				shapeRenderer.rect(r.x, r.y, r.width, 2f);
			}
		}

		// "Your turn" pill or "Enemy turn" pill
		float pillW = 110f;
		float pillH = 22f;
		float pillX = hudCenterX0 + (hudCenterX1 - hudCenterX0 - pillW) / 2f;
		float pillY = HUD_PANEL_Y_TOP - 28f;
		if (gameBoard.isHeroTurn()) {
			shapeRenderer.setColor(GOLD);
		} else {
			shapeRenderer.setColor(DIM_TRACK);
		}
		shapeRenderer.rect(pillX, pillY, pillW, pillH);

		// END TURN button
		if (gameBoard.isHeroTurn()) {
			shapeRenderer.setColor(GOLD);
		} else {
			shapeRenderer.setColor(DIM_TRACK);
		}
		shapeRenderer.rect(endTurnButtonBounds.x, endTurnButtonBounds.y, endTurnButtonBounds.width, endTurnButtonBounds.height);

		// Enemy panel content
		if (adjacent != null) {
			int enemyMaxHp = Math.max(1, adjacent.getMaxHealth());
			float eBarX = hudRightX0 + 48f;
			float eBarY = HUD_PANEL_Y_BOTTOM + 96f;
			float eBarW = 150f;
			float eBarH = 10f;
			shapeRenderer.setColor(DIM_TRACK);
			shapeRenderer.rect(eBarX, eBarY, eBarW, eBarH);
			float eFill = Math.max(0f, Math.min(1f, adjacent.getHealth() / (float) enemyMaxHp));
			shapeRenderer.setColor(HP_RED);
			shapeRenderer.rect(eBarX, eBarY, eBarW * eFill, eBarH);

			// Enemy MOV bar
			float eMovBarX = eBarX;
			float eMovBarY = HUD_PANEL_Y_BOTTOM + 72f;
			float eMovBarW = eBarW;
			float eMovBarH = eBarH;
			shapeRenderer.setColor(DIM_TRACK);
			shapeRenderer.rect(eMovBarX, eMovBarY, eMovBarW, eMovBarH);
			int eMaxSpeed = Math.max(1, adjacent.getMaxSpeed());
			float eMovFill = Math.max(0f, Math.min(1f, adjacent.getSpeed() / (float) eMaxSpeed));
			shapeRenderer.setColor(MOV_BLUE);
			shapeRenderer.rect(eMovBarX, eMovBarY, eMovBarW * eMovFill, eMovBarH);
		}

		shapeRenderer.end();
	}

	private void renderHudLines(Hero hero, Monster adjacent) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

		// Top border
		shapeRenderer.setColor(BORDER);
		shapeRenderer.line(0, HUD_PANEL_Y_TOP, hudCamera.viewportWidth, HUD_PANEL_Y_TOP);
		// Divider above log strip
		shapeRenderer.line(0, HUD_PANEL_Y_BOTTOM, hudCamera.viewportWidth, HUD_PANEL_Y_BOTTOM);
		// Vertical panel dividers
		shapeRenderer.line(hudLeftX1, HUD_PANEL_Y_BOTTOM, hudLeftX1, HUD_PANEL_Y_TOP);
		shapeRenderer.line(hudCenterX1, HUD_PANEL_Y_BOTTOM, hudCenterX1, HUD_PANEL_Y_TOP);

		// Weapon card borders
		Weapon activeWeapon = hero.getCurrentWeapon();
		List<Weapon> inv = new ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = weaponCardBounds();
		for (int i = 0; i < cardRects.length; i++) {
			if (i >= inv.size()) break;
			Rectangle r = cardRects[i];
			if (inv.get(i) == activeWeapon) {
				shapeRenderer.setColor(GOLD);
			} else {
				shapeRenderer.setColor(BORDER);
			}
			shapeRenderer.rect(r.x, r.y, r.width, r.height);
		}

		// END TURN button border
		shapeRenderer.setColor(gameBoard.isHeroTurn() ? GOLD : BORDER);
		shapeRenderer.rect(endTurnButtonBounds.x, endTurnButtonBounds.y, endTurnButtonBounds.width, endTurnButtonBounds.height);

		shapeRenderer.end();
	}

	private void renderHudText(Hero hero, Monster adjacent) {
		batch.begin();

		// Hero portrait (top-left) + HERO label to its right
		Texture heroPortrait = hero.getTexture();
		if (heroPortrait != null) {
			batch.setColor(Color.WHITE);
			batch.draw(heroPortrait, LEFT_X0 + 12f, HUD_PANEL_Y_TOP - 44f, 32f, 32f);
		}
		font.setColor(GOLD);
		font.draw(batch, "HERO", LEFT_X0 + 52f, HUD_PANEL_Y_TOP - 20f);

		// HP
		font.setColor(Color.WHITE);
		font.draw(batch, "HP", LEFT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 105f);
		String hpText = hero.getHealth() + " / " + hero.getMaxHealth();
		font.draw(batch, hpText, LEFT_X0 + 222f, HUD_PANEL_Y_BOTTOM + 105f);

		// MOV
		font.draw(batch, "MOV", LEFT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 83f);
		font.draw(batch, String.valueOf(hero.getSpeed()), LEFT_X0 + 222f, HUD_PANEL_Y_BOTTOM + 83f);

		// Weapon cards
		Weapon activeWeapon = hero.getCurrentWeapon();
		List<Weapon> inv = new ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = weaponCardBounds();
		for (int i = 0; i < cardRects.length; i++) {
			if (i >= inv.size()) break;
			Weapon w = inv.get(i);
			Rectangle r = cardRects[i];
			if (w == activeWeapon) {
				font.setColor(Color.WHITE);
			} else {
				font.setColor(MUTED);
			}
			glyphLayout.setText(font, w.getName());
			font.draw(batch, w.getName(), r.x + (r.width - glyphLayout.width) / 2f, r.y + r.height - 8f);
			int hitPct = Math.round(w.getChanceToHit() * 100);
			String stats = hitPct + "%  " + w.getDamage() + " dmg";
			font.setColor(MUTED);
			glyphLayout.setText(font, stats);
			font.draw(batch, stats, r.x + (r.width - glyphLayout.width) / 2f, r.y + 16f);
		}

		// Center panel: pill + controls
		float pillW = 110f;
		float pillH = 22f;
		float pillX = hudCenterX0 + (hudCenterX1 - hudCenterX0 - pillW) / 2f;
		float pillY = HUD_PANEL_Y_TOP - 28f;
		String pillText = gameBoard.isHeroTurn() ? "YOUR TURN" : "ENEMY TURN";
		glyphLayout.setText(font, pillText);
		float pillTextX = pillX + (pillW - glyphLayout.width) / 2f;
		float pillTextY = pillY + (pillH + glyphLayout.height) / 2f - 1f;
		if (gameBoard.isHeroTurn()) {
			font.setColor(GOLD_PILL_TEXT);
		} else {
			font.setColor(MUTED);
		}
		font.draw(batch, pillText, pillTextX, pillTextY);

		// Round label below pill
		font.setColor(MUTED);
		String roundText = "Round " + gameBoard.getRound();
		glyphLayout.setText(font, roundText);
		font.draw(batch, roundText,
				hudCenterX0 + (hudCenterX1 - hudCenterX0 - glyphLayout.width) / 2f,
				HUD_PANEL_Y_TOP - 36f);

		String roomsText = "Rooms " + gameBoard.getRoomsExploredCount() + " / " + gameBoard.getTotalRooms();
		glyphLayout.setText(font, roomsText);
		font.draw(batch, roomsText,
				hudCenterX0 + (hudCenterX1 - hudCenterX0 - glyphLayout.width) / 2f,
				HUD_PANEL_Y_TOP - 54f);

		// END TURN button text
		String endBtnText = "END TURN";
		glyphLayout.setText(font, endBtnText);
		float endBtnTextX = endTurnButtonBounds.x + (endTurnButtonBounds.width - glyphLayout.width) / 2f;
		float endBtnTextY = endTurnButtonBounds.y + (endTurnButtonBounds.height + glyphLayout.height) / 2f;
		font.setColor(gameBoard.isHeroTurn() ? GOLD_PILL_TEXT : MUTED);
		font.draw(batch, endBtnText, endBtnTextX, endBtnTextY);

		// Control hints (two columns) shifted below Round label
		font.setColor(MUTED);
		float centerMid = (hudCenterX0 + hudCenterX1) / 2f;
		float hintY1 = HUD_PANEL_Y_BOTTOM + 98f;
		float hintY2 = HUD_PANEL_Y_BOTTOM + 68f;
		drawCenteredText("WASD", (hudCenterX0 + centerMid) / 2f, hintY1);
		drawCenteredText("U / click", (centerMid + hudCenterX1) / 2f, hintY1);
		drawCenteredText("SPACE  end turn", (hudCenterX0 + centerMid) / 2f, hintY2);
		drawCenteredText("I / Switch weapon", (centerMid + hudCenterX1) / 2f, hintY2);

		// Enemy panel (mirror hero: portrait top-left, name to its right)
		if (adjacent != null) {
			Texture portrait = adjacent.getTexture();
			if (portrait != null) {
				batch.setColor(Color.WHITE);
				batch.draw(portrait, hudRightX0 + 12f, HUD_PANEL_Y_TOP - 44f, 32f, 32f);
			}

			font.setColor(GOLD);
			font.draw(batch, adjacent.getName().toUpperCase(), hudRightX0 + 52f, HUD_PANEL_Y_TOP - 20f);

			// HP row
			font.setColor(Color.WHITE);
			font.draw(batch, "HP", hudRightX0 + 12f, HUD_PANEL_Y_BOTTOM + 105f);
			font.draw(batch, adjacent.getHealth() + " / " + adjacent.getMaxHealth(),
					hudRightX0 + 206f, HUD_PANEL_Y_BOTTOM + 105f);

			// MOV row
			font.draw(batch, "MOV", hudRightX0 + 12f, HUD_PANEL_Y_BOTTOM + 81f);
			font.draw(batch, String.valueOf(adjacent.getSpeed()),
					hudRightX0 + 206f, HUD_PANEL_Y_BOTTOM + 81f);

			// Weapon summary
			Weapon mw = adjacent.getWeapon();
			if (mw != null) {
				int pct = Math.round(mw.getChanceToHit() * 100);
				String line = mw.getName() + "  \u00B7  " + pct + "% hit  \u00B7  " + mw.getDamage() + " dmg";
				font.setColor(MUTED);
				font.draw(batch, line, hudRightX0 + 12f, HUD_PANEL_Y_BOTTOM + 40f);
			}
		}

		batch.end();

		// Combat log strip (separate begin/end so we can share the batch cleanly)
		renderCombatLog();
	}

	private void drawCenteredText(String text, float centerX, float y) {
		glyphLayout.setText(font, text);
		font.draw(batch, text, centerX - glyphLayout.width / 2f, y);
	}

	private void renderCombatLog() {
		java.util.List<String> recent = gameBoard.getCombatLog().recent();
		if (recent.isEmpty()) {
			return;
		}

		float margin = 10f;
		float available = hudCamera.viewportWidth - margin * 2f;
		String sep = "  \u00B7  ";
		glyphLayout.setText(font, sep);
		float sepW = glyphLayout.width;

		// Compute widths, drop oldest if overflow
		java.util.List<String> entries = new java.util.ArrayList<>(recent);
		float[] widths = new float[entries.size()];
		float total = 0f;
		for (int i = 0; i < entries.size(); i++) {
			glyphLayout.setText(font, entries.get(i));
			widths[i] = glyphLayout.width;
			total += widths[i];
			if (i > 0) total += sepW;
		}
		while (total > available && entries.size() > 1) {
			total -= widths[0];
			if (entries.size() > 1) total -= sepW;
			entries.remove(0);
			float[] w2 = new float[entries.size()];
			System.arraycopy(widths, 1, w2, 0, entries.size());
			widths = w2;
		}

		batch.begin();
		float x = margin;
		float y = 16f;
		int last = entries.size() - 1;
		for (int i = 0; i < entries.size(); i++) {
			if (i == last) {
				font.setColor(LOG_HIGHLIGHT);
			} else {
				font.setColor(LOG_DIM);
			}
			font.draw(batch, entries.get(i), x, y);
			x += widths[i];
			if (i < last) {
				font.setColor(LOG_DIM);
				font.draw(batch, sep, x, y);
				x += sepW;
			}
		}
		batch.end();
	}

	public boolean handleHudClick(int screenX, int screenY) {
		if (gameBoard.isVictory()) {
			return false;
		}
		touchPoint.set(screenX, screenY, 0);
		hudViewport.unproject(touchPoint);
		if (touchPoint.y > HUD_PANEL_Y_TOP) {
			return false;
		}

		Hero hero = gameBoard.getHero();
		if (hero == null) {
			return true;
		}
		java.util.List<Weapon> inv = new java.util.ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = weaponCardBounds();
		for (int i = 0; i < inv.size() && i < cardRects.length; i++) {
			if (cardRects[i].contains(touchPoint.x, touchPoint.y)) {
				hero.setCurrentWeapon(inv.get(i));
				return true;
			}
		}
		if (gameBoard.isHeroTurn() && endTurnButtonBounds.contains(touchPoint.x, touchPoint.y)) {
			gameBoard.endHeroTurn();
			return true;
		}
		return true;
	}

	private void renderToasts() {
		if (activeToasts.isEmpty()) {
			return;
		}
		
		// Get the most recent toast (or we could show multiple, but for simplicity show just one)
		Toast currentToast = activeToasts.get(activeToasts.size() - 1);
		
		// Calculate position: bottom center, above HUD
		float toastY = TOAST_Y;
		float padding = 10;
		
		glyphLayout.setText(font, currentToast.getMessage());
		float textWidth = glyphLayout.width;
		float textHeight = glyphLayout.height;
		float boxWidth = textWidth + padding * 2;
		float boxHeight = textHeight + padding * 2;
		float boxX = (hudCamera.viewportWidth - boxWidth) / 2;
		float boxY = toastY;
		
		// Draw background (gray transparent)
		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		float alpha = currentToast.getAlpha();
		shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.8f * alpha);
		shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
		shapeRenderer.end();
		
		// Draw text
		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.setColor(1, 1, 1, alpha);
		font.draw(batch, currentToast.getMessage(), boxX + padding, boxY + padding + textHeight);
		batch.end();
	}

	private void updateHudPanelLayout() {
		float w = hudCamera.viewportWidth;
		hudLeftX1 = w / 3f;
		hudCenterX0 = hudLeftX1;
		hudCenterX1 = w * 2f / 3f;
		hudRightX0 = hudCenterX1;
		float endBtnW = 120f;
		float endBtnH = 34f;
		endTurnButtonBounds.set((hudCenterX0 + hudCenterX1 - endBtnW) / 2f, HUD_PANEL_Y_BOTTOM + 6f, endBtnW, endBtnH);

		float rowWidth = 3f * WEAPON_CARD_W + 2f * WEAPON_CARD_GAP;
		float rowStartX = (hudLeftX1 - rowWidth) / 2f;
		float cardY = HUD_PANEL_Y_BOTTOM + 10f;
		weaponCard1Bounds.set(rowStartX, cardY, WEAPON_CARD_W, WEAPON_CARD_H);
		weaponCard2Bounds.set(rowStartX + WEAPON_CARD_W + WEAPON_CARD_GAP, cardY, WEAPON_CARD_W, WEAPON_CARD_H);
		weaponCard3Bounds.set(rowStartX + 2f * (WEAPON_CARD_W + WEAPON_CARD_GAP), cardY, WEAPON_CARD_W, WEAPON_CARD_H);
	}

	private Rectangle[] weaponCardBounds() {
		return new Rectangle[] { weaponCard1Bounds, weaponCard2Bounds, weaponCard3Bounds };
	}

	private void renderPlayAreaHoverPopup() {
		if (!gameBoard.getHero().isAlive() || gameBoard.isVictory()) return;

		mouseHudCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		hudViewport.unproject(mouseHudCoords);
		if (mouseHudCoords.y <= HUD_PANEL_Y_TOP) return;

		mouseWorldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		worldViewport.unproject(mouseWorldCoords);
		int tileX = (int) Math.floor(mouseWorldCoords.x / GameBoard.SQUARE_SIZE);
		int tileY = (int) Math.floor(mouseWorldCoords.y / GameBoard.SQUARE_SIZE);

		Monster hovered = gameBoard.getMonsterAt(tileX, tileY);
		if (hovered != null) {
			renderHoverPopup(
					"Monster: " + hovered.getName() + "    HP: " + hovered.getHealth(),
					formatWeaponLine(hovered.getWeapon()));
			return;
		}

		WeaponPickup weaponPickup = gameBoard.getWeaponPickupAt(tileX, tileY);
		if (weaponPickup != null) {
			Weapon weapon = weaponPickup.getWeapon();
			renderHoverPopup(
					"Weapon: " + weapon.getName(),
					formatWeaponLine(weapon));
		}
	}

	private String formatWeaponLine(Weapon weapon) {
		if (weapon == null) {
			return null;
		}
		int hitPct = Math.round(weapon.getChanceToHit() * 100);
		return weapon.getName() + "    Hit: " + hitPct + "%    Dmg: " + weapon.getDamage();
	}

	private void renderHoverPopup(String line1, String line2) {
		mouseHudCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		hudViewport.unproject(mouseHudCoords);

		float padding = 8f;
		float gap = 4f;

		glyphLayout.setText(font, line1);
		float lineH = glyphLayout.height;
		float maxW = glyphLayout.width;
		if (line2 != null) {
			glyphLayout.setText(font, line2);
			maxW = Math.max(maxW, glyphLayout.width);
		}

		int numLines = (line2 != null) ? 2 : 1;
		float boxW = maxW + padding * 2;
		float boxH = padding * 2 + lineH * numLines + (numLines > 1 ? gap : 0);

		float boxX = mouseHudCoords.x - boxW / 2f;
		float boxY = mouseHudCoords.y + 15f;

		boxX = Math.max(0, Math.min(boxX, hudCamera.viewportWidth - boxW));
		if (boxY + boxH > hudCamera.viewportHeight) {
			boxY = mouseHudCoords.y - boxH - 15f;
		}
		boxY = Math.max(HUD_PANEL_Y_TOP + 8f, boxY);

		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.85f);
		shapeRenderer.rect(boxX, boxY, boxW, boxH);
		shapeRenderer.end();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.setColor(Color.WHITE);
		font.draw(batch, line1, boxX + padding, boxY + boxH - padding);
		if (line2 != null) {
			font.draw(batch, line2, boxX + padding, boxY + boxH - padding - lineH - gap);
		}
		batch.end();
	}

	private void renderVictoryDialog() {
		if (gameBoard == null || !gameBoard.isVictory()) {
			return;
		}
		renderEndRunDialog(true);
	}

	private void renderGameOverDialog() {
		Hero hero = gameBoard.getHero();
		if (hero == null || hero.isAlive() || gameBoard.isVictory()) {
			return;
		}
		renderEndRunDialog(false);
	}

	private void renderEndRunDialog(boolean victory) {
		final float panelWidth = 360f;
		final float panelHeight = 240f;
		final float panelX = (hudCamera.viewportWidth - panelWidth) / 2f;
		final float panelY = (hudCamera.viewportHeight - panelHeight) / 2f;

		final float buttonWidth = 180f;
		final float buttonHeight = 50f;
		final float buttonX = panelX + (panelWidth - buttonWidth) / 2f;
		final float buttonY = panelY + 35f;
		restartButtonBounds.set(buttonX, buttonY, buttonWidth, buttonHeight);

		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
		shapeRenderer.rect(0, 0, hudCamera.viewportWidth, hudCamera.viewportHeight);
		shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.95f);
		shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
		if (victory) {
			shapeRenderer.setColor(GOLD);
		} else {
			shapeRenderer.setColor(0.75f, 0.1f, 0.1f, 1f);
		}
		shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
		shapeRenderer.end();

		String title = victory ? "Victory!" : "Game Over";
		String subtitle = victory ? "The dungeon is fully explored." : "Your hero has fallen.";
		String stats = "Round " + gameBoard.getRound() + "  \u00B7  Monsters killed " + gameBoard.getMonstersKilled();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.setColor(victory ? GOLD : Color.WHITE);
		glyphLayout.setText(font, title);
		font.draw(batch, title, panelX + (panelWidth - glyphLayout.width) / 2f, panelY + panelHeight - 55f);

		font.setColor(Color.WHITE);
		glyphLayout.setText(font, subtitle);
		font.draw(batch, subtitle, panelX + (panelWidth - glyphLayout.width) / 2f, panelY + panelHeight - 90f);

		font.setColor(MUTED);
		glyphLayout.setText(font, stats);
		font.draw(batch, stats, panelX + (panelWidth - glyphLayout.width) / 2f, panelY + panelHeight - 120f);

		font.setColor(victory ? GOLD_PILL_TEXT : Color.WHITE);
		glyphLayout.setText(font, "Restart");
		font.draw(batch, "Restart", buttonX + (buttonWidth - glyphLayout.width) / 2f, buttonY + (buttonHeight + glyphLayout.height) / 2f);
		batch.end();
	}

	private void handleEndRunInput() {
		Hero hero = gameBoard.getHero();
		if (hero == null || !Gdx.input.justTouched()) {
			return;
		}
		if (hero.isAlive() && !gameBoard.isVictory()) {
			return;
		}

		touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		hudViewport.unproject(touchPoint);
		if (restartButtonBounds.contains(touchPoint.x, touchPoint.y)) {
			restartGame();
		}
	}

	private void restartGame() {
		gameBoard = new GameBoard();
		gameBoard.setToastNotifier(new StringCallback() {
			public void call(String value) {
				showToast(value);
			}
		});
		activeToasts.clear();
		inputAdapter = new MyInputAdapter(gameBoard);
		inputAdapter.setViewport(worldViewport);
		inputAdapter.setHudClickHandler(new MyInputAdapter.HudClickHandler() {
			public boolean handleHudTouch(int sx, int sy) {
				return MyGdxGame.this.handleHudClick(sx, sy);
			}
		});
		Gdx.input.setInputProcessor(inputAdapter);
	}
}
