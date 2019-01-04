package Animals.Pet.Antarctica;

import Animals.Pet.Pet;
import Items.Item;

public class KingPenguin extends Pet {
    public KingPenguin(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.KingPenguin);
    }
    private KingPenguin(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Plume, x, y);
    }
}
