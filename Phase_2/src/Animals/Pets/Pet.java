package Animals.Pets;

import Animals.Animal;
import Ground.Grass;
import Ground.GrassField;
import Items.Item;
import Utilities.AnimalType;
import Utilities.Loader;
import Utilities.State;
import Utilities.Sprite;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public abstract class Pet extends Animal {
    protected static GrassField grassField = null;
    protected final HashMap<State, ImageView> statesViews;

    protected static final HashMap<AnimalType, AudioClip> TOSS_SOUNDS;
    protected static final HashMap<AnimalType, AudioClip> HUNGRY_SOUNDS;
    protected static final HashMap<AnimalType, AudioClip> SPAWN_SOUNDS;
    protected static final HashMap<AnimalType, AudioClip> DIE_SOUNDS;

    static {
        TOSS_SOUNDS = new HashMap<>();
        HUNGRY_SOUNDS = new HashMap<>();
        SPAWN_SOUNDS = new HashMap<>();
        DIE_SOUNDS = new HashMap<>();

        LinkedList<AnimalType> animalTypes = new LinkedList<>();
        animalTypes.add(AnimalType.Buffalo);
        animalTypes.add(AnimalType.GuineaFowl);
        animalTypes.add(AnimalType.Ostrich);

        for (AnimalType animalType : animalTypes) {
            TOSS_SOUNDS.put(animalType,
                    new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/" + animalType + "_toss.mp3").toExternalForm()));
            DIE_SOUNDS.put(animalType,
                    new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/" + animalType + "_die.mp3").toExternalForm()));
            HUNGRY_SOUNDS.put(animalType,
                    new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/" + animalType + "_hungry.mp3").toExternalForm()));
            SPAWN_SOUNDS.put(animalType,
                    new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/" + animalType + "_spawn.mp3").toExternalForm()));
        }
        TOSS_SOUNDS.put(AnimalType.Cat, new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/Cat_toss.mp3").toExternalForm()));
        SPAWN_SOUNDS.put(AnimalType.Cat, new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/Cat_spawn.mp3").toExternalForm()));
        SPAWN_SOUNDS.put(AnimalType.Dog, new AudioClip(Pet.class.getResource("/res/Animals/Pets/Sounds/Dog_spawn.mp3").toExternalForm()));
    }

    protected final double hungerRate;

    public double getFullness() {
        return fullness.get();
    }

    public DoubleProperty fullnessProperty() {
        return fullness;
    }

    protected final DoubleProperty fullness;

    protected transient final int headOffsetX;
    protected transient final int headOffsetY;

    protected int[] destinationGrassCell = null;

    protected boolean isAutoPilot = true;
    protected final Rectangle hungerIndicator;

    protected final long foodSeekBuff = 100_000_000;
    protected final long foodEatBuff = 1_000_000_000;
    protected transient final int eatingSpeed;

    protected transient final AnimationTimer hungerControllerThread;
    protected transient final AnimationTimer foodSeekerThread;
    protected transient final Object eatLock = new Object();
    protected final ScheduledExecutorService productionTimer;
    private Runnable productionRunnable = new Runnable() {
        @Override
        public void run() {
            status.set(status.get() | State.Produced.value);
            productionTask = productionTimer.schedule(this, 16, TimeUnit.SECONDS);
        }
    };
    private ScheduledFuture<?> productionTask = null;

    protected Pet(AnimalType type) {
        super(type);
        productionTimer = Executors.newScheduledThreadPool(1);
        hungerIndicator = new Rectangle();
        fullness = new SimpleDoubleProperty(100);
        statesViews = new HashMap<>();

        for (State s : getStateTextures().keySet()) {
            Sprite t = getTexture(s);
            ImageView iv = new ImageView(t.getImage());
            iv.layoutXProperty().bind(scale.multiply(t.getRelativeOffset()[0]));
            iv.layoutYProperty().bind(scale.multiply(t.getRelativeOffset()[1]));
            statesViews.put(s, iv);
        }
        // inverting the scale, because - * - = +
        statesViews.get(State.MOVE_RIGHT).setScaleX(-1);
        statesViews.get(State.MOVE_UP_RIGHT).setScaleX(-1);
        statesViews.get(State.MOVE_DOWN_RIGHT).setScaleX(-1);

        for (ImageView iv : statesViews.values()) {
            iv.scaleXProperty().bind(scale.multiply(iv.getScaleX()));
            iv.scaleYProperty().bind(scale);
        }


        HashMap<String, Number> data = Loader.getPetsData().get(type);
        this.hungerRate = data.get("HUNGER_RATE").doubleValue();
        this.eatingSpeed = data.get("EAT_SPEED").intValue();
        this.headOffsetX = data.get("Head_Offset_X").intValue();
        this.headOffsetY = data.get("Head_Offset_Y").intValue();

        speed = BASE_SPEED;

        hungerControllerThread = setupHungerControllerThread();
        foodSeekerThread = setupFoodSeekerThread();

        hungerIndicator.widthProperty().bind(fullness);
        hungerIndicator.scaleXProperty().bind(scale);
        hungerIndicator.scaleYProperty().bind(scale);
        moveTowardRandomLocation();
    }

    protected void stopAllThreads() {
        foodSeekerThread.stop();
        positionUpdaterThread.stop();
        animation.stop();
        hungerControllerThread.stop();
    }

    private boolean reachedDestination = false;

    protected AnimationTimer setupFoodSeekerThread() {
        return new AnimationTimer() {
            private long prev = -1;
            private boolean isStart = false;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now - foodSeekBuff;
                    return;
                }
                synchronized (eatLock) {
                    Grass grass;
                    if (destinationGrassCell == null) {
                        destinationGrassCell = grassField.getNearestGrassBlock(getX(), getY());
                        if (destinationGrassCell != null) {
                            grass = grassField.getGrasses()[destinationGrassCell[0]][destinationGrassCell[1]];
                            moveToward(grass.getPosX() + headOffsetX * scale.get(),
                                    grass.getPosY() + headOffsetY * scale.get());
                        } else {
                            if (reachedDestination) {
                                moveTowardRandomLocation();
                            }
                        }
                    } else {
                        if (now - prev >= foodSeekBuff) {
                            grass = grassField.getGrasses()[destinationGrassCell[0]][destinationGrassCell[1]];
                            if (grass.getAmount() == 0) {
                                destinationGrassCell = null;
                                return;
                            } else {
                                moveToward(grass.getPosX() + headOffsetX * scale.get(),
                                        grass.getPosY() + headOffsetY * scale.get());
                            }
                            if (reachedDestination) {
                                startEating();
                                hungerControllerThread.stop();
                                stop();
                                return;
                            }
                            prev = now;
                        }
                    }
                }
            }

            @Override
            public void start() {
                if (!isStart) {
                    HUNGRY_SOUNDS.get(type).play();
                    synchronized (movementVector) {
                        isAutoPilot = false;
                    }
                    isStart = true;
                    speed = RUNNING_SPEED;
                    super.start();
                }
            }

            @Override
            public void stop() {
                isAutoPilot = true;
                isStart = false;
                speed = BASE_SPEED;
                prev = -1;
                super.stop();
            }
        };
    }

    private boolean isMoving = false;

    @Override
    protected AnimationTimer setupPositionUpdaterThread() {
        //noinspection Duplicates
        return new AnimationTimer() {
            private long prev = -1;
            private double deltaT;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }

                deltaT = (((now - prev) * GENERAL_TIME_MULTIPLIER.get()) / 1_000_000_000);
                setTextureX(graphic.getLayoutX() + deltaT * speed * movementVector.getX());
                setTextureY(graphic.getLayoutY() + deltaT * speed * movementVector.getY());
                synchronized (movementVector) {
                    if (movementVector.dot(des_x - getX(), des_y - getY()) < 0) {
                        if (isAutoPilot) {
                            moveTowardRandomLocation();
                        } else {
                            setX(des_x);
                            setY(des_y);
                            reachedDestination = true;
                            stop();
                            return;
                        }
                    }
                    prev = now;
                }
            }

            @Override
            public void start() {
                if (!isMoving) {
                    isMoving = true;
                    super.start();
                }
            }

            @Override
            public void stop() {
                if (isMoving) {
                    isMoving = false;
                    prev = -1;
                    super.stop();
                }
            }
        };
    }

    protected AnimationTimer setupHungerControllerThread() {
        return new AnimationTimer() {
            private long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                synchronized (eatLock) {
                    if (now - prev >= 100_000_000) {
                        double val = Math.max(0, fullness.get() - hungerRate * (now - prev) * GENERAL_TIME_MULTIPLIER.get() / 1_000_000_000D);
                        fullness.set(val);
                        if (val <= 50) {
                            if (val > 0) {
                                foodSeekerThread.start();
                            } else {
                                stop();
                                die();
                                return;
                            }
                        }
                        val *= 2.55;
                        hungerIndicator.setFill(Color.rgb(255 - (int) (val), (int) val, 0));
                        prev = now;
                    }
                }
            }

            @Override
            public void stop() {
                prev = -1;
                super.stop();
            }
        };
    }

    public static void setGrassField(GrassField grassField) {
        Pet.grassField = grassField;
    }

    public void moveToward(double des_x, double des_y) {
        if (this.des_x == des_x && this.des_y == des_y)
            return;
        reachedDestination = false;
        this.des_x = des_x;
        this.des_y = des_y;
        synchronized (movementVector) {
            movementVector.update(des_x - getX(), des_y - getY());
        }
        State s = getDirection(des_x, des_y);
        ImageView view = statesViews.get(s);
        boolean change = true;
        if (animation.getImageViews().size() > 0) {
            if (animation.getImageViews().get(0).equals(view))
                change = false;
        }
        if (change) {
            graphic.getChildren().clear();
            animation.stop();
            Sprite texture = getTexture(s);
            graphic.getChildren().add(hungerIndicator);
            graphic.getChildren().add(hitBox);
            graphic.getChildren().add(view);
            animation.clear();
            animation.addTexture(texture, view);
            animation.playFromStart();
            positionUpdaterThread.start();
        }
    }

    protected Path createTossPath(double endX, double endY) {
        MoveTo moveTo = new MoveTo();
        moveTo.setX(hitBox.getCenterX());
        moveTo.setY(hitBox.getCenterY());

        ArcTo arcTo = new ArcTo();
        arcTo.setX(endX - graphic.getLayoutX());
        arcTo.setY(endY - graphic.getLayoutY());

        arcTo.setRadiusX(300);
        arcTo.setRadiusY(400);

        Path path = new Path();
        path.getElements().addAll(moveTo, arcTo);
        return path;
    }

    public void destruct() {
        eatingTimer.stop();
        animation.stop();
        animation.clear();
        hungerControllerThread.stop();
        positionUpdaterThread.stop();
        foodSeekerThread.stop();

        for (ImageView view : statesViews.values()) {
            view.setImage(null);
            view.layoutYProperty().unbind();
            view.layoutXProperty().unbind();
            view.scaleXProperty().unbind();
            view.scaleYProperty().unbind();
        }
        scale.unbind();
        statesViews.clear();
        graphic.getChildren().clear();
    }

    @Override
    public void togglePause() {
        if (isPaused) {
            productionTask = productionTimer.schedule(productionRunnable, 16, TimeUnit.SECONDS);
            if ((getStatus() & (State.BeingTossed.value)) != 0) {
                tossAnimation.play();
            } else if ((getStatus() & (State.Eat.value)) != 0) {
                animation.play();
                eatingTimer.start();
            } else {
                hungerControllerThread.start();
            }
        } else {
            if (productionTask != null)
                productionTask.cancel(true);
            if ((status.get() & (State.BeingTossed.value)) != 0) {
                tossAnimation.pause();
            } else if ((status.get() & (State.Eat.value)) != 0) {
                animation.pause();
                eatingTimer.stop();
            } else {
                hungerControllerThread.stop();
                foodSeekerThread.stop();
            }
        }
        super.togglePause();
    }

    private AnimationTimer eatingTimer = new AnimationTimer() {
        long prev = -1;

        @Override
        public void handle(long now) {
            if (prev == -1) {
                prev = now;
                return;
            }
            if (now - prev >= 100_000_000) {
                double deltaT = (now - prev) * GENERAL_TIME_MULTIPLIER.get() / 1_000_000_000D;
                synchronized (eatLock) {
                    double full = fullness.get();
                    if (full < 100) {
                        double amount = eatingSpeed * deltaT;
                        double val = grassField.useGrass(destinationGrassCell[0], destinationGrassCell[1], amount);
                        if (val != 0) {
                            fullness.set(Math.min(100, full + val));
                        } else {
                            destinationGrassCell = null;
                            foodSeekerThread.start();
                            hungerControllerThread.start();
                            stop();
                            status.set(status.get() & (~State.Eat.value));
                            return;
                        }
                    } else {
                        isAutoPilot = true;
                        destinationGrassCell = null;
                        moveTowardRandomLocation();
                        hungerControllerThread.start();
                        stop();
                        status.set(status.get() & (~State.Eat.value));
                        return;
                    }
                }
                prev = now;
            }
        }

        @Override
        public void stop() {
            prev = -1;
            super.stop();
        }
    };

    public void startEating() {
        graphic.getChildren().clear();
        animation.clear();
        Sprite t = getStateTextures().get(State.Eat);
        ImageView v = statesViews.get(State.Eat);
        animation.addTexture(t, v);
        graphic.getChildren().add(v);
        graphic.getChildren().add(hungerIndicator);
        graphic.getChildren().add(hitBox);
        animation.playFromStart();
        status.set(status.get() | State.Eat.value);
        eatingTimer.start();
    }

    private FadeTransition dyingAnimation = null;

    public void die() {
        stopMoving();
        animation.stop();
        graphic.getChildren().clear();
        ImageView view = statesViews.get(State.Death);
        graphic.getChildren().add(view);
        animation.addTexture(getStateTextures().get(State.Death), view);
        dyingAnimation = new FadeTransition(animation.getCycleDuration());
        dyingAnimation.setNode(graphic);
        dyingAnimation.setToValue(0.01);
        DIE_SOUNDS.get(type).play();
        animation.setCycleCount(1);
        animation.playFromStart();
        dyingAnimation.playFromStart();
        dyingAnimation.setOnFinished(e -> {
            dyingAnimation = null;
            destruct();
            status.set(status.get() | State.Death.value);
        });
    }

    private boolean beingTossed = false;
    private ParallelTransition tossAnimation = null;

    public void toss() {
        if (!beingTossed) {
            status.set(State.BeingTossed.value);
            beingTossed = true;
            hungerControllerThread.stop();
            foodSeekerThread.stop();
            positionUpdaterThread.stop();
            eatingTimer.stop();
            TOSS_SOUNDS.get(type).play();
            stopMoving();
            animation.stop();
            graphic.getChildren().clear();
            graphic.getChildren().add(new ImageView(getPicture()));
            RotateTransition rotateTransition = new RotateTransition();
            ScaleTransition scaleTransition = new ScaleTransition();
            PathTransition pathTransition = new PathTransition();
            tossAnimation = new ParallelTransition();
            tossAnimation.setNode(graphic);
            pathTransition.setDuration(Duration.millis(2000));
            pathTransition.setInterpolator(Interpolator.EASE_IN);

            Path path = createTossPath(200, 40);

            pathTransition.setPath(path);
            scaleTransition.setDuration(Duration.millis(2000));
            scaleTransition.setFromX(scale.get());
            scaleTransition.setFromY(scale.get());
            scaleTransition.setToX(0.001);
            scaleTransition.setToY(0.001);

            rotateTransition.setToAngle(360);
            rotateTransition.setDuration(Duration.millis(2000));
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.setRate(9);
            rotateTransition.setCycleCount(Animation.INDEFINITE);

            tossAnimation.getChildren().addAll(rotateTransition, scaleTransition, pathTransition);
            tossAnimation.playFromStart();
            scaleTransition.setOnFinished(e -> {
                tossAnimation.stop();
                tossAnimation = null;
                destruct();
                status.set(State.Tossed.value);
            });
        }
    }

    @Override
    public void spawn(double x, double y) {
        ImageView view = new ImageView(getPicture());
        view.setScaleX(scale.get());
        view.setScaleY(scale.get());
        graphic.getChildren().add(view);
        scale(scale.get());
        graphic.setLayoutX(x - hitBox.getCenterX());
        graphic.setLayoutY(-100);
        status.set(status.get() & State.Spawning.value);
        new AnimationTimer() {
            long prev = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaT = (now - prev) * GENERAL_TIME_MULTIPLIER.get() / 1_000_000_000D;
                graphic.setLayoutY(graphic.getLayoutY() + 100 * deltaT);
                if (graphic.getLayoutY() >= y - hitBox.getCenterY()) {
                    SPAWN_SOUNDS.get(type).play();
                    stop();
                    graphic.getChildren().remove(view);
                    moveTowardRandomLocation();
                    hungerControllerThread.start();
                    productionTask = productionTimer.schedule(productionRunnable, 16, TimeUnit.SECONDS);
                    status.set(status.get() & (~State.Spawning.value));
                }
            }
        }.start();
    }

    public void startMoving() {
        moveToward(des_x, des_y);
        positionUpdaterThread.start();
        hungerControllerThread.start();
        animation.play();
    }

    public void stopMoving() {
        positionUpdaterThread.stop();
        animation.pause();
    }

    public static void setGeneralTimeMultiplier(DoubleProperty GENERAL_TIME_MULTIPLIER) {
        Pet.GENERAL_TIME_MULTIPLIER = GENERAL_TIME_MULTIPLIER;
    }

    public abstract Image getPicture();

    @Override
    public void scale(double v) {
        hungerIndicator.setTranslateY((v - 1) * (0.5 * hungerIndicator.getHeight()) + hitBox.getCenterY() * (1 - v));
        for (State s : statesViews.keySet()) {
            ImageView iv = statesViews.get(s);
            double width = getStateTextures().get(s).getFrameSize()[0];
            double height = getStateTextures().get(s).getFrameSize()[1];
            iv.setTranslateX((v - 1) * (0.5 * width) + hitBox.getCenterX() * (1 - v));
            iv.setTranslateY((v - 1) * (0.5 * height) + hitBox.getCenterY() * (1 - v));
        }
        scale.set(v);
        hitBox.setRadiusX(v * (HITBOX_RADIUS_X));
        hitBox.setRadiusY(v * (HITBOX_RADIUS_Y));
    }

    public abstract Item produce();

    protected abstract HashMap<State, Sprite> getStateTextures();

    protected abstract Sprite getTexture(State state);
}
