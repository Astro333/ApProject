package Items;

import Interfaces.Processable;
import Utilities.SUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Item {
    private int x, y;

    private final ItemType type;
    private final int itemPrice;
    private transient final BooleanProperty isTossed;
    private Long id;

    /**
     * @param type Item type.
     */
    public Item(ItemType type, int x, int y) {
        this.type = type;
        this.itemPrice = Utilities.Constants.getProductBuyCost(type.toString());
        id = SUID.generateId();
        isTossed = new SimpleBooleanProperty(this, "isTossed", false);
        this.x = x;
        this.y = y;
    }

    public Item(ItemType type) {
        this.type = type;
        this.itemPrice = Utilities.Constants.getProductBuyCost(type.toString());
        id = SUID.generateId();
        isTossed = new SimpleBooleanProperty(this, "isTossed", false);
        x = 0;
        y = 0;
    }

    public void setTossed(boolean tossed){
        isTossed.set(tossed);
    }

    public BooleanProperty isTossedProperty() {
        return isTossed;
    }

    public long getId() {
        return id;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public ItemType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    // implements Processable so that could be processed at workshops
    public enum ItemType implements Processable {
        Adornment,
        BrightHorn,
        CagedBrownBear, CagedJaguar, CagedLion, CagedWhiteBear, Cake,
        CarnivalDress, Cheese, CheeseFerment, ColoredPlume, Curd,
        DriedEgg,
        Egg,
        Fabric, Flour, FlouryCake,
        Horn,
        Intermediate,
        MegaPie,
        Milk,
        Plume,
        Sewing, SourCream, Souvenir, SpruceBrownBear, SpruceGrizzly, SpruceJaguar, SpruceLion, SpruceWhiteBear,
        Varnish,
        Wool
    }
}
