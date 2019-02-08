package Ground;

import Utilities.ViewSwitcher;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Grass {
    private double amount;
    private final int MAX_AMOUNT = 50;
    private final double Epsilon = 0.01;
    private final ViewSwitcher grassLevelSwitcher;
    private final Pane graphics;
    private final int x, y;
    public Grass(String continent, int amount, int i, int j){
        this.x = i;
        this.y = j;
        this.amount = 0;
        String name = continent.equals("Antarctica") ? "fish1.png" : "grass"+continent+".png";
        ImageView view = new ImageView(new Image("res/Grass/Textures/" +name));
        grassLevelSwitcher = new ViewSwitcher(view, 16, 4, new int[]{48, 48}, new int[]{0, 0});
        graphics = new Pane();
        graphics.setPickOnBounds(false);
        graphics.getChildren().add(view);
        useGrass(-amount);
    }

    public double getPosX(){
        return graphics.getLayoutX();
    }

    public double getPosY(){
        return graphics.getLayoutY();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getAmount() {
        return amount;
    }

    public Pane getGraphics() {
        return graphics;
    }
    public double useGrass(double amount){
        graphics.setOpacity(1);
        if(this.amount - amount >= 0 && this.amount - amount <= MAX_AMOUNT){
            this.amount -= amount;
            if(this.amount <= Epsilon)
                this.amount = 0;
            grassLevelSwitcher.setIndex((int) ((this.amount*1.0d/MAX_AMOUNT)*grassLevelSwitcher.getFrameCount())-1);
            return amount;
        } else if(this.amount - amount < 0){
            grassLevelSwitcher.setIndex(0);
            double ret = this.amount;
            this.amount = 0;
            graphics.setOpacity(0);
            return ret;
        } else {
            grassLevelSwitcher.setIndex(grassLevelSwitcher.getFrameCount()-1);
            double ret = this.amount-MAX_AMOUNT;
            this.amount = MAX_AMOUNT;
            return ret;
        }
    }
}
