package com.groundcontrol.game.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.groundcontrol.game.controller.elements.BigPlanetController;
import com.groundcontrol.game.controller.elements.CometController;
import com.groundcontrol.game.controller.elements.PlanetController;
import com.groundcontrol.game.controller.elements.PlayerController;
import com.groundcontrol.game.controller.gameflow.ForceController;
import com.groundcontrol.game.controller.gameflow.ScoreController;
import com.groundcontrol.game.controller.state.InputDecoder;
import com.groundcontrol.game.model.GameModel;
import com.groundcontrol.game.model.elements.CometModel;
import com.groundcontrol.game.model.elements.ElementModel;
import com.groundcontrol.game.model.elements.PlanetModel;
import com.groundcontrol.game.model.elements.PlayerModel;
import com.groundcontrol.game.view.ScreenModules.GameSection;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.math.MathUtils.random;

public class GameController implements ContactListener {

    public static final int ARENA_WIDTH = 50;

    public static final int ARENA_HEIGHT = 100;

    public static final double G = Math.pow(3.667, -2);

    private static final float TIME_BETWEEN_COMETS = 3f;

    protected final World world;

    protected final PlayerController playerController;

    protected float accumulator;

    protected GameModel gameModel;

    protected ScoreController scoreController;

    protected ForceController forceController;

    protected float timeToNextComet;

    private InputDecoder decoder;

    private List<PlanetModel> planetsToAdd = new ArrayList<PlanetModel>();

    public GameController(GameModel gameModel) {
        world = new World(new Vector2(0, 0), true);

        this.gameModel = gameModel;

        List<PlanetModel> planets = this.gameModel.getPlanets();

        playerController = new PlayerController(world, this.gameModel.getPlayer());

        for (PlanetModel p : planets) {

            if (p.getSize() == PlanetModel.PlanetSize.MEDIUM)
                new PlanetController(world, p);
            else
                new BigPlanetController(world, p);
        }

        this.decoder = new InputDecoder();

        this.scoreController = new ScoreController();

        this.forceController = new ForceController();

        this.timeToNextComet = TIME_BETWEEN_COMETS;

        world.setContactListener(this);
    }

    public void setPlanetForce(float delta, float x, float y) {

        this.forceController.updateForce(delta, x, y);
    }


    private void applyGravityToPlanets(Array<Body> bodies) {

        for (Body body : bodies) {

            if (!(body.getUserData() instanceof PlayerModel || body.getUserData() instanceof CometModel)) {

                float random = (float) (0.7 + Math.random() * (1.4 - 0.7));

                Vector2 randomForce = new Vector2();
                randomForce.x = forceController.getForce().x * random;
                randomForce.y = forceController.getForce().y * random;

                body.setLinearVelocity(randomForce);


            }

        }
    }

    public void handleInput(GameSection.StateInput input) {

        this.playerController.handleInput(this.decoder.convertViewInput(input));

    }


    public void update(float delta) {

        this.gameModel.update(delta);

        this.scoreController.update(delta);

        Array<Body> bodies = new Array<Body>();

        world.getBodies(bodies);

        applyGravityToPlanets(bodies);

        playerController.update(bodies, delta);

        float frameTime = Math.min(delta, 0.25f);

        accumulator += frameTime;

        while (accumulator >= 1 / 60f) {
            world.step(1 / 60f, 6, 2);
            accumulator -= 1 / 60f;

        }

        this.updateModelInfo();

        bodies.clear();
    }

    private void updateModelInfo() {

        Array<Body> bodies = new Array<Body>();

        world.getBodies(bodies);

        for (Body body : bodies) {
            verifyBounds(body);
            ((ElementModel) body.getUserData()).setX(body.getPosition().x);
            ((ElementModel) body.getUserData()).setY(body.getPosition().y);
            ((ElementModel) body.getUserData()).setRotation(body.getAngle());
        }


        ((PlayerModel) playerController.getBody().getUserData()).setRightSide(playerController.isRightSide());

        //this.gameModel.setScore(scoreController.getScore());

        this.gameModel.setTimeLeft(playerController.getTimeLeft());

        bodies.clear();

    }

