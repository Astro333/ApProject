package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Chicken extends Pet {
    public Chicken(int x, int y) {
        super(x, y, 1, 1, 6,AnimalType.Chicken);
    }

    private Chicken(){}

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Egg, x, y);
    }
}
