package Items;

import Interfaces.Processable;
import Utilities.SUID;

import java.util.HashMap;

public class Item {
    private int x, y;
    private final ItemType type;
    private Long id;

    /**
     * @param type Item type.
     */
    public Item(ItemType type, int x, int y) {
        this.type = type;
        id = SUID.generateId();
        this.x = x;
        this.y = y;
    }

    public Item(ItemType type) {
        this.type = type;
        id = SUID.generateId();
        x = 0;
        y = 0;
    }

    public long getId() {
        return id;
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
        Adornment(false),
        BrightHorn(false),
        CagedBrownBear(true), CagedJaguar(true), CagedLion(true), CagedWhiteBear(true), Cake(false),
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
        ItemType(boolean IS_ANIMAL){
            this.IS_ANIMAL = IS_ANIMAL;
        }
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
