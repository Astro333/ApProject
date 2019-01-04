package Animals.Pet.Africa;

import Animals.Pet.Pet;
import Items.Item;

public class Ostrich extends Pet {
    public Ostrich(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Ostrich);
    }
    private Ostrich(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Plume, x, y);
    }
}
