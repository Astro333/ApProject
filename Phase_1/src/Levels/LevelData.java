package Levels;

public class LevelData {

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

    public int[] getWorkshopsPosition() {
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

    private final String[] goals;
    private final int[]  goalsAmount;

    private final String[] workshops;
    private final int[] workshopsLevel;
    private final int[] workshopsPosition;

    private final String[] startingPets;
    private final int[] startingPetsAmount;


    private final int initialCoin;

    private final int goldenTime;
    private final int goldenPrize;

    private final int silverTime;
    private final int silverPrize;

    private final int bronzeTime;
    private final int bronzePrize;

    private final int prize;

    private LevelData(String[] goals,
                      int[] goalsAmount,
                      String[] workshops,
                      int[] workshopsLevel,
                      int[] workshopsPosition,
                      String[] startingAnimals,
                      int[] startingAnimalsAmount,
                      int initialCoin,
                      int goldenTime,
                      int goldenPrize,
                      int silverTime,
                      int silverPrize,
                      int bronzeTime,
                      int bronzePrize,
                      int prize)
    {
        this.goals= goals;
        this.goalsAmount = goalsAmount;
        this.workshops = workshops;
        this.workshopsLevel = workshopsLevel;
        this.workshopsPosition = workshopsPosition;
        this.startingPets = startingAnimals;
        this.startingPetsAmount = startingAnimalsAmount;
        this.initialCoin = initialCoin;
        this.goldenTime =goldenTime;
        this.goldenPrize = goldenPrize;
        this.silverTime = silverTime;
        this.silverPrize = silverPrize;
        this.bronzeTime = bronzeTime;
        this.bronzePrize = bronzePrize;
        this.prize = prize;
    }
}
