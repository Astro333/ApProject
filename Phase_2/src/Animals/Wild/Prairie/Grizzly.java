package Animals.Wild.Prairie;

import Animals.Wild.Wild;

public class Grizzly extends Wild {
    public Grizzly(int x, int y) {
        super(x, y, 1, 1, AnimalType.Grizzly);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 10;
    }
}
