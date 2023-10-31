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
	Sound music;
	
	@Override
	public void create () {	// this is done once
		gameBoard = new GameBoard();
		batch = new SpriteBatch();
		tile = new Texture("tile-small.jpeg");
		//tile = new Texture("tile.jpeg");
		Gdx.input.setInputProcessor(new MyInputAdapter(gameBoard));
		music = Gdx.audio.newSound(Gdx.files.internal("music.mp3"));
		music.play(0.5f);
	}

	@Override
	public void render () {	// this is loop rendered 24 FPS
		//System.out.println("Rendering");
		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
		batch.begin();

		//draw tiles with repeat
		TextureRegion textureRegion = new TextureRegion(tile);
		TiledDrawable tiledDrawable = new TiledDrawable(textureRegion);
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		tiledDrawable.draw(batch, 0, 0, width, height);

		//draw each texture from the board
		for (int i = 0; i < GameBoard.BOARD_SQUARE_LENGTH; i++) {
			for (int j = 0; j < GameBoard.BOARD_SQUARE_LENGTH; j++) {
				Texture tex = gameBoard.getTexture(i,j);
				if (tex != null) {
					batch.draw(tex,
							i * GameBoard.SQUARE_SIZE,
							j * GameBoard.SQUARE_SIZE,
							GameBoard.SQUARE_SIZE,
							GameBoard.SQUARE_SIZE);
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
