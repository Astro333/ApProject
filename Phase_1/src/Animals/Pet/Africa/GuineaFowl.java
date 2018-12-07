package Animals.Pet.Africa;

import Animals.Pet.Pet;
import Interfaces.Productive;
import Items.Item;

public class GuineaFowl extends Pet {
    public GuineaFowl(int x, int y) {
        super(x, y, AnimalType.GuineaFowl);
    }

    @Override
    public Item produce() {
        /*
        * Write code to produce Egg
        * */
        return null;
    }
}
