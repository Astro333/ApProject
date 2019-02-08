package Items;

import Interfaces.Processable;
import Interfaces.Scalable;
import Interfaces.Spawnable;
import Utilities.Loader;
import Utilities.State;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Item implements Spawnable, Scalable {
    public static final HashMap<ItemType, Image> images;
    private static final AudioClip TAKE_SOUND = new AudioClip(Item.class.getResource("/res/Items/Sounds/product_take.mp3").toExternalForm());
    private static final AudioClip CRACK_SOUND = new AudioClip(Item.class.getResource("/res/Items/Sounds/product_crack.mp3").toExternalForm());
    private static final AudioClip SPAWN_SOUND = new AudioClip(Item.class.getResource("/res/Items/Sounds/product_landing.mp3").toExternalForm());
    private static ColorAdjust adjust = new ColorAdjust(0, 0, 0.3, 0);
    private static transient final Random random = new Random();
    private static double depotX, depotY;

    public static double getDepotX() {
        return depotX;
    }

    public static double getDepotY() {
        return depotY;
    }

    public final long id;

    public static void setDepotX(double depotX) {
        Item.depotX = depotX;
    }

    public static void setDepotY(double depotY) {
        Item.depotY = depotY;
    }

    private double scale = 1;

    static {
        images = new HashMap<>();
    }

    public static void setupTextures(String continent) {
        images.clear();
        for (ItemType type : ItemType.values()) {
            String des;
            switch (type) {
                case Horn:
                    if (!continent.equals("Russia") && !continent.equals("Prairie"))
                        des = "/res/Items/Textures/Images/Horn_" + continent + ".png";
                    else
                        continue;
                    break;
                case Egg:
                    des = "/res/Items/Textures/Images/Egg_" + continent + ".png";
                    break;
                case Wool:
                    if (!continent.equals("Antarctica")) {
                        des = "/res/Items/Textures/Images/Wool_" + continent + ".png";
                    } else
                        continue;
                    break;
                case Plume:
                    if (continent.equals("Africa") || continent.equals("Antarctica"))
                        des = "/res/Items/Textures/Images/Plume_" + continent + ".png";
                    else
                        continue;
                    break;
                default:
                    des = "/res/Items/Textures/Images/" + type.toString() + ".png";
                    break;
            }

            images.put(type, new Image(Item.class.getResource(des).toExternalForm()));
        }
    }

    public final Ellipse hitBox;
    private final ItemType type;
    private final Pane graphic;
    private ImageView view;

    /**
     * @param type Item type.
     */
    public Item(ItemType type, double x, double y) {
        this.type = type;
        this.status = new SimpleIntegerProperty(this, "status", 0);
        hitBox = new Ellipse(Loader.itemsConfig.get(type).get("hitBox_center_x").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_center_y").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_radius_x").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_radius_y").doubleValue());
        hitBox.setFill(Color.RED);
        hitBox.setOpacity(0.4);
        view = new ImageView(images.get(type));
        graphic = new Pane();
        setup();
        setX(x);
        setY(y);
        id = UUID.randomUUID().getLeastSignificantBits();
    }

    public Item(ItemType type) {
        this.type = type;
        this.status = new SimpleIntegerProperty(this, "status", 0);
        graphic = new Pane();
        view = new ImageView(images.get(type));

        hitBox = new Ellipse(Loader.itemsConfig.get(type).get("hitBox_center_x").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_center_y").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_radius_x").doubleValue(),
                Loader.itemsConfig.get(type).get("hitBox_radius_y").doubleValue());
        hitBox.setFill(Color.RED);
        hitBox.setOpacity(0.4);

        setup();
        id = UUID.randomUUID().getLeastSignificantBits();
    }

    private void setup() {
        view.setOnMouseEntered(e -> {
            view.setEffect(adjust);
        });
        view.setOnMouseExited(e -> view.setEffect(null));
    }

    public void collect() {
        if ((status.get() & (State.BeingCollected.value)) == 0) {
            status.set(status.get() | State.BeingCollected.value);
            TAKE_SOUND.play();
            ParallelTransition pt = createCollectionTransition();
            pt.playFromStart();
            pt.setOnFinished(e1 -> {
                synchronized (statusProperty()) {
                    status.set((status.get() & (~State.BeingCollected.value)) | State.Collected.value);
                    destruct();
                }
            });
        }
    }

    private ParallelTransition createCollectionTransition() {
        ScaleTransition st = new ScaleTransition();
        st.setFromX(scale);
        st.setFromY(scale);
        st.setToX(0.001);
        st.setToY(0.001);
        double x = depotX;
        double y = depotY;
        double animLength = Math.sqrt(x * x + y * y);
        st.setDuration(Duration.millis(animLength));
        TranslateTransition tt = new TranslateTransition();
        tt.setToX(x);
        tt.setToY(y);
        tt.setDuration(Duration.millis(animLength));
        ParallelTransition pt = new ParallelTransition();
        pt.setNode(graphic);
        pt.getChildren().addAll(st, tt);
        return pt;
    }

    public static ParallelTransition createToWorkshopTransition(ItemType type, double workshopX, double workshopY) {
        ScaleTransition st = new ScaleTransition();
        st.setFromX(0.001);
        st.setFromY(0.001);
        st.setToX(1);
        st.setToY(1);
        double x = workshopX;
        double y = workshopY;
        double animLength = Math.sqrt(x * x + y * y) * 3;
        st.setDuration(Duration.millis(animLength));
        TranslateTransition tt = new TranslateTransition();

        tt.setToX(x);
        tt.setToY(y);
        tt.setDuration(Duration.millis(animLength));
        ParallelTransition pt = new ParallelTransition();
        ImageView view = new ImageView(images.get(type));
        view.setTranslateX(depotX);
        view.setTranslateY(depotY);
        view.setScaleX(0.001);
        view.setScaleY(0.001);
        pt.setNode(view);
        view.setViewOrder(-1000);

        pt.getChildren().addAll(st, tt);
        return pt;
    }

    private void destruct() {
        graphic.onMouseClickedProperty().unbind();
        graphic.onMouseEnteredProperty().unbind();
        graphic.onMouseExitedProperty().unbind();
        graphic.getChildren().clear();
    }

    public void crack() {
        CRACK_SOUND.play();
        synchronized (status) {
            destruct();
            status.set(status.get() | State.Crack.value);
        }
    }

    @Override
    public void spawn(double x, double y) {
        if ((status.get() & (State.Spawning.value)) == 0) {
            SPAWN_SOUND.play();
            graphic.getChildren().add(hitBox);
            graphic.getChildren().add(view);
            graphic.viewOrderProperty().bind(graphic.translateYProperty().add(hitBox.getCenterY() + 10).multiply(-1));
            view.viewOrderProperty().bind(graphic.viewOrderProperty());
            ParallelTransition pt = createSpawnTransition(x, y);
            status.set(status.get() & State.Spawning.value);
            pt.setOnFinished(e -> status.set(status.get() & (~State.Spawning.value)));
            pt.playFromStart();
        }
    }

    public int getStatus() {
        return status.get();
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    private final IntegerProperty status;

    public Pane getGraphic() {
        return graphic;
    }

    public ItemType getType() {
        return type;
    }

    public void setX(double x) {
        graphic.setTranslateX(x - hitBox.getCenterX());
    }

    public void setY(double y) {
        graphic.setTranslateY(y - hitBox.getCenterY());
    }

    public double getX() {
        return graphic.getTranslateX() + hitBox.getCenterX();
    }

    public double getY() {
        return graphic.getTranslateY() + hitBox.getCenterY();
    }

    @Override
    public void scale(double v) {
        view.setScaleX(v);
        view.setScaleY(v);
        view.setTranslateX((v - 1) * (0.5 * view.getLayoutBounds().getWidth()) + hitBox.getCenterX() * (1 - v));
        view.setTranslateY((v - 1) * (0.5 * view.getLayoutBounds().getHeight()) + hitBox.getCenterY() * (1 - v));
        hitBox.setRadiusX(v * Loader.itemsConfig.get(type).get("hitBox_radius_x").doubleValue());
        hitBox.setRadiusY(v * Loader.itemsConfig.get(type).get("hitBox_radius_y").doubleValue());
    }

    private ParallelTransition createSpawnTransition(double x, double y) {
        ScaleTransition st = new ScaleTransition();
        st.setFromX(0.001);
        st.setFromY(0.001);
        st.setToX(scale);
        st.setToY(scale);
        long duration = (long) Math.sqrt(x * x + y * y);
        st.setDuration(Duration.millis(duration));
        TranslateTransition tt = new TranslateTransition();
        tt.setToX(x);
        tt.setToY(y);
        tt.setDuration(Duration.millis(duration));
        ParallelTransition pt = new ParallelTransition();
        pt.setNode(graphic);
        pt.getChildren().addAll(st, tt);
        return pt;
    }

    public ImageView getView() {
        return view;
    }

    public enum ItemType implements Processable {
        Adornment(false),
        BrightHorn(false),
        CagedBrownBear(true), CagedJaguar(true), CagedLion(true), CagedWhiteBear(true), CagedGrizzly(true), Cake(false),
        CarnivalDress(false), Cheese(false), CheeseFerment(false), ColoredPlume(false), Curd(false),
        DriedEggs(false),
        Egg(false),
        Fabric(false), Flour(false), FlouryCake(false),
        Horn(false),
        Intermediate(false),
        MegaPie(false),
        Milk(false),
        Plume(false),
        Sewing(false), SourCream(false), Souvenir(false),
        SpruceBrownBear(true), SpruceGrizzly(true), SpruceJaguar(true), SpruceLion(true), SpruceWhiteBear(true),
        Varnish(false),
        Wool(false),
        Coin(false);

        public final boolean IS_ANIMAL;

        ItemType(boolean IS_ANIMAL) {
            this.IS_ANIMAL = IS_ANIMAL;
        }

        private static HashMap<String, ItemType> types;

        static {
            types = new HashMap<>();
            for (ItemType type : values()) {
                types.put(type.toString(), type);
            }
        }

        public static ItemType getType(String name) {
            return types.getOrDefault(name, null);
        }
    }
}
