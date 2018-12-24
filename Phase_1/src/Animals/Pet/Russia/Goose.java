package Animals.Pet.Russia;

import Animals.Pet.Pet;
import Items.Item;

public class Goose extends Pet {
    public Goose(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Goose);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Egg, x, y);
    }
}
