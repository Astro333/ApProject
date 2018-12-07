package Animals.Pet.Russia;

import Animals.Pet.Pet;
import Items.Item;

public class Yak extends Pet {
    public Yak(int x, int y) {
        super(x, y,AnimalType.Yak);
    }

    @Override
    public Item produce() {
        return null;
    }
}
