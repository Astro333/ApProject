package Animals.Pet.Antarctica;

import Animals.Pet.Pet;
import Items.Item;

public class Penguin extends Pet {
    public Penguin(int x, int y) {
        super(x, y,AnimalType.Penguin);
    }

    @Override
    public Item produce() {
        return null;
    }
}
