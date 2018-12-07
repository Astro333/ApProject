package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Goat extends Pet {
    public Goat(int x, int y) {
        super(x, y,AnimalType.Goat);
    }

    @Override
    public Item produce() {
        return null;
    }
}
