package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Llama extends Pet {
    public Llama(int x, int y) {
        super(x, y,AnimalType.Llama);
    }

    @Override
    public Item produce() {
        return null;
    }
}
