package com.groundcontrol.game.controller.elements;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.groundcontrol.game.controller.GameController;
import com.groundcontrol.game.model.elements.ElementModel;

import static com.groundcontrol.game.view.GameView.PIXEL_TO_METER;

public abstract class ElementController {

    //Collision Handling
    final static short PLANET_BODY = 0x0001;
    final static short PLAYER_BODY = 0x0002;
    final static short COMET_BODY = 0x0003;

    protected Body body;

    protected int width;
    protected int height;

    protected int width_meters;
    protected int height_meters;


    ElementController(World world, ElementModel model, BodyDef.BodyType bodyType) {

        BodyDef bodydef = new BodyDef();

        bodydef.type = bodyType;

        bodydef.position.set(model.getX(), model.getY());

        bodydef.angle = model.getRotation();

        body = world.createBody(bodydef);

        body.setUserData(model);
    }


    final private void fixVertexes(float[] vertexes) {

        for (int i = 0; i < vertexes.length; i++) {
            if (i % 2 == 0)
                vertexes[i] -= width / 2;
            if (i % 2 != 0)
                vertexes[i] -= height / 2;

            if (i % 2 != 0)
                vertexes[i] *= -1;

            vertexes[i] *= PIXEL_TO_METER;
        }


    }

    final void createFixture(Body body, FixtureInfo info) {

        this.height_meters = (int) ((float) this.height * PIXEL_TO_METER);

        this.width_meters = (int) ((float) this.width * PIXEL_TO_METER);

        this.fixVertexes(info.vertexes);

        PolygonShape polygon = new PolygonShape();
        polygon.set(info.vertexes);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygon;

        fixtureDef.density = info.density;

        fixtureDef.friction = info.friction;
        fixtureDef.restitution = info.restitution;
        fixtureDef.filter.categoryBits = info.category;
        fixtureDef.filter.maskBits = info.mask;

        body.createFixture(fixtureDef);

        polygon.dispose();

    }


    final void createFixture(Body body, float[] vertexes, int width, int height, float density, float friction, float restitution, short category, short mask) {

        this.height_meters = (int) ((float) this.height * PIXEL_TO_METER);

        this.width_meters = (int) ((float) this.width * PIXEL_TO_METER);

        for (int i = 0; i < vertexes.length; i++) {
            if (i % 2 == 0)
                vertexes[i] -= width / 2;
            if (i % 2 != 0)
                vertexes[i] -= height / 2;

            if (i % 2 != 0)
                vertexes[i] *= -1;

            vertexes[i] *= PIXEL_TO_METER;
        }

        PolygonShape polygon = new PolygonShape();
        polygon.set(vertexes);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygon;

        fixtureDef.density = density;

        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        fixtureDef.filter.categoryBits = category;
        fixtureDef.filter.maskBits = mask;

        body.createFixture(fixtureDef);

        polygon.dispose();

    }


    public Body getBody() {
        return this.body;
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public void setTransform(float x, float y, float angle) {
        body.setTransform(x, y, angle);
    }

    public void applyForceToCenter(Vector2 v, boolean awake) {
        body.applyForceToCenter(v, awake);
    }

    public void applyLinearImpulseToCenter(Vector2 v, boolean awake) {
        body.applyLinearImpulse(v, body.getLocalCenter(), awake);
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public float getMass() {
        return body.getMass();
    }

    public float getAngleBetween(Body body) {
        float rot = (float) Math.atan2(body.getPosition().y - this.getY(), body.getPosition().x - this.getX());

        rot += Math.PI / 2.0;

        return rot;
    }

    public Vector2 calculatePullForce(Body body) {

        double distanceSquared = body.getPosition().dst2(this.getPosition());

        double planet_mass = body.getMass();

        double player_mass = this.getMass();

        double force_module = GameController.G * (planet_mass * player_mass) / distanceSquared;

        Vector2 force = body.getPosition().sub(this.getPosition()).nor();

        force.setLength((float) force_module);

        return force;
    }

    public abstract float getMaxVelocity();

    public void limitVelocity() {

        float x = this.body.getLinearVelocity().x;
        float y = this.body.getLinearVelocity().y;

        if (Math.abs(x) > getMaxVelocity())
            x = getMaxVelocity() * Math.signum(x);

        if (Math.abs(y) > getMaxVelocity())
            y = getMaxVelocity() * Math.signum(y);

        this.body.setLinearVelocity(x, y);
    }

    public abstract float getMaxAngular();

    public void limitAngularVelocity() {

        float omega = this.body.getAngularVelocity();

        if (Math.abs(omega) > getMaxVelocity())
            omega = getMaxAngular() * Math.signum(omega);

        this.body.setAngularVelocity(omega);
    }


}
