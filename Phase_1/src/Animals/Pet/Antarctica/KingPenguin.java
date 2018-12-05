package Animals.Pet.Antarctica;

import Animals.Pet.Pet;
import Items.Item;

public class KingPenguin extends Pet {
    public KingPenguin(int x, int y) {
        super(x, y, AnimalType.KingPenguin);
    }

    @Override
    public Item produce() {
        return null;
    }
}
