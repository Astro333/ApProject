package Animals.Pet.Antarctica;

import Animals.Pet.Pet;
import Items.Item;

public class Walrus extends Pet {
    public Walrus(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Walrus);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Horn, x, y);
    }
}
