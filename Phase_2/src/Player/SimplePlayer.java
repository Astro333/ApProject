package Player;

public class SimplePlayer {
    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    private int money;

    public void setGoldMoney(int goldMoney) {
        this.goldMoney = goldMoney;
    }

    public int getGoldMoney() {
        return goldMoney;
    }

    private int goldMoney;

    public String getName() {
        return name;
    }
    private int levelsFinished;
    private final String name;

    public void setLevelsFinished(int levelsFinished) {
        this.levelsFinished = levelsFinished;
    }

    public int getLevelsFinished() {
        return levelsFinished;
    }

    public SimplePlayer(String name, int money, int goldMoney, int levelsFinished){
        this.name = name;
        this.money = money;
        this.goldMoney = goldMoney;
        this.levelsFinished = levelsFinished;
    }
}
