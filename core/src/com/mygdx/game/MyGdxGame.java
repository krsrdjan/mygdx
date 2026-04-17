package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
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
	BitmapFont font;
	ShapeRenderer shapeRenderer;
	MyInputAdapter inputAdapter;
	private List<Toast> activeToasts = new ArrayList<>();
	private final GlyphLayout glyphLayout = new GlyphLayout();
	private final Rectangle restartButtonBounds = new Rectangle();
	private final Vector3 touchPoint = new Vector3();
	private final Vector3 mouseWorldCoords = new Vector3();
	private final Vector3 mouseHudCoords = new Vector3();

	// HUD layout (hudCamera 800x600 screen space)
	private static final float LOG_STRIP_H = 22f;
	private static final float HUD_PANEL_Y_BOTTOM = LOG_STRIP_H;
	private static final float HUD_PANEL_H = 160f;
	private static final float HUD_PANEL_Y_TOP = HUD_PANEL_Y_BOTTOM + HUD_PANEL_H;
	private static final float LEFT_X0 = 0f, LEFT_X1 = 260f;
	private static final float CENTER_X0 = 260f, CENTER_X1 = 540f;
	private static final float RIGHT_X0 = 540f, RIGHT_X1 = 800f;
	private static final float TOAST_Y = 195f;

	// HUD colors
	private static final Color HUD_BG = new Color(0.08f, 0.06f, 0.05f, 0.95f);
	private static final Color LOG_BG = new Color(0.05f, 0.04f, 0.03f, 0.95f);
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
	
	@Override
	public void create () {	// this is done once
		gameBoard = new GameBoard();
		batch = new SpriteBatch();

		tile = new Texture("tile-small.jpeg");
		textureRegion = new TextureRegion(tile);
		tiledDrawable = new TiledDrawable(textureRegion);
		
		// Setup camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 600); // Show 12-13 squares horizontally, 9-10 vertically
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, 800, 600);

		font = new BitmapFont();
		shapeRenderer = new ShapeRenderer();
		
		weaponCard1Bounds.set(LEFT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 26f, 114f, 44f);
		weaponCard2Bounds.set(LEFT_X0 + 134f, HUD_PANEL_Y_BOTTOM + 26f, 114f, 44f);

		inputAdapter = new MyInputAdapter(gameBoard);
		inputAdapter.setCamera(camera);
		inputAdapter.setHudClickHandler(new MyInputAdapter.HudClickHandler() {
			public boolean handleHudTouch(int sx, int sy) {
				return MyGdxGame.this.handleHudClick(sx, sy);
			}
		});
		Gdx.input.setInputProcessor(inputAdapter);

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
		
		// Update camera to follow hero
		updateCamera();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
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
				
				// Draw creature on this square (hero, monsters, etc.)
				Square square = gameBoard.getSquare(i, j);
				if (square != null && square.getCreature() != null) {
					Texture creatureTex = square.getCreature().getTexture();
					if (creatureTex != null) {
						batch.draw(creatureTex,
								i * GameBoard.SQUARE_SIZE,
								j * GameBoard.SQUARE_SIZE,
								GameBoard.SQUARE_SIZE,
								GameBoard.SQUARE_SIZE);
					}
				}
			}
		}

		batch.end();

		// HUD rendering (screen space)
		hudCamera.update();
		renderHud();

		// Render monster hover popup
		renderMonsterHoverPopup();

		// Render toasts above HUD
		renderToasts();
		renderGameOverDialog();
		handleGameOverInput();
	}
	
	private void updateCamera() {
		// Get hero position
		Position heroPos = gameBoard.getHero().getPosition();
		
		// Calculate hero's world position (center of the square)
		float heroWorldX = heroPos.x * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;
		float heroWorldY = heroPos.y * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;
		
		// Calculate world bounds
		float worldWidth = GameBoard.BOARD_SQUARE_WIDTH * GameBoard.SQUARE_SIZE;
		float worldHeight = GameBoard.BOARD_SQUARE_HEIGHT * GameBoard.SQUARE_SIZE;
		float halfViewportWidth = camera.viewportWidth / 2f;
		float halfViewportHeight = camera.viewportHeight / 2f;
		
		// Clamp camera position to world bounds
		float clampedX = Math.max(halfViewportWidth, Math.min(heroWorldX, worldWidth - halfViewportWidth));
		float clampedY = Math.max(halfViewportHeight, Math.min(heroWorldY, worldHeight - halfViewportHeight));
		
		// Set camera position with bounds
		camera.position.set(clampedX, clampedY, 0);
	}
	
	@Override
	public void resize(int width, int height) {
		// Update camera viewport when window is resized
		camera.viewportWidth = 800;
		camera.viewportHeight = 600;
		camera.update();
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
		float hpBarX = LEFT_X0 + 40f;
		float hpBarY = HUD_PANEL_Y_BOTTOM + 110f;
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
		float movBarY = HUD_PANEL_Y_BOTTOM + 88f;
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
		Rectangle[] cardRects = new Rectangle[] { weaponCard1Bounds, weaponCard2Bounds };
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
		float pillX = CENTER_X0 + (CENTER_X1 - CENTER_X0 - pillW) / 2f;
		float pillY = HUD_PANEL_Y_TOP - 28f;
		if (gameBoard.isHeroTurn()) {
			shapeRenderer.setColor(GOLD);
		} else {
			shapeRenderer.setColor(DIM_TRACK);
		}
		shapeRenderer.rect(pillX, pillY, pillW, pillH);

		// Enemy panel content
		if (adjacent != null) {
			int enemyMaxHp = Math.max(1, adjacent.getMaxHealth());
			float eBarX = RIGHT_X0 + 12f;
			float eBarY = HUD_PANEL_Y_BOTTOM + 78f;
			float eBarW = 200f;
			float eBarH = 10f;
			shapeRenderer.setColor(DIM_TRACK);
			shapeRenderer.rect(eBarX, eBarY, eBarW, eBarH);
			float eFill = Math.max(0f, Math.min(1f, adjacent.getHealth() / (float) enemyMaxHp));
			shapeRenderer.setColor(HP_RED);
			shapeRenderer.rect(eBarX, eBarY, eBarW * eFill, eBarH);

			// Pip row
			float pipSize = 14f;
			float pipGap = 4f;
			float pipsX = RIGHT_X0 + 12f;
			float pipsY = HUD_PANEL_Y_BOTTOM + 52f;
			int currentHp = Math.max(0, adjacent.getHealth());
			for (int i = 0; i < enemyMaxHp; i++) {
				float px = pipsX + i * (pipSize + pipGap);
				if (i < currentHp) {
					shapeRenderer.setColor(HP_RED);
				} else {
					shapeRenderer.setColor(DIM_TRACK);
				}
				shapeRenderer.rect(px, pipsY, pipSize, pipSize);
			}
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
		shapeRenderer.line(LEFT_X1, HUD_PANEL_Y_BOTTOM, LEFT_X1, HUD_PANEL_Y_TOP);
		shapeRenderer.line(CENTER_X1, HUD_PANEL_Y_BOTTOM, CENTER_X1, HUD_PANEL_Y_TOP);

		// Weapon card borders
		Weapon activeWeapon = hero.getCurrentWeapon();
		List<Weapon> inv = new ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = new Rectangle[] { weaponCard1Bounds, weaponCard2Bounds };
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

		// Enemy pip outlines
		if (adjacent != null) {
			int enemyMaxHp = Math.max(1, adjacent.getMaxHealth());
			float pipSize = 14f;
			float pipGap = 4f;
			float pipsX = RIGHT_X0 + 12f;
			float pipsY = HUD_PANEL_Y_BOTTOM + 52f;
			shapeRenderer.setColor(BORDER);
			for (int i = 0; i < enemyMaxHp; i++) {
				float px = pipsX + i * (pipSize + pipGap);
				shapeRenderer.rect(px, pipsY, pipSize, pipSize);
			}
		}

		shapeRenderer.end();
	}

	private void renderHudText(Hero hero, Monster adjacent) {
		batch.begin();

		// HERO label
		font.setColor(GOLD);
		font.draw(batch, "HERO", LEFT_X0 + 12f, HUD_PANEL_Y_TOP - 12f);

		// HP
		font.setColor(Color.WHITE);
		font.draw(batch, "HP", LEFT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 119f);
		String hpText = hero.getHealth() + " / " + hero.getMaxHealth();
		font.draw(batch, hpText, LEFT_X0 + 210f, HUD_PANEL_Y_BOTTOM + 119f);

		// MOV
		font.draw(batch, "MOV", LEFT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 97f);
		font.draw(batch, String.valueOf(hero.getSpeed()), LEFT_X0 + 210f, HUD_PANEL_Y_BOTTOM + 97f);

		// Weapon cards
		Weapon activeWeapon = hero.getCurrentWeapon();
		List<Weapon> inv = new ArrayList<>(hero.getInventory());
		Rectangle[] cardRects = new Rectangle[] { weaponCard1Bounds, weaponCard2Bounds };
		for (int i = 0; i < cardRects.length; i++) {
			if (i >= inv.size()) break;
			Weapon w = inv.get(i);
			Rectangle r = cardRects[i];
			if (w == activeWeapon) {
				font.setColor(Color.WHITE);
			} else {
				font.setColor(MUTED);
			}
			font.draw(batch, w.getName(), r.x + 8f, r.y + r.height - 8f);
			int hitPct = Math.round(w.getChanceToHit() * 100);
			font.setColor(MUTED);
			font.draw(batch, hitPct + "%  " + w.getDamage() + " dmg", r.x + 8f, r.y + 16f);
		}

		// Center panel: pill + controls
		float pillW = 110f;
		float pillH = 22f;
		float pillX = CENTER_X0 + (CENTER_X1 - CENTER_X0 - pillW) / 2f;
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

		// Control hints (two columns)
		font.setColor(MUTED);
		float centerMid = (CENTER_X0 + CENTER_X1) / 2f;
		float hintY1 = HUD_PANEL_Y_BOTTOM + 92f;
		float hintY2 = HUD_PANEL_Y_BOTTOM + 62f;
		drawCenteredText("WASD", (CENTER_X0 + centerMid) / 2f, hintY1);
		drawCenteredText("U / click", (centerMid + CENTER_X1) / 2f, hintY1);
		drawCenteredText("SPACE  end turn", (CENTER_X0 + centerMid) / 2f, hintY2);
		drawCenteredText("[I]  switch", (centerMid + CENTER_X1) / 2f, hintY2);

		// Enemy panel
		if (adjacent != null) {
			font.setColor(MUTED);
			font.draw(batch, "NEARBY ENEMY", RIGHT_X0 + 12f, HUD_PANEL_Y_TOP - 12f);

			font.setColor(GOLD);
			font.draw(batch, adjacent.getName().toUpperCase(), RIGHT_X0 + 12f, HUD_PANEL_Y_TOP - 32f);

			font.setColor(MUTED);
			font.draw(batch, "Melee fighter", RIGHT_X0 + 12f, HUD_PANEL_Y_TOP - 50f);

			// Portrait icon (top-right of panel)
			Texture portrait = adjacent.getTexture();
			if (portrait != null) {
				batch.setColor(Color.WHITE);
				batch.draw(portrait, RIGHT_X1 - 44f, HUD_PANEL_Y_TOP - 44f, 32f, 32f);
			}

			// HP text above bar
			font.setColor(Color.WHITE);
			font.draw(batch, adjacent.getHealth() + " / " + adjacent.getMaxHealth() + " HP",
					RIGHT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 102f);

			// Weapon summary
			Weapon mw = adjacent.getWeapon();
			if (mw != null) {
				int pct = Math.round(mw.getChanceToHit() * 100);
				String line = mw.getName() + "  \u00B7  " + pct + "% hit  \u00B7  " + mw.getDamage() + " dmg";
				font.setColor(MUTED);
				font.draw(batch, line, RIGHT_X0 + 12f, HUD_PANEL_Y_BOTTOM + 30f);
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
		touchPoint.set(screenX, screenY, 0);
		hudCamera.unproject(touchPoint);
		Hero hero = gameBoard.getHero();
		if (hero == null) return false;
		java.util.List<Weapon> inv = new java.util.ArrayList<>(hero.getInventory());
		if (inv.size() >= 1 && weaponCard1Bounds.contains(touchPoint.x, touchPoint.y)) {
			hero.setCurrentWeapon(inv.get(0));
			return true;
		}
		if (inv.size() >= 2 && weaponCard2Bounds.contains(touchPoint.x, touchPoint.y)) {
			hero.setCurrentWeapon(inv.get(1));
			return true;
		}
		return false;
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

	private void renderMonsterHoverPopup() {
		if (!gameBoard.getHero().isAlive()) return;

		mouseWorldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(mouseWorldCoords);
		int tileX = (int) Math.floor(mouseWorldCoords.x / GameBoard.SQUARE_SIZE);
		int tileY = (int) Math.floor(mouseWorldCoords.y / GameBoard.SQUARE_SIZE);

		Monster hovered = gameBoard.getMonsterAt(tileX, tileY);
		if (hovered == null) return;

		mouseHudCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		hudCamera.unproject(mouseHudCoords);

		String line1 = "Monster: " + hovered.getName() + "    HP: " + hovered.getHealth();
		String line2 = null;
		Weapon monsterWeapon = hovered.getWeapon();
		if (monsterWeapon != null) {
			int hitPct = Math.round(monsterWeapon.getChanceToHit() * 100);
			line2 = monsterWeapon.getName() + "    Hit: " + hitPct + "%    Dmg: " + monsterWeapon.getDamage();
		}

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
		shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
		shapeRenderer.rect(boxX, boxY, boxW, boxH);
		shapeRenderer.end();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.setColor(Color.WHITE);
		float textX = boxX + padding;
		float textY = boxY + boxH - padding;
		font.draw(batch, line1, textX, textY);
		if (line2 != null) {
			font.draw(batch, line2, textX, textY - lineH - gap);
		}
		batch.end();
	}

	private void renderGameOverDialog() {
		Hero hero = gameBoard.getHero();
		if (hero == null || hero.isAlive()) {
			return;
		}

		final float panelWidth = 360f;
		final float panelHeight = 220f;
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
		shapeRenderer.setColor(0.75f, 0.1f, 0.1f, 1f);
		shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
		shapeRenderer.end();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.setColor(Color.WHITE);
		glyphLayout.setText(font, "Game Over");
		font.draw(batch, "Game Over", panelX + (panelWidth - glyphLayout.width) / 2f, panelY + panelHeight - 55f);

		glyphLayout.setText(font, "Your hero has fallen.");
		font.draw(batch, "Your hero has fallen.", panelX + (panelWidth - glyphLayout.width) / 2f, panelY + panelHeight - 95f);

		glyphLayout.setText(font, "Restart");
		font.draw(batch, "Restart", buttonX + (buttonWidth - glyphLayout.width) / 2f, buttonY + (buttonHeight + glyphLayout.height) / 2f);
		batch.end();
	}

	private void handleGameOverInput() {
		Hero hero = gameBoard.getHero();
		if (hero == null || hero.isAlive() || !Gdx.input.justTouched()) {
			return;
		}

		touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		hudCamera.unproject(touchPoint);
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
		inputAdapter.setCamera(camera);
		inputAdapter.setHudClickHandler(new MyInputAdapter.HudClickHandler() {
			public boolean handleHudTouch(int sx, int sy) {
				return MyGdxGame.this.handleHudClick(sx, sy);
			}
		});
		Gdx.input.setInputProcessor(inputAdapter);
	}
}
