package Animals.Pet.Russia;

import Animals.Pet.Pet;
import Animals.Wild.Russia.BrownBear;
import Items.Item;

public class BrownCow extends Pet {
    public BrownCow(int x, int y) {
        super(x, y, 1, 1, 10, AnimalType.BrownCow);
    }

    private BrownCow(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Milk, x, y);
    }
}
