package Animals.Wilds;

import Animals.Animal;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Wild extends Animal {
    protected static int cageLevel = 3;

    protected static final HashMap<String, Sprite> cages;
    protected final HashMap<String, ImageView> cagesViews;

    protected final HashMap<State, ImageView> statesViews;
    private static final AudioClip CAGE_CLICK = new AudioClip(Wild.class.getResource("/res/CommonSounds/cage_click.mp3").toExternalForm());

    private static final AudioClip CAGE_BROKE = new AudioClip(Wild.class.getResource("/res/CommonSounds/cage_broke_enemy_flee.mp3").toExternalForm());
    private static final AudioClip LANDING = new AudioClip(Wild.class.getResource("/res/CommonSounds/enemy_landing.mp3").toExternalForm());
    private static final HashMap<AnimalType, Image> SHADOWS;
    private static final HashMap<AnimalType, Image> PICTURES;
    private static final HashMap<AnimalType, AudioClip> SCREAMS;

    static {
        SCREAMS = new HashMap<>();
        SHADOWS = new HashMap<>();
        PICTURES = new HashMap<>();
        for (AnimalType animalType : AnimalType.getWilds().values()) {
            SCREAMS.put(animalType, new AudioClip(Wild.class.getResource("/res/Animals/Wilds/" + animalType +
                    "/Sounds/" + animalType + "_scream.mp3").toExternalForm()));
            SHADOWS.put(animalType, new Image(Wild.class.getResource("/res/Animals/Wilds/" + animalType + "/Textures/Images/shadow.png").toExternalForm()));
            PICTURES.put(animalType, new Image(Wild.class.getResource("/res/Animals/Wilds/" + animalType + "/Textures/Images/pic.png").toExternalForm()));
        }
    }

    private ScheduledExecutorService cageRegressionTimer;

    protected int cagingProgress = 0;

    protected ViewSwitcher cageStatusView;
    private final Object cageLock = new Object();

    private boolean hasTossed = false;

    public boolean isCaged() {
        return ((getStatus() & State.Caged.value) != 0);
    }

    public boolean hasTossed() {
        return hasTossed;
    }

    public void setHasTossed(boolean value) {
        hasTossed = value;
    }

    static {
        HashMap<String, Sprite> temp = null;
        try {
            Reader reader = new FileReader(new File("src/res/Cages/CageStates.json"));
            Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
            Type type = new TypeToken<HashMap<String, Sprite>>() {
            }.getType();
            temp = gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        cages = temp;
    }

    protected Wild(AnimalType type) {
        super(type);
        statesViews = new HashMap<>();
        cageRegressionTimer = Executors.newScheduledThreadPool(1);

        cagesViews = new HashMap<>();

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

        for (String s : cages.keySet()) {
            Sprite t = cages.get(s);
            ImageView iv = new ImageView(t.getImage());
            iv.layoutXProperty().bind(scale.multiply(t.getRelativeOffset()[0]));
            iv.layoutYProperty().bind(scale.multiply(t.getRelativeOffset()[1]));
            cagesViews.put(s, iv);
            iv.scaleXProperty().bind(scale);
            iv.scaleYProperty().bind(scale);
        }
        animation.setCycleCount(Animation.INDEFINITE);
        graphic.getChildren().add(hitBox);
        Sprite t = cages.get("build0" + (cageLevel + 1));
        cageStatusView = new ViewSwitcher(cagesViews.get("build0" + (cageLevel + 1)), t.getFrameCount(), t.getAnimationColumns(),
                t.getFrameSize(), t.getOffset());
        graphic.getChildren().add(cageStatusView.getImageView());
        graphic.viewOrderProperty().bind(graphic.layoutYProperty().add(hitBox.getCenterY()).multiply(-1));
    }

    private static ScaleTransition getFallAnimation() {
        ScaleTransition dropAnimation = new ScaleTransition();
        dropAnimation.setFromX(0.2);
        dropAnimation.setFromY(0.2);
        dropAnimation.setToX(1);
        dropAnimation.setToY(1);
        dropAnimation.setDuration(Duration.millis(4000/GENERAL_TIME_MULTIPLIER.get()));
        return dropAnimation;
    }

    public void togglePause() {
        if (isPaused) {
            if (dropAnimation != null) {
                dropAnimation.start();
            } else if (fallAnimation != null) {
                fallAnimation.play();
            }
            if (f != null) {
                f = cageRegressionTimer.schedule(incompleteCageRegressionTask, (long) (1000 / GENERAL_TIME_MULTIPLIER.get()), TimeUnit.MILLISECONDS);
            }
        } else {
            if (f != null) {
                f.cancel(true);
            }
            if (dropAnimation != null) {
                dropAnimation.stop();
            } else if (fallAnimation != null) {
                fallAnimation.pause();
            }
        }
        super.togglePause();
    }

    @Override
    public void destruct() {
        if (f != null)
            f.cancel(true);
        positionUpdaterThread.stop();
        cageRegressionTimer.shutdownNow();
        animation.stop();
        graphic.getChildren().clear();
        animation.clear();
    }

    @Override
    public void spawn(double x, double y) {
        ImageView shadow = new ImageView(SHADOWS.get(type));
        shadow.setLayoutX(x + Loader.getWildsData().get(type).get("ShadowOffsetX").doubleValue());
        shadow.setLayoutY(y + Loader.getWildsData().get(type).get("ShadowOffsetY").doubleValue());
        fallAnimation = getFallAnimation();
        fallAnimation.setNode(shadow);
        graphic.getChildren().add(shadow);
        status.set(status.get() | State.Spawning.value);
        SCREAMS.get(type).play();
        fallAnimation.playFromStart();
        fallAnimation.setOnFinished(e -> {
            fallAnimation = null;
            drop(x, y);
        });
    }

    private ScaleTransition fallAnimation = null;
    private AnimationTimer dropAnimation = null;

    private void drop(double x, double y) {
        graphic.getChildren().clear();
        ImageView view = new ImageView(PICTURES.get(type));
        graphic.getChildren().add(view);
        graphic.setLayoutX(x - hitBox.getCenterX());
        graphic.setLayoutY(-100);
        dropAnimation = new AnimationTimer() {
            long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                double deltaT = (now - prev) / 1_000_000_000D;
                graphic.setLayoutY(graphic.getLayoutY() + 100 * deltaT);
                if (graphic.getLayoutY() >= y - hitBox.getCenterY()) {
                    LANDING.play();
                    status.set(status.get() & (~State.Spawning.value));
                    moveToward(200, 200);
                    dropAnimation = null;
                    stop();
                }
            }

            @Override
            public void stop() {
                prev = -1;
                super.stop();
            }
        };
        dropAnimation.start();
    }

    public static void setCageLevel(int cageLevel) {
        Wild.cageLevel = cageLevel;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected AnimationTimer setupPositionUpdaterThread() {
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
                        moveTowardRandomLocation();
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

    private boolean isMoving = false;

    private boolean reachedDestination = false;

    @SuppressWarnings("Duplicates")
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
            graphic.getChildren().add(hitBox);
            graphic.getChildren().add(view);
            graphic.getChildren().add(cageStatusView.getImageView());
            animation.clear();
            animation.addTexture(texture, view);
            animation.playFromStart();
            positionUpdaterThread.start();
        }
    }

    private ScheduledFuture<?> f = null;
    private final Runnable incompleteCageRegressionTask = new Runnable() {
        @Override
        public void run() {
            synchronized (cageLock) {
                if (cagingProgress < (cageStatusView.getFrameCount() - 1) && cagingProgress != 0) {
                    speed *= 2;
                    cageStatusView.switchToPreviousFrame();
                    --cagingProgress;
                    if (cagingProgress != 0) {
                        f = cageRegressionTimer.schedule(this,
                                (long) ((6000 + cageLevel * 2000) / ((cageStatusView.getFrameCount() - 1)
                                        * GENERAL_TIME_MULTIPLIER.get())),
                                TimeUnit.MILLISECONDS);
                    } else {
                        f.cancel(true);
                    }
                }
            }
        }
    };

    public boolean progressCaging() {
        if (getStatus() != 8) {
            if (f != null)
                f.cancel(true);
            CAGE_CLICK.play();
            ++cagingProgress;
            if (cagingProgress < cageStatusView.getFrameCount() - 1) {
                speed /= 2;
                cageStatusView.switchToNextFrame();

                f = cageRegressionTimer.schedule(incompleteCageRegressionTask, (long) (1000 / GENERAL_TIME_MULTIPLIER.get()), TimeUnit.MILLISECONDS);
            } else {
                f.cancel(true);
                speed = 0;
                cageStatusView.setIndex(0);
                cagingProgress = 0;
                ImageView view = statesViews.get(State.Caged);
                if (view != animation.getImageViews().get(0)) {
                    animation.stop();
                    graphic.getChildren().clear();
                    animation.clear();
                    Sprite texture = getTexture(State.Caged);
                    Sprite cage = cages.get("break0" + (cageLevel + 1));
                    ImageView cageView = cagesViews.get("break0" + (cageLevel + 1));
                    animation.addTexture(cage, cageView);
                    animation.addTexture(texture, view);
                    graphic.getChildren().addAll(view, cageView);
                    graphic.getChildren().add(hitBox);
                    status.set(State.Caged.value);// caged status
                    Runnable cageBreakTask = new Runnable() {
                        @Override
                        public void run() {
                            animation.setRelativeRate(2.5);
                            animation.setCycleCount((cageLevel + 1) * 5);
                            animation.playFromStart();
                            animation.setOnFinished(e -> {
                                CAGE_BROKE.play();
                                animation.setCycleCount(Animation.INDEFINITE);
                                status.set(State.BrokeCage.value);// broke cage status
                                animation.setRelativeRate(1);
                                moveToward(random.nextBoolean() ? 0 : 10000, getY());
                                speed = RUNNING_SPEED;
                                animation.playFromStart();
                            });
                        }
                    };
                    f = cageRegressionTimer.schedule(cageBreakTask, (long) ((6 + cageLevel * 2) / GENERAL_TIME_MULTIPLIER.get()), TimeUnit.SECONDS);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void collect(double depotX, double depotY) {
        f.cancel(true);
        AnimationTimer timer = new AnimationTimer() {
            private long prev = -1;
            double Epsilon = 0.05;
            double init = scale.get();

            {
                des_x = depotX;
                des_y = depotY;
                double dx = (des_x - getX());
                double dy = (des_y - getY());
                double dis = Math.sqrt(dx * dx + dy * dy);
                movementVector.update(dx, dy);

                speed = (dis / ((init - Epsilon) * 1));
                positionUpdaterThread.start();
            }

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                } else {
                    double v = init - (now - prev) * GENERAL_TIME_MULTIPLIER.get() / 1_000_000_000D;
                    if (v <= Epsilon) {
                        animation.stop();
                        animation.clear();
                        stop();
                        graphic.getChildren().clear();
                        statesViews.clear();
                        cagesViews.clear();
                        graphic.getChildren().clear();
                        positionUpdaterThread.stop();
                        status.set(State.Collected.value);// collected status
                    }
                    scale(v);
                }
            }
        };
        timer.start();
    }

    public void startMoving() {
        moveToward(des_x, des_y);
        animation.play();
        positionUpdaterThread.start();
    }

    public void stopMoving() {
        positionUpdaterThread.stop();
        animation.pause();
    }

    protected abstract HashMap<State, Sprite> getStateTextures();

    protected abstract Sprite getTexture(State state);
}
