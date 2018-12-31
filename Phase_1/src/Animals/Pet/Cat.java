package Animals.Pet;

import Animals.Animal;
import Items.Item;
import Map.Map;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import static Utilities.Math_C.distance;

public class Cat extends Animal {

    private final byte intelligence;

    private Long destinationItemId = null;

    public Cat(int x, int y, byte intelligence) {
        super(x, y, 1, 1, AnimalType.Cat);
        destinationX = 0;
        destinationY = 0;
        this.intelligence = intelligence;
    }

    private Cat(){
        intelligence = 0;
    }

    public Long getDestinationItemId() {
        return destinationItemId;
    }

    public byte getIntelligence() {
        return intelligence;
    }

    /**
     * @param map map
     * @return {x,y} return changed position to map
     */

    public int[] updatePosition(Map map) {

        HashSet<Item> items = map.getItems();
        Item destinationItem = map.getCell(destinationX, destinationY).getItems().getOrDefault(destinationItemId, null);
        moveTowardDestination();
        if (x == destinationX && y == destinationY && items.contains(destinationItem)) {
            map.removeItem(destinationItem);
            map.getDepot().addStorable(destinationItem.getType());
            System.out.println("Cat collected "+destinationItem.getType());
            destinationItemId = null;
            destinationItem = null;
        }
        if (!items.contains(destinationItem)) {
            // if there is any item in map
            if (items.size() > 0) {
                if (intelligence >= 2) {
                    Iterator<Item> it = items.iterator();
                    destinationItem = it.next();
                    while (it.hasNext()) {
                        Item item = it.next();
                        if (distance(x, y, item.getX(), item.getY()) <
                                distance(x, y, destinationItem.getX(), destinationItem.getY())) {
                            destinationItem = item;
                        }
                    }
                    destinationX = destinationItem.getX();
                    destinationY = destinationItem.getY();
                    destinationItemId = destinationItem.getId();
                } else {
                    destinationItem = items.iterator().next();
                    destinationX = destinationItem.getX();
                    destinationY = destinationItem.getY();
                    destinationItemId = destinationItem.getId();
                }
            } else {
                destinationX = random.nextInt(map.cellsWidth);
                destinationY = random.nextInt(map.cellsHeight);
            }
        }
        return new int[]{x, y};
    }
}
