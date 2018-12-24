package Animals.Pet.SouthAmerica;

import Animals.Pet.Pet;
import Items.Item;

public class Llama extends Pet {
    public Llama(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Llama);
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Wool, x, y);
    }
}
