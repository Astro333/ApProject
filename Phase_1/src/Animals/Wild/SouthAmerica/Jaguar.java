package Animals.Wild.SouthAmerica;

import Animals.Wild.Wild;

public class Jaguar extends Wild {
    public Jaguar(int x, int y) {
        super(x, y, AnimalType.Jaguar);
    }

    @Override
    protected int calculateTossingBuffer() {
        return 0;
    }
}
