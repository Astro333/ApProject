package Animals.Pets;

import Animals.Animal;
import Animals.Wilds.Wild;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.*;
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

public class Dog extends Animal {

    private final HashMap<State, ImageView> statesViews;
    private static final Sprite battleTexture;
    private String continent;
    private boolean isAutoPilot = true;
    private boolean reachedDestination = false;
    private boolean isMoving = false;
    private Long destinationWildId = null;
    private int wildSeekBuff = 100_000_000;
    private static HashMap<Long, Wild> wilds;

    private static final AudioClip BARK_SOUND = new AudioClip(Dog.class.getResource(
            "/res/Animals/Pets/Sounds/Dog_bay.mp3").toExternalForm());

    public static void setWilds(HashMap<Long, Wild> wilds) {
        Dog.wilds = wilds;
    }

    private transient final AnimationTimer wildSeekerThread;

    private static final HashMap<String, HashMap<State, Sprite>> stateTextures = new HashMap<>();
    private static final HashMap<String, Image> pictures = new HashMap<>();

    static {
        pictures.put("Africa", new Image(Cat.class.getResource("/res/Animals/Pets/Dog/Africa/Textures/Images/pic.png").toExternalForm()));
    }

    static {
        HashMap<State, Sprite> temp = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Animals/Pets/Dog/Africa/States.json"));
            Type type = new TypeToken<HashMap<State, Sprite>>() {
            }.getType();
            temp = gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        temp.put(State.MOVE_UP_RIGHT, temp.get(State.MOVE_UP_LEFT));
        temp.put(State.MOVE_RIGHT, temp.get(State.MOVE_LEFT));
        temp.put(State.MOVE_DOWN_RIGHT, temp.get(State.MOVE_DOWN_LEFT));
        stateTextures.put("Africa", temp);
        try {
            reader = new FileReader(new File("src/res/Animals/Pets/Dog/battle.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Sprite t = gson.fromJson(reader, Sprite.class);
        battleTexture = t;
    }

    public Dog(String continent) {
        super(AnimalType.Dog);
        this.continent = continent;
        statesViews = new HashMap<>();

        for (State s : stateTextures.get(continent).keySet()) {
            Sprite t = stateTextures.get(continent).get(s);
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
        wildSeekerThread = setupWildSeekerThread();
        wildSeekerThread.start();
    }

    private AnimationTimer setupWildSeekerThread() {
        return new AnimationTimer() {
            private long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now - wildSeekBuff;
                    return;
                }
                Wild wild;

                if (destinationWildId == null) {
                    destinationWildId = findNearestWild();
                    if (destinationWildId != null) {
                        synchronized (wilds) {
                            speed = RUNNING_SPEED;
                            wild = wilds.get(destinationWildId);
                            if (wild != null) {
                                moveToward(wild.getX(), wild.getY());
                            }
                        }
                    } else {
                        if (reachedDestination) {
                            moveTowardRandomLocation();
                        }
                    }
                } else {
                    if ((now - prev) * GENERAL_TIME_MULTIPLIER.get() >= wildSeekBuff) {
                        synchronized (wilds) {
                            if (!wilds.containsKey(destinationWildId)) {
                                destinationWildId = null;
                                speed = BASE_SPEED;
                                return;
                            } else {
                                wild = wilds.get(destinationWildId);
                                moveToward(wild.getX(), wild.getY());
                            }
                            /*if (reachedDestination) {
                                if (depot.addStorable(wild.getType())) {
                                    wild.collect();
                                } else {
                                    Utility.FOOL_ACTION_SOUND.play();
                                    depot.spark();
                                    prev += 1_000_000_000;
                                }
                                destinationWildId = null;
                                return;
                            }*/
                        }
                        prev = now;
                    }
                }
            }

            @Override
            public void start() {
                isAutoPilot = false;
                super.start();
            }

            @Override
            public void stop() {
                isAutoPilot = true;
                prev = -1;
                super.stop();
            }
        };
    }

    private Long findNearestWild() {
        Long itemId = null;
        double distance = Double.MAX_VALUE;
        synchronized (wilds) {
            for (Wild wild : wilds.values()) {
                if ((wild.getStatus() & (State.Spawning.value)) == 0) {
                    double x = (getX() - wild.getX());
                    double y = (getY() - wild.getY());
                    if (x * x + y * y < distance) {
                        itemId = wild.id;
                    }
                }
            }
            return itemId;
        }
    }

    public void stopMoving() {
        positionUpdaterThread.stop();
        animation.pause();
    }

    private boolean inFight = false;

    public void enterFight() {
        if (!inFight) {
            inFight = true;
            BARK_SOUND.play();
            stopMoving();
            animation.stop();
            animation.clear();
            ImageView view = new ImageView(battleTexture.getImage());
            animation.addTexture(battleTexture, view);
            graphic.getChildren().clear();
            graphic.getChildren().add(view);
            TranslateTransition tt = new TranslateTransition();
            tt.setToX(1200);
            tt.setNode(graphic);
            tt.setDuration(Duration.millis(2000));
            tt.setCycleCount(1);
            tt.playFromStart();
            animation.playFromStart();
            tt.setOnFinished(e -> {
                status.set(status.get() | State.MarkedToRemove.value);
                destruct();
            });
        }
    }

    public void destruct() {
        animation.stop();
        animation.clear();
        positionUpdaterThread.stop();
        wildSeekerThread.stop();

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
    public void startMoving() {
        moveToward(des_x, des_y);
        positionUpdaterThread.start();
        animation.play();
    }

    @SuppressWarnings("Duplicates")
    @Override
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
            Sprite texture = stateTextures.get(continent).get(s);
            graphic.getChildren().add(hitBox);
            graphic.getChildren().add(view);
            animation.clear();
            animation.addTexture(texture, view);
            animation.playFromStart();
            positionUpdaterThread.start();
        }
    }

    public void togglePause() {
        if (isPaused) {
            wildSeekerThread.start();
        } else {
            wildSeekerThread.stop();
        }
        super.togglePause();
    }

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

    @SuppressWarnings("Duplicates")
    @Override
    public void scale(double v) {
        HashMap<State, Sprite> textures = stateTextures.get(continent);
        for (State s : statesViews.keySet()) {
            ImageView iv = statesViews.get(s);
            double width = textures.get(s).getFrameSize()[0];
            double height = textures.get(s).getFrameSize()[1];
            iv.setTranslateX((v - 1) * (0.5 * width) + hitBox.getCenterX() * (1 - v));
            iv.setTranslateY((v - 1) * (0.5 * height) + hitBox.getCenterY() * (1 - v));
        }
        scale.set(v);
        hitBox.setRadiusX(v * (HITBOX_RADIUS_X));
        hitBox.setRadiusY(v * (HITBOX_RADIUS_Y));
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void spawn(double x, double y) {
        ImageView view = new ImageView(pictures.get(continent));
        view.setScaleX(scale.get());
        view.setScaleY(scale.get());
        graphic.getChildren().add(view);
        scale(scale.get());
        graphic.setLayoutX(x - hitBox.getCenterX());
        graphic.setLayoutY(-100);
        new AnimationTimer() {
            long prev = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaT = (now - prev) * GENERAL_TIME_MULTIPLIER.get() / 1_000_000_000D;
                graphic.setLayoutY(graphic.getLayoutY() + 100 * deltaT);
                if (graphic.getLayoutY() >= y - hitBox.getCenterY()) {
                    Pet.SPAWN_SOUNDS.get(type).play();
                    stop();
                    moveTowardRandomLocation();
                }
            }
        }.start();
    }
}
