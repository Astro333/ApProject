package Levels;

import Animals.Animal;
import Buildings.Well;
import Buildings.Workshop;
import Interfaces.Processable;
import Items.Item;
import Transportation.Helicopter;
import Transportation.TransportationTool;
import Transportation.Truck;

import java.util.HashMap;
import java.util.LinkedList;

public class SaveData {
    private int coin;

    // Map Should Fill These Data And Read From Them
    private LinkedList<Animal> animalsInMap;
    private LinkedList<Item> itemsInMap;
    private int mapWidth;
    private int mapHeight;
    private int[][] cellsGrassLevel;

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // Depot Data
    private HashMap<Item.ItemType, Integer> depotItems;
    private byte depotMaxLevel;
    private float depotStoredThingsVolume;
    private byte depotLevel;
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private Helicopter helicopter = null;
    private Truck truck = null;
    private Well well;
    private HashMap<String, Byte> gameElementsLevel;

    private LinkedList<Workshop> workshops;

    private String levelLog = null;
    private String pathToLevelJsonFile = null;

    private HashMap<Processable, Integer> levelRequirements;

    private int timeRemainedToSpawnWild = 0;
    private int timePassed = 0;
    public SaveData(int mapWidth, int mapHeight){
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        cellsGrassLevel = new int[mapWidth][mapHeight];
        animalsInMap = new LinkedList<>();
        itemsInMap = new LinkedList<>();
        workshops = new LinkedList<>();
        levelRequirements = new HashMap<>();
        depotItems = new HashMap<>();
    }

    public HashMap<Item.ItemType, Integer> getDepotItems() {
        return depotItems;
    }

    public void setDepotItems(HashMap<Item.ItemType, Integer> depotItems) {
        this.depotItems = depotItems;
    }

    public float getDepotStoredThingsVolume() {
        return depotStoredThingsVolume;
    }

    public void setDepotStoredThingsVolume(float depotStoredThingsVolume) {
        this.depotStoredThingsVolume = depotStoredThingsVolume;
    }

    public byte getDepotMaxLevel() {
        return depotMaxLevel;
    }

    public void setDepotMaxLevel(byte depotMaxLevel) {
        this.depotMaxLevel = depotMaxLevel;
    }

    public byte getDepotLevel() {
        return depotLevel;
    }

    public void setDepotLevel(byte depotLevel) {
        this.depotLevel = depotLevel;
    }
    public void setLevelRequirements(HashMap<Processable, Integer> levelRequirements) {
        this.levelRequirements = levelRequirements;
    }

    public LinkedList<Workshop> getWorkshops() {
        return workshops;
    }

    public Integer getCoin() {
        return coin;
    }

    public TransportationTool getHelicopter() {
        return helicopter;
    }

    public TransportationTool getTruck() {
        return truck;
    }

    public void setGameElementsLevel(HashMap<String, Byte> gameElementsLevel) {
        this.gameElementsLevel = gameElementsLevel;
    }

    public HashMap<String, Byte> getGameElementsLevel() {
        return gameElementsLevel;
    }

    public Well getWell() {
        return well;
    }

    public String getLevelLog() {
        return levelLog;
    }

    public String getPathToLevelJsonFile() {
        return pathToLevelJsonFile;
    }


    public java.util.Map getLevelRequirements() {
        return levelRequirements;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public void setHelicopter(Helicopter helicopter) {
        this.helicopter = helicopter;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public void setWell(Well well) {
        this.well = well;
    }

    public void setLevelLog(String levelLog) {
        this.levelLog = levelLog;
    }

    public void setPathToLevelJsonFile(String pathToLevelJsonFile) {
        this.pathToLevelJsonFile = pathToLevelJsonFile;
    }

    public void setTimeRemainedToSpawnWild(int timeRemainedToSpawnWild) {
        this.timeRemainedToSpawnWild = timeRemainedToSpawnWild;
    }

    public LinkedList<Animal> getAnimalsInMap() {
        return animalsInMap;
    }

    public LinkedList<Item> getItemsInMap() {
        return itemsInMap;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int[][] getCellsGrassLevel() {
        return cellsGrassLevel;
    }

    public void setTimePassed(int timePassed) {
        this.timePassed = timePassed;
    }

    public int getTimeRemainedToSpawnWild() {
        return timeRemainedToSpawnWild;
    }

    public int getTimePassed() {
        return timePassed;
    }

    public void setWorkshops(LinkedList<Workshop> workshops) {
        this.workshops = workshops;
    }
}
