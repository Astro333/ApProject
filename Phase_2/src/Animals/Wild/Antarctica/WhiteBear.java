package Animals.Wild.Antarctica;

import Animals.Wild.Wild;

public class WhiteBear extends Wild {

    protected WhiteBear(int x, int y) {
        super(x, y, 1, 1, AnimalType.WhiteBear);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 10;
    }
}
