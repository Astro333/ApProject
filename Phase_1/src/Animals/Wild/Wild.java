package Animals.Wild;

import Animals.Animal;
import Items.Item;

public abstract class Wild extends Animal {
    protected boolean isCaged = false;
    protected int cageBreakCountDown = -1;

    protected Wild(int x, int y, AnimalType type) {
        super(x, y, type);
    }

    public void destroy(Animal animal){
        animal.setDead(true);
    }
    public void destroy(Item item){

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
    public int[] updatePosition(){
        int[] position = new int[2];
        if(!isCaged){
            x = destinationX;
            y = destinationY;
            /*
            * implement movement Algorithm here.
            * */
        }
        else {
            --cageBreakCountDown;
            if(cageBreakCountDown == 0){
                return null;
            }
            destinationX = x;
            destinationY = y;
        }
        position[0] = x;
        position[1] = y;
        return position;
    }
}
