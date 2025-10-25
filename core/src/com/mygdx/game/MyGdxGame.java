package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
	
	@Override
	public void create () {	// this is done once
		gameBoard = new GameBoard();
		batch = new SpriteBatch();

		tile = new Texture("tile-small.jpeg");
		textureRegion = new TextureRegion(tile);
		tiledDrawable = new TiledDrawable(textureRegion);
		
		Gdx.input.setInputProcessor(new MyInputAdapter(gameBoard));

		music = Gdx.audio.newSound(Gdx.files.internal("music.mp3"));
		music.play(0.1f);
	}

	@Override
	public void render () { // this is loop rendered 60 FPS
		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
		batch.begin();

		//draw tiles with repeat
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		tiledDrawable.draw(batch, 0, 0, width, height);

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
	
	@Override
	public void dispose () {
		batch.dispose();
		tile.dispose();
		gameBoard = null;
	}
}
