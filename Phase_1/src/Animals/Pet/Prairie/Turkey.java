package Animals.Pet.Prairie;

import Animals.Pet.Pet;
import Items.Item;

public class Turkey extends Pet {
    public Turkey(int x, int y) {
        super(x, y,1, 1, 6, AnimalType.Turkey);
    }
    private Turkey(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Egg, x, y);
    }
}
