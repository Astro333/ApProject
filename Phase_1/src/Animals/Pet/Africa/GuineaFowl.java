package Animals.Pet.Africa;

import Animals.Pet.Pet;
import Interfaces.Productive;
import Items.Item;

public class GuineaFowl extends Pet {
    public GuineaFowl(int x, int y) {
        super(x, y, 1, 1, 6, AnimalType.GuineaFowl);
    }
    private GuineaFowl(){}
    @Override
    public Item produce() {
        return new Item(Item.ItemType.Egg, x, y);
    }
}
