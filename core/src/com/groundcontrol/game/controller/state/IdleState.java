package com.groundcontrol.game.controller.state;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.groundcontrol.game.controller.elements.PlayerController;

/**
 * Represents the state in which the player is just stading without moving in a planet
 */
public class IdleState implements PlayerState {

    private float timeInPlanet;

    @Override
    public float getTime(){
        return this.timeInPlanet;
    }

    @Override
    public void updateTime(PlayerController context, float delta){
        this.timeInPlanet -= delta;

        if(this.timeInPlanet < 0){
            context.setLost(true);
        }

    }

    /**
     * Class constructor with given time
     * @param time
     */
    public IdleState(float time){
        this.timeInPlanet = time;
    }

    @Override
    public void handleInput(PlayerController context, InputDecoder.Input input) {

        if (input == InputDecoder.Input.JUMP) {
            context.jump();
        } else if (input == InputDecoder.Input.RIGHT || input == InputDecoder.Input.LEFT) {
            context.setState(new RunningState(this.timeInPlanet));
        }

    }

    @Override
    public void setRotation(PlayerController context, Array<Body> objects) {

        context.setTransform(context.getX(), context.getY(), context.getAngleBetween(context.getPlanet()));

    }

    @Override
    public void applyPullForce(PlayerController context, Array<Body> objects) {
        Vector2 force = context.calculatePullForce(context.getPlanet());
        context.applyForceToCenter(force, true);
    }
}
