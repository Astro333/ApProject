package Animals.Wild;

import Animals.Animal;

public abstract class Wild extends Animal {
    protected boolean isCaged = false;
    protected int cageBreakCountDown = -1;
    private int tossingBuffer = -1;

    protected abstract int calculateTossingBuffer();

    protected Wild(int x, int y, int speed, int runningSpeed, AnimalType type) {
        super(x, y, speed, runningSpeed, type);
    }

    protected Wild(){}

    public int getTossingBuffer() {
        return tossingBuffer;
    }

    public void resetTossingBuffer() {
        tossingBuffer = calculateTossingBuffer();
    }

    public boolean isCaged() {
        return isCaged;
    }

    public void setCaged(int timeToBreakCage){
        if(!isCaged){
            isCaged = true;
            cageBreakCountDown = timeToBreakCage;
        }
    }

    public int[] updatePosition(int cellsWidth, int cellsHeight) {
        if (tossingBuffer > 0) {
            --tossingBuffer;
        } else {
            tossingBuffer = -1;
        }
        if(!isCaged){
            moveTowardDestination();
            destinationX = random.nextInt(cellsWidth);
            destinationY = random.nextInt(cellsHeight);
        }
        else {
            --cageBreakCountDown;
            if(cageBreakCountDown == 0){
                return null;
            }
            destinationX = x;
            destinationY = y;
        }
        return new int[]{x, y};
    }
}
