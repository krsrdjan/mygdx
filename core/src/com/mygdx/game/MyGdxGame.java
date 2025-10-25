package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
	GameBoard gameBoard;
	SpriteBatch batch;
	Texture tile;
	TiledDrawable tiledDrawable;
	TextureRegion textureRegion;
	Sound music;
	OrthographicCamera camera;
	
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
		
		Gdx.input.setInputProcessor(new MyInputAdapter(gameBoard));

		music = Gdx.audio.newSound(Gdx.files.internal("music.mp3"));
		music.play(0.1f);
	}

	@Override
	public void render () { // this is loop rendered 60 FPS
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
	}
	
	private void updateCamera() {
		// Get hero position
		Position heroPos = gameBoard.getHero().getPosition();
		
		// Calculate hero's world position (center of the square)
		float heroWorldX = heroPos.x * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;
		float heroWorldY = heroPos.y * GameBoard.SQUARE_SIZE + GameBoard.SQUARE_SIZE / 2f;
		
		// Set camera position to follow hero
		camera.position.set(heroWorldX, heroWorldY, 0);
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
		gameBoard = null;
	}
}
