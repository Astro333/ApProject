package Animals.Wild.Russia;

import Animals.Wild.Wild;

public class BrownBear extends Wild {
    public BrownBear(int x, int y) {
        super(x, y, AnimalType.BrownBear);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 0;
    }
}
