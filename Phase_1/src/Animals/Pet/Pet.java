package Animals.Pet;

import Animals.Animal;
import Interfaces.Productive;
import Map.Cell;
import Map.Map;

import java.util.HashSet;
import java.util.Iterator;

public abstract class Pet extends Animal implements Productive {
    protected byte fullness;

    protected Pet(int x, int y, AnimalType type) {
        super(x, y, type);
    }

    /**
     * @param map list of cells with grass
     * @return {x, y} tame has to return it's current position as a 2 indexed array
     * */

    //ToDo: Bug HERE(Where?)
    public int[] updatePosition(Map map){
        int[] position = new int[2];
        Cell within = map.getCell(x, y);
        HashSet<Cell> cellsWithGrass = map.getCellsWithGrass();
        x = destinationX;
        y = destinationY;
        position[0] = x;
        position[1] = y;

        if(fullness <= 4) {
            if(within.getGrassInCell() > 0) {
                fullness += 2;
                within.useGrass((byte)1);
                if(within.getGrassInCell() > 0) {
                    destinationX = x;
                    destinationY = y;
                    return position;
                }

            }
            else {
                Iterator<Cell> it = cellsWithGrass.iterator();
                int minDistance = Integer.MAX_VALUE;
                int distance;

                while (it.hasNext()) {
                    Cell temp = it.next();
                    distance = temp.getX() + temp.getY();
                    if (distance < minDistance) {
                        minDistance = distance;
                        destinationX = temp.getX();
                        destinationY = temp.getY();
                    }
                }
            }
        }
        increaseHunger();
        return position;
    }
    private void increaseHunger(){
        --fullness;
        if(fullness <= 0)
            setTossed(true);
    }
}
