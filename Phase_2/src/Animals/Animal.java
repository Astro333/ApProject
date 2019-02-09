package Animals;

import Animals.Pets.Cat;
import Animals.Pets.Dog;
import Animals.Pets.Pet;
import Interfaces.Destructible;
import Interfaces.Scalable;
import Interfaces.Spawnable;
import Utilities.*;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.CacheHint;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public abstract class Animal implements Scalable, Spawnable, Destructible {

    protected static DoubleProperty GENERAL_TIME_MULTIPLIER = null;

    public static double getMaxX() {
        return MAX_X;
    }

    public static double getMinX() {
        return MIN_X;
    }

    public static double getMaxY() {
        return MAX_Y;
    }

    public static double getMinY() {
        return MIN_Y;
    }

    protected static double MAX_X = 500;

    protected static double MIN_X = 50;
    protected static double MAX_Y = 500;

    protected static double MIN_Y = 50;
    private static double sin_22_5 = 0.3826834323;

    private static double cos_22_5 = 0.9238795325;
    public final Ellipse hitBox;

    protected double des_x, des_y;
    protected final UnitVector movementVector;
    public final double BASE_SPEED;

    public final double RUNNING_SPEED;
    protected double speed;
    protected final double HITBOX_RADIUS_X;

    protected final double HITBOX_RADIUS_Y;
    protected final double HITBOX_CENTER_X;
    protected final double HITBOX_CENTER_Y;
    protected final AnimationTimer positionUpdaterThread;

    protected final Random random;

    protected transient final SpriteAnimation animation;
    protected final Pane graphic;

    protected final DoubleProperty scale;
    public final long id;

    protected final AnimalType type;
    protected final IntegerProperty status;

    public static Animal getInstance(AnimalType type, String continent) {
        switch (type) {
            case Cat:
                return new Cat(continent, true);
            case Dog:
                return new Dog(continent);
        }
        String animalClassPath = Loader.getAnimalClassPath(type);
        if (animalClassPath == null)
            return null;
        try {
            Class clazz = Class.forName(animalClassPath);
            Constructor constructor;
            if (clazz.isAssignableFrom(Pet.class)) {
                constructor = clazz.getDeclaredConstructor();
            } else {
                constructor = clazz.getDeclaredConstructor();
            }
            constructor.setAccessible(true);
            Animal instance = (Animal) constructor.newInstance();
            constructor.setAccessible(false);
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract void stopMoving();

    public abstract void startMoving();

    protected Animal(AnimalType type) {
        this.type = type;
        id = UUID.randomUUID().getLeastSignificantBits();
        random = new Random();
        scale = new SimpleDoubleProperty(1);
        movementVector = new UnitVector(random.nextDouble(), random.nextDouble());
        graphic = new Pane();
        graphic.setPickOnBounds(false);
        graphic.setCacheShape(true);
        graphic.setCache(true);
        graphic.setCacheHint(CacheHint.SPEED);


        status = new SimpleIntegerProperty(this, "status", 0);


        animation = new SpriteAnimation(GENERAL_TIME_MULTIPLIER, Duration.millis(1000));
        animation.setCycleCount(Animation.INDEFINITE);


        HashMap<String, Number> data = type.IS_WILD ? Loader.getWildsData().get(type) : Loader.getPetsData().get(type);
        this.HITBOX_RADIUS_X = data.get("HITBOX_RADIUS_X").doubleValue();
        this.HITBOX_RADIUS_Y = data.get("HITBOX_RADIUS_Y").doubleValue();
        this.HITBOX_CENTER_X = data.get("HITBOX_CENTER_X").doubleValue();
        this.HITBOX_CENTER_Y = data.get("HITBOX_CENTER_Y").doubleValue();

        this.BASE_SPEED = data.get("BASE_SPEED").doubleValue();
        this.RUNNING_SPEED = data.get("RUNNING_SPEED").doubleValue();

        speed = BASE_SPEED;
        hitBox = new Ellipse(HITBOX_CENTER_X, HITBOX_CENTER_Y, HITBOX_RADIUS_X, HITBOX_RADIUS_Y);
        hitBox.setFill(Color.rgb(255, 0, 0, 0.3));

        graphic.viewOrderProperty().bind(graphic.layoutYProperty().add(hitBox.getCenterY()).multiply(-1));

        positionUpdaterThread = setupPositionUpdaterThread();
    }

    protected State getDirection(UnitVector movementVector) {

        double tx = movementVector.getX();
        double ty = -(movementVector.getY());

        double delta_x = tx * cos_22_5 - ty * sin_22_5;
        double delta_y = tx * sin_22_5 + ty * cos_22_5;

        double tan = delta_y / delta_x;

        int i = tan < 0 ? 0 : 1;
        int j = delta_x < 0 ? 0 : 2;
        tan = tan < 0 ? -tan : tan;
        int k = tan < 1 ? 0 : 4;
        int index = i | j | k;
        return State.get(index);
    }

    protected boolean isPaused = false;

    public void togglePause() {
        if (isPaused) {
            isPaused = false;
            if ((status.get() & (State.Spawning.value | State.Eat.value | State.BeingTossed.value)) == 0)
                startMoving();
        } else {
            isPaused = true;
            if ((status.get() & (State.Spawning.value | State.Eat.value | State.BeingTossed.value)) == 0)
                stopMoving();
        }
    }

    public static void setMinY(double minY) {
        MIN_Y = minY;
    }

    protected State getDirection(double des_x, double des_y) {

        double tx = des_x - (graphic.getLayoutX() + hitBox.getCenterX());
        double ty = -(des_y - (graphic.getLayoutY() + hitBox.getCenterY()));

        double delta_x = tx * cos_22_5 - ty * sin_22_5;
        double delta_y = tx * sin_22_5 + ty * cos_22_5;

        double tan = delta_y / delta_x;

        int i = tan < 0 ? 0 : 1;
        int j = delta_x < 0 ? 0 : 2;
        tan = tan < 0 ? -tan : tan;
        int k = tan < 1 ? 0 : 4;
        int index = i | j | k;
        return State.get(index);
    }

    public static void setGeneralTimeMultiplier(DoubleProperty GENERAL_TIME_MULTIPLIER) {
        Animal.GENERAL_TIME_MULTIPLIER = GENERAL_TIME_MULTIPLIER;
    }

    public void moveTowardRandomLocation() {
        moveToward(MIN_X + (MAX_X - MIN_X) * random.nextDouble(),
                MIN_Y + (MAX_Y - MIN_Y) * random.nextDouble());
    }

    public double getY() {
        return graphic.getLayoutY() + hitBox.getCenterY();
    }

    public double getX() {
        return graphic.getLayoutX() + hitBox.getCenterX();
    }

    public void setX(double x) {
        graphic.setLayoutX(x - hitBox.getCenterX());
    }

    public void setY(double y) {
        graphic.setLayoutY(y - hitBox.getCenterY());
    }

    public void setTextureX(double x) {
        graphic.setLayoutX(x);
    }

    public void setTextureY(double y) {
        graphic.setLayoutY(y);
    }

    public abstract void moveToward(double des_x, double des_y);

    protected abstract AnimationTimer setupPositionUpdaterThread();

    public AnimalType getType() {
        return type;
    }

    public Pane getGraphic() {
        return graphic;
    }

    public int getStatus() {
        return status.get();
    }

    public static void setMaxX(double maxX) {
        MAX_X = maxX;
    }

    public static void setMinX(double minX) {
        MIN_X = minX;
    }

    public static void setMaxY(double maxY) {
        MAX_Y = maxY;
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    public boolean isSpawning() {
        return ((status.get() & State.Spawning.value) != 0);
    }
}
