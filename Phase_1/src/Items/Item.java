package Items;

import Interfaces.Processable;
import Utilities.SUID;

import java.util.HashMap;

public class Item {
    private int x, y;

    private final ItemType type;
    private final int itemPrice;
    private Long id;

    /**
     * @param type Item type.
     */
    public Item(ItemType type, int x, int y) {
        this.type = type;
        this.itemPrice = Utilities.Constants.getProductBuyCost(type.toString());
        id = SUID.generateId();
        this.x = x;
        this.y = y;
    }

    public Item(ItemType type) {
        this.type = type;
        this.itemPrice = Utilities.Constants.getProductBuyCost(type.toString());
        id = SUID.generateId();
        x = 0;
        y = 0;
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

    @Override
    public String toString() {
        return type+", At ("+x+", "+y+")";
    }

    // implements Processable so that could be processed at workshops
    public enum ItemType implements Processable {
        Adornment,
        BrightHorn,
        CagedBrownBear, CagedJaguar, CagedLion, CagedWhiteBear, Cake,
        CarnivalDress, Cheese, CheeseFerment, ColoredPlume, Curd,
        DriedEggs,
        Egg,
        Fabric, Flour, FlouryCake,
        Horn,
        Intermediate,
        MegaPie,
        Milk,
        Plume,
        Sewing, SourCream, Souvenir, SpruceBrownBear, SpruceGrizzly, SpruceJaguar, SpruceLion, SpruceWhiteBear,
        Varnish,
        Wool,
        Coin;
        private static HashMap<String, ItemType> types;
        static {
            types = new HashMap<>();
            for(ItemType type : values()){
                types.put(type.toString(), type);
            }
        }
        public static ItemType getType(String name){
            return types.getOrDefault(name, null);
        }
    }
}