    private void verifyBounds(Body body) {

        if (body.getUserData() instanceof CometModel || body.getUserData() instanceof PlayerModel)
            return;

        if (body.getPosition().x < -10)
            body.setTransform(ARENA_WIDTH + 10, body.getPosition().y, body.getAngle());

        if (body.getPosition().y < -10)
            body.setTransform(body.getPosition().x, ARENA_HEIGHT + 10, body.getAngle());

        if (body.getPosition().x > ARENA_WIDTH + 10)
            body.setTransform(-10, body.getPosition().y, body.getAngle());

        if (body.getPosition().y > ARENA_HEIGHT + 10)
            body.setTransform(body.getPosition().x, -10, body.getAngle());

    }

    public void createNewPlanets() {

        for (PlanetModel pm : planetsToAdd) {

            this.gameModel.addPlanet(pm);

            if (pm.getSize() == PlanetModel.PlanetSize.MEDIUM)
                new PlanetController(world, pm);
            else
                new BigPlanetController(world, pm);

        }

        planetsToAdd.clear();

    }

    public void checkForNewComet(float delta) {

        this.timeToNextComet -= delta;

        if (timeToNextComet <= 0) {

            Vector2 r = generateRandomPeripheralPoints(0);

            CometModel comet = this.gameModel.createComet(r.x, r.y);

            CometController cometC = new CometController(world, comet);

            int vx_direction = r.x > ARENA_WIDTH / 2.0f ? -1 : 1;
            int vy_direction = r.y > ARENA_HEIGHT / 2.0f ? -1 : 1;

            cometC.applyInitialVelocity(vx_direction, vy_direction);

            this.timeToNextComet = TIME_BETWEEN_COMETS;

        }

    }

    @Override
    public void beginContact(Contact contact) {

        Body A = contact.getFixtureA().getBody();
        Body B = contact.getFixtureB().getBody();

        if (A.getUserData() instanceof CometModel) {

            if (B.getUserData() instanceof PlayerModel)
                cometPlayerCollision(A, B);
            else if (B.getUserData() instanceof PlanetModel)
                cometObjectCollision(A, B);

        } else if (B.getUserData() instanceof CometModel) {

            if (A.getUserData() instanceof PlayerModel)
                cometPlayerCollision(B, A);
            else if (A.getUserData() instanceof PlanetModel)
                cometObjectCollision(B, A);

        } else if (A.getUserData() instanceof PlayerModel) {

            if (B.getUserData() instanceof PlanetModel)
                this.playerPlanetCollision(B);

        } else if (B.getUserData() instanceof PlayerModel) {

            if (A.getUserData() instanceof PlanetModel)
                this.playerPlanetCollision(A);

        }

    }

    private void cometPlayerCollision(Body comet, Body player) {

        System.out.println("YOU LOST!");

    }

    private void cometObjectCollision(Body comet, Body planet) {

        if (this.playerController.getPlanet() == planet)
            playerController.jump();

        this.gameModel.createExplosion(planet.getPosition().x, planet.getPosition().y);

        ((ElementModel) comet.getUserData()).setToBeRemoved(true);

        PlanetModel planetModel = (PlanetModel) planet.getUserData();

        planetModel.setToBeRemoved(true);

        Vector2 r = generateRandomPeripheralPoints(0);

        planetsToAdd.add(new PlanetModel(r.x, r.y, 0, random.nextBoolean() ? PlanetModel.PlanetSize.BIG : PlanetModel.PlanetSize.MEDIUM));

    }

    private void playerPlanetCollision(Body planet) {

        if (playerController.isInPlanet())
            return;

        playerController.setInPlanet(planet);

        playerController.handleInput(InputDecoder.Input.PLANET_LAND);

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public void removeFlagged() {

        Array<Body> bodies = new Array<Body>();

        world.getBodies(bodies);

        for (Body body : bodies) {

            if (((ElementModel) body.getUserData()).isToBeRemoved()) {

                this.gameModel.removeModel((ElementModel) body.getUserData());

                world.destroyBody(body);
            }

        }


    }

    private Vector2 generateRandomPeripheralPoints(float offset) {

        float x;
        float y;
        float r = random.nextFloat();

        if (random.nextBoolean()) {
            x = random.nextBoolean() ? 0 : ARENA_WIDTH;
            y = r * ARENA_HEIGHT;
        } else {
            y = random.nextBoolean() ? 0 : ARENA_HEIGHT;
            x = r * ARENA_WIDTH;
        }

        return new Vector2(x, y);

    }
}
