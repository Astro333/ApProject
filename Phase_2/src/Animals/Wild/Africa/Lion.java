package Animals.Wild.Africa;

import Animals.Wild.Wild;

public class Lion extends Wild {

    public Lion(int x, int y) {
        super(x, y, 1, 1, AnimalType.Lion);
    }

    private Lion(){}

    @Override
    protected int calculateTossingBuffer() {
        return 10;
    }
}
