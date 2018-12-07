package Levels;

public class LevelData {

    private final String[] goals = null;
    private final int[]  goalsAmount = null;

    private final String[] workshops = null;
    private final int[] workshopsLevel = null;
    private final byte[] workshopsPosition = null;

    private final String[] startingPets = null;
    private final int[] startingPetsAmount = null;

    private final Byte wellStartingLevel = null;
    private final Byte helicopterStartingLevel = null;
    private final Byte truckStartingLevel = null;

    private final String continent = null;//first letter must be upper case

    private final Integer initialCoin = null;

    private final Integer goldenTime = null;
    private final Integer goldenPrize = null;

    private final Integer silverTime = null;
    private final Integer silverPrize = null;

    private final Integer bronzeTime = null;
    private final Integer bronzePrize = null;

    private final Integer prize = null;

    private LevelData() {
    }

    public Byte getHelicopterStartingLevel() {
        return helicopterStartingLevel;
    }

    public Byte getTruckStartingLevel() {
        return truckStartingLevel;
    }

    public byte getWellStartingLevel() {
        return wellStartingLevel;
    }

    public String[] getGoals() {
        return goals;
    }

    public int[] getGoalsAmount() {
        return goalsAmount;
    }

    public String[] getWorkshops() {
        return workshops;
    }

    public int[] getWorkshopsLevel() {
        return workshopsLevel;
    }

    public byte[] getWorkshopsPosition() {
        return workshopsPosition;
    }

    public String[] getStartingPets() {
        return startingPets;
    }

    public int[] getStartingPetsAmount() {
        return startingPetsAmount;
    }

    public int getInitialCoin() {
        return initialCoin;
    }

    public int getGoldenTime() {
        return goldenTime;
    }

    public int getGoldenPrize() {
        return goldenPrize;
    }

    public int getSilverTime() {
        return silverTime;
    }

    public int getSilverPrize() {
        return silverPrize;
    }

    public int getBronzeTime() {
        return bronzeTime;
    }

    public int getBronzePrize() {
        return bronzePrize;
    }

    public int getPrize() {
        return prize;
    }

    public String getContinent() {
        return continent;
    }
}
