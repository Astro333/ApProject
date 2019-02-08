package Animals.Pets;

import Animals.Animal;
import Buildings.Depot;
import Items.Item;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Cat extends Animal {

    private final HashMap<State, ImageView> statesViews;
    private String continent;
    private boolean isAutoPilot = true;
    private boolean reachedDestination = false;
    private boolean isMoving = false;
    private static HashMap<Long, Item> items;
    private Long destinationItemId = null;
    private transient final AnimationTimer itemSeekerThread;
    private transient static Depot depot;
    private boolean isIntelligent;

    public static void setDepot(Depot depot) {
        Cat.depot = depot;
    }

    private final long itemSeekBuff = 100_000_000;

    public static void setItems(HashMap<Long, Item> items) {
        Cat.items = items;
    }

    private static final HashMap<String, HashMap<State, Sprite>> stateTextures = new HashMap<>();
    private static final HashMap<String, Image> pictures = new HashMap<>();

    static {
        pictures.put("Africa", new Image(Cat.class.getResource("/res/Animals/Pets/Cat/Africa/Textures/pic.png").toExternalForm()));
    }

    static {
        HashMap<State, Sprite> temp = null;
        try {
            Reader reader = new FileReader(new File("src/res/Animals/Pets/Cat/Africa/States.json"));

            Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
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
    }

    public Cat(String continent, boolean isIntelligent) {
        super(AnimalType.Cat);
        this.isIntelligent = isIntelligent;
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
        itemSeekerThread = setupItemSeekerThread();
        itemSeekerThread.start();
    }

    private AnimationTimer setupItemSeekerThread() {
        return new AnimationTimer() {
            private long prev = -1;
            private boolean isStart = false;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now - itemSeekBuff;
                    return;
                }

                Item item;

                if (destinationItemId == null) {
                    destinationItemId = findNearestItem();
                    if (destinationItemId != null) {
                        synchronized (items) {
                            item = items.get(destinationItemId);
                            if (item != null) {
                                moveToward(item.getX(), item.getY());
                            }
                        }
                    } else {
                        if (reachedDestination) {
                            moveTowardRandomLocation();
                        }
                    }
                } else {
                    if ((now - prev) * GENERAL_TIME_MULTIPLIER.get() >= itemSeekBuff) {
                        synchronized (items) {
                            if (!items.containsKey(destinationItemId)) {
                                destinationItemId = null;
                                return;
                            } else {
                                item = items.get(destinationItemId);
                                moveToward(item.getX(), item.getY());
                            }
                            if (reachedDestination) {
                                if (depot.addStorable(item.getType())) {
                                    item.collect();
                                } else {
                                    Utility.FOOL_ACTION_SOUND.play();
                                    depot.spark();
                                    prev += 1_000_000_000;
                                }
                                destinationItemId = null;
                                return;
                            }
                        }
                        prev = now;
                    }
                }

            }

            @Override
            public void start() {
                if (!isStart) {
                    synchronized (movementVector) {
                        isAutoPilot = false;
                    }
                    isStart = true;
                    super.start();
                }
            }

            @Override
            public void stop() {
                isAutoPilot = true;
                isStart = false;
                prev = -1;
                super.stop();
            }
        };
    }

    private Long findNearestItem() {
        Long itemId = null;
        double distance = Double.MAX_VALUE;
        synchronized (items) {
            for (Item item : items.values()) {
                if ((item.getStatus() & (State.BeingCollected.value | State.Spawning.value)) == 0) {
                    double x = (getX() - item.getX());
                    double y = (getY() - item.getY());
                    if (x * x + y * y < distance) {
                        itemId = item.id;
                    }
                    if (!isIntelligent) {
                        return itemId;
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

    private boolean beingTossed = false;
    private ParallelTransition tossAnimation = null;

    public void toss() {
        if (!beingTossed) {
            status.set(State.BeingTossed.value);
            beingTossed = true;
            positionUpdaterThread.stop();
            Pet.TOSS_SOUNDS.get(type).play();
            stopMoving();
            animation.stop();
            graphic.getChildren().clear();
            graphic.getChildren().add(new ImageView(pictures.get(continent)));
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

    private Path createTossPath(double endX, double endY) {
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
        animation.stop();
        animation.clear();
        positionUpdaterThread.stop();
        itemSeekerThread.stop();

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

    @Override
    public void scale(double v) {
        HashMap<State, Sprite> textures = stateTextures.get(continent);
        //noinspection Duplicates
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
                double deltaT = (now - prev) / 1_000_000_000D;
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
