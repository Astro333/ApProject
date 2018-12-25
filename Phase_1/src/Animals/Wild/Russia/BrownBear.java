package Animals.Wild.Russia;

import Animals.Wild.Wild;

public class BrownBear extends Wild {
    public BrownBear(int x, int y) {
        super(x, y, 1, 1, AnimalType.BrownBear);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 10;
    }
}
