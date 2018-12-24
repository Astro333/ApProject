package Animals.Pet.Africa;

import Animals.Pet.Pet;
import Items.Item;

public class Buffalo extends Pet {
    public Buffalo(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Buffalo);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Horn, x, y);
    }
}
