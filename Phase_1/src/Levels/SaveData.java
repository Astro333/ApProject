package Levels;

import Interfaces.Processable;
import Map.Map;
import Structures.Well;
import Structures.Workshop;
import Transportation.TransportationTool;

import java.util.HashMap;
import java.util.LinkedList;

public class SaveData {
    private final Integer coin = null;
    private final Map map = null;
    private final TransportationTool helicopter = null;
    private final Well well = null;
    private final LinkedList<Workshop> workshops = null;
    private final String levelLog = null;
    private final String pathToLevelJsonFile = null;
    private final HashMap<Processable, Integer> levelRequirements = null;

    private SaveData(){}

    public LinkedList<Workshop> getWorkshops() {
        return workshops;
    }

    public Integer getCoin() {
        return coin;
    }

    public Map getMap() {
        return map;
    }

    public TransportationTool getHelicopter() {
        return helicopter;
    }

    public TransportationTool getTruck() {
        return truck;
    }

    private final TransportationTool truck = null;
    private final HashMap<String, Byte> gameElementsLevel = null;

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
}
