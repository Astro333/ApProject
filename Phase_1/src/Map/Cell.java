package Map;

import Animals.Animal;
import Animals.Wild.Wild;
import Items.Item;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;

public class Cell {

    private transient final HashMap<Long, Wild> wilds = new HashMap<>();
    private transient final HashMap<Long, Animal> pets = new HashMap<>();
    private transient final HashMap<Long, Item> items = new HashMap<>();
    private transient final BooleanProperty noGrass =
            new SimpleBooleanProperty(this, "noGrass", false);


    private final int x;
    private final int y;
    private byte grassInCell;


    protected Cell(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public byte getGrassInCell() {
        return grassInCell;
    }
    public BooleanProperty noGrassProperty() {
        return noGrass;
    }

    protected void removeItem(Long itemId){
        items.remove(itemId);
    }
    protected void removeItem(Item item){
        items.remove(item.getId());
    }

    public HashMap<Long, Item> getItems() {
        return items;
    }

    protected void clearItems(){
        items.clear();
    }

    public HashMap<Long, Wild> getWilds() {
        return wilds;
    }

    public HashMap<Long, Animal> getPets() {
        return pets;
    }

    protected void cageWilds(int timeToBreakCage){
        for(Wild wild : wilds.values()){
            wild.setCaged(timeToBreakCage);
        }
    }

    public void useGrass(byte amount){
        if(grassInCell > amount)
            grassInCell -= amount;
        else {
            grassInCell = 0;
            noGrass.set(true);
        }
    }

    protected void setGrassInCell(byte value){
        grassInCell = value;
        if(grassInCell > 0)
            noGrass.set(false);
    }

    protected void addItem(Item item){
        item.setX(x);
        item.setY(y);
        items.put(item.getId(), item);
    }

    protected boolean removeAnimal(Animal animal){
        wilds.remove(animal.getId());
        pets.remove(animal.getId());
        return true;
    }

    protected void addAnimal(Animal animal) {
        if(animal instanceof Wild)
            wilds.put(animal.getId(), (Wild) animal);
        else
            pets.put(animal.getId(), animal);
    }
}
