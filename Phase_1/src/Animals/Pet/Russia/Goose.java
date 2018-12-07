package Animals.Pet.Russia;

import Animals.Pet.Pet;
import Items.Item;

public class Goose extends Pet {
    public Goose(int x, int y) {
        super(x, y,AnimalType.Goose);
    }

    @Override
    public Item produce() {
        return null;
    }
}
