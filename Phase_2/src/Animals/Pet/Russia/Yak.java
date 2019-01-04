package Animals.Pet.Russia;

import Animals.Pet.Pet;
import Items.Item;

public class Yak extends Pet {
    public Yak(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.Yak);
    }
    private Yak(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Wool, x, y);
    }
}
