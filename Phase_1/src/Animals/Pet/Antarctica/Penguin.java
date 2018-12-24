package Animals.Pet.Antarctica;

import Animals.Pet.Pet;
import Items.Item;

public class Penguin extends Pet {
    public Penguin(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Penguin);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Egg, x, y);
    }
}
