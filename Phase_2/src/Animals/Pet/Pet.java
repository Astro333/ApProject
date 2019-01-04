package Animals.Pet;

import Animals.Animal;
import Interfaces.Productive;
import Items.Item;
import Map.Cell;
import Map.Map;
import Utilities.Math_C;

import java.util.HashSet;
import java.util.Iterator;

public abstract class Pet extends Animal implements Productive {
    protected int fullness;

    protected int timeRemainedToProduce;
    public final int PRODUCTION_TIME;
    protected Pet(int x, int y, int speed, int runningSpeed, int PRODUCTION_TIME, AnimalType type) {
        super(x, y, speed, runningSpeed, type);
        this.fullness = 20;
        this.PRODUCTION_TIME = PRODUCTION_TIME;
        timeRemainedToProduce = PRODUCTION_TIME/2;
    }
    protected Pet(){
        PRODUCTION_TIME = -1;
    }
    public int getFullness() {
        return fullness;
    }

    /**
     * @param map list of cells with grass
     * @return {x, y} tame has to return it's current position as a 2 indexed array
     */

    private boolean isEating = false;
    public int[] updatePosition(Map map) {
        --timeRemainedToProduce;
        if (timeRemainedToProduce < 0) {
            Item product = produce();
            map.addItem(product);
            System.out.println(getType()+" made "+product.getType());
            timeRemainedToProduce = PRODUCTION_TIME;
        }
        HashSet<Cell> cellsWithGrass = map.getCellsWithGrass();
        moveTowardDestination();
        Cell within = map.getCell(x, y);

        if (fullness <= 8 || (isEating && fullness <= 20)) {
            if (within.getGrassInCell() > 0) {
                fullness += 3;
                within.useGrass((byte)1);
                if (within.getGrassInCell() > 0) {
                    isEating = true;
                    destinationX = x;
                    destinationY = y;
                } else
                    isEating = false;
            } else {
                isEating = false;
                Iterator<Cell> it = cellsWithGrass.iterator();
                int minDistance = Integer.MAX_VALUE;
                int distance;

                while (it.hasNext()) {
                    Cell temp = it.next();
                    distance = Math_C.distance(temp.getX(), temp.getY(), x, y);
                    if (distance < minDistance) {
                        minDistance = distance;
                        destinationX = temp.getX();
                        destinationY = temp.getY();
                    }
                }
            }
        }
        incrementHunger();
        if(fullness <= 0)
            return null;
        return new int[]{x, y};
    }

    private void incrementHunger() {
        --fullness;
    }
}
