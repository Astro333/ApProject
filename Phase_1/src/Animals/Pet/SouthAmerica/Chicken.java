package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Chicken extends Pet {
    public Chicken(int x, int y) {
        super(x, y, AnimalType.Chicken);
    }

    @Override
    public Item produce() {
        return null;
    }
}
