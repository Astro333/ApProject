package Animals.Wild.SouthAmerica;

import Animals.Wild.Wild;

public class Jaguar extends Wild {
    public Jaguar(int x, int y) {
        super(x, y, 1, 1, AnimalType.Jaguar);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 10;
    }
}
