package Animals.Pet.Prairie;

import Animals.Pet.Pet;
import Items.Item;

public class Cow extends Pet {

    public Cow(int x, int y) {
        super(x, y, 1, 1, 6,AnimalType.Cow);
    }

    private Cow() {}

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Milk, x, y);
    }
}
