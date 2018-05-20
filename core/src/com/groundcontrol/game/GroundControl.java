package com.groundcontrol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.groundcontrol.game.controller.GameController;
import com.groundcontrol.game.model.GameModel;
import com.groundcontrol.game.view.GameView;
import com.groundcontrol.game.view.MenuScreen;
import com.groundcontrol.game.view.SecondPlayerScreen;

public class GroundControl extends Game {
	private SpriteBatch batch;
	private AssetManager assetManager;


	@Override
	public void create () {
		batch = new SpriteBatch();
		assetManager = new AssetManager();

		startGame();
	}

	private void startGame(){

        GameModel gameModel = new GameModel();

		GameController gameController = new GameController(gameModel);


		setScreen(new MenuScreen(this));

	}

	public void startNewGame(){
		GameModel gameModel = new GameModel();

		GameController gameController = new GameController(gameModel);

		setScreen(new GameView(this, gameModel, gameController));
	}

	@Override
	public void dispose () {
		batch.dispose();
		assetManager.dispose();
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void startMP() {
		setScreen(new SecondPlayerScreen(this));
	}

    public void startMainMenu() {
		setScreen(new MenuScreen(this));
    }
}
