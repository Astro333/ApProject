package Animals.Pet.Prairie;

import Animals.Pet.Pet;
import Items.Item;

public class Sheep extends Pet{
    public Sheep(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Sheep);
    }

    private Sheep() {}

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Wool, x, y);
    }
}
