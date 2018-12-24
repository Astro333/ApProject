package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Goat extends Pet {
    public Goat(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Goat);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Horn, x, y);
    }
}
