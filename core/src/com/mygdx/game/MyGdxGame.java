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
	private final Color hudBgColor = new Color(0, 0, 0, 0.6f);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	private final Rectangle restartButtonBounds = new Rectangle();
	private final Vector3 touchPoint = new Vector3();
	
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
		
		inputAdapter = new MyInputAdapter(gameBoard);
		inputAdapter.setCamera(camera);
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
		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(hudBgColor);
		shapeRenderer.rect(0, 0, hudCamera.viewportWidth, 60);
		shapeRenderer.end();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		Hero hero = gameBoard.getHero();
		float hudPadding = 10f;
		String hudText = "HP: " + hero.getHealth() + "    Moves: " + hero.getSpeed();
		font.setColor(Color.WHITE);
		font.draw(batch, hudText, hudPadding, 45);

		// Show weapon info
		Weapon weapon = hero.getCurrentWeapon();
		if (weapon != null) {
			int hitChancePercent = Math.round(weapon.getChanceToHit() * 100);
			String weaponHud = weapon.getName() + "    Hit: " + hitChancePercent + "%    Dmg: " + weapon.getDamage();
			font.draw(batch, weaponHud, hudPadding, 25);
		}

		// Basic controls in the middle of the HUD
		String controlsTop = "Move: " + inputAdapter.getMoveKeysLabel() + "/Click"
				+ "    Attack: " + inputAdapter.getAttackKeyLabel() + "/Click monster";
		glyphLayout.setText(font, controlsTop);
		float controlsTopX = (hudCamera.viewportWidth - glyphLayout.width) / 2f;
		font.draw(batch, controlsTop, controlsTopX, 45);
		String controlsBottom = "Switch weapon: " + inputAdapter.getSwitchWeaponKeyLabel()
				+ "    End turn: " + inputAdapter.getEndTurnKeyLabel();
		glyphLayout.setText(font, controlsBottom);
		float controlsBottomX = (hudCamera.viewportWidth - glyphLayout.width) / 2f;
		font.draw(batch, controlsBottom, controlsBottomX, 25);

		// Show adjacent monster info (name and HP) if any
		Monster adjacent = gameBoard.getAdjacentMonsterToHero();
		if (adjacent != null) {
			String monsterHud = "Monster: " + adjacent.getName() + "    HP: " + adjacent.getHealth();
			glyphLayout.setText(font, monsterHud);
			float monsterHudX = hudCamera.viewportWidth - glyphLayout.width - hudPadding;
			font.draw(batch, monsterHud, monsterHudX, 45);
			
			// Show monster weapon info
			Weapon monsterWeapon = adjacent.getWeapon();
			if (monsterWeapon != null) {
				int hitChancePercent = Math.round(monsterWeapon.getChanceToHit() * 100);
				String monsterWeaponHud = monsterWeapon.getName() + "    Hit: " + hitChancePercent + "%    Dmg: " + monsterWeapon.getDamage();
				glyphLayout.setText(font, monsterWeaponHud);
				float monsterWeaponHudX = hudCamera.viewportWidth - glyphLayout.width - hudPadding;
				font.draw(batch, monsterWeaponHud, monsterWeaponHudX, 25);
			}
		}
		
		batch.end();
		
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
	
	private void renderToasts() {
		if (activeToasts.isEmpty()) {
			return;
		}
		
		// Get the most recent toast (or we could show multiple, but for simplicity show just one)
		Toast currentToast = activeToasts.get(activeToasts.size() - 1);
		
		// Calculate position: bottom center, above HUD (which is 60px high)
		float toastY = 80; // Above HUD
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
		Gdx.input.setInputProcessor(inputAdapter);
	}
}
