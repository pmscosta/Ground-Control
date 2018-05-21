package com.groundcontrol.game.view.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.groundcontrol.game.GroundControl;
import com.groundcontrol.game.model.elements.ElementModel;
import com.groundcontrol.game.model.elements.PlayerModel;

public class PlayerView extends ElementView {

    private static final int numberOfRunningStates = 6;

    private static final int numberOfIdleStates = 3;

    private static final float FRAME_TIME = 0.1f;

    private boolean stop = false;

    private float stateTime = 0;

    private boolean flip = true;

    private Animation<TextureRegion> runningAnimation;

    private Animation<TextureRegion> idleAnimation;

    private Animation<TextureRegion> currentAnimation;

    public PlayerView(GroundControl game) {
        super(game);
    }

    private Animation<TextureRegion> createRunningAnimation(GroundControl game) {

        Texture runningTexture = game.getAssetManager().get("runningSheet.png");
        TextureRegion[][] runRegion = TextureRegion.split(runningTexture, runningTexture.getWidth() / numberOfRunningStates, runningTexture.getHeight());

        TextureRegion[] frames = new TextureRegion[numberOfRunningStates];

        System.arraycopy(runRegion[0], 0, frames, 0, numberOfRunningStates);

        return new Animation<TextureRegion>(FRAME_TIME, frames);

    }

    private Animation<TextureRegion> createIdleAnimation(GroundControl game) {

        Texture idleTexture = game.getAssetManager().get("idleSheet.png");
        TextureRegion[][] idleRegion = TextureRegion.split(idleTexture, idleTexture.getWidth() / numberOfIdleStates, idleTexture.getHeight());

        TextureRegion[] frames = new TextureRegion[numberOfIdleStates];

        System.arraycopy(idleRegion[0], 0, frames, 0, numberOfIdleStates);

        return new Animation<TextureRegion>(FRAME_TIME, frames);

    }

    public Sprite createSprite(GroundControl game) {
        Texture texture = game.getAssetManager().get("player.png");
        this.runningAnimation = createRunningAnimation(game);
        this.idleAnimation = createIdleAnimation(game);
        return new Sprite(texture, texture.getWidth(), texture.getHeight());
    }

    private void updateCurrentAnimation(ElementModel model) {

        switch (((PlayerModel) model).getCurrentState()) {

            case IDLE:
                this.currentAnimation = idleAnimation;
                break;
            case RUNNING:
                this.currentAnimation = runningAnimation;
                break;
            default:
                this.currentAnimation = idleAnimation;
                break;

        }


    }

    @Override
    public void update(ElementModel model) {
        super.update(model);

        flip = ((PlayerModel) model).isRightSide();

        this.updateCurrentAnimation(model);
    }

    public void setStopped() {
        this.stop = true;
    }

    public void removeStopped() {
        this.stop = false;
    }

    @Override
    public void draw(SpriteBatch batch) {

        if (!stop)
            this.stateTime += Gdx.graphics.getDeltaTime();


        sprite.setRegion(currentAnimation.getKeyFrame(stateTime, true));

        if (!flip)
            sprite.flip(true, false);

        sprite.draw(batch);

    }
}
