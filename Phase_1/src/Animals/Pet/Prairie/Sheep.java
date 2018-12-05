package Animals.Pet.Prairie;

import Animals.Pet.Pet;
import Interfaces.Productive;
import Items.Item;

public class Sheep extends Pet{
    public Sheep(int x, int y) {
        super(x, y, AnimalType.Sheep);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Wool);
    }
}
