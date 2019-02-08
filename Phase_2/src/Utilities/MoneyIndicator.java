package Utilities;

import Interfaces.Destructible;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;

public class MoneyIndicator implements Destructible {
    public Pane getGraphic() {
        return graphic;
    }

    private final Pane graphic;
    private final Rectangle surface;
    private final Text money;
    private final Text money_shadow;

    public MoneyIndicator() {
        graphic = new Pane();
        graphic.setPickOnBounds(false);
        money = new Text("999,999,999");
        money_shadow = new Text("999,999,999");
        money.setTextAlignment(TextAlignment.CENTER);
        surface = new Rectangle(0, 0, 0, 0);
        surface.setStrokeWidth(3);
        surface.setStroke(Color.GOLD);
        money.setFill(Color.GOLD);
        money.setFont(Font.font("IONA-U1", 24));
        surface.setWidth(money.getLayoutBounds().getWidth());
        surface.setHeight(money.getLayoutBounds().getHeight() + 49);
        money.setX(surface.getX() + surface.getWidth() / 2 - money.getLayoutBounds().getWidth() / 2);
        money.setY(surface.getY() + surface.getHeight() / 2 + money.getLayoutBounds().getHeight() / 2);

        money_shadow.setFill(Color.GOLD);
        money_shadow.setFont(Font.font("IONA-U1", 24));
        money_shadow.xProperty().bind(money.xProperty().subtract(6));
        money_shadow.yProperty().bind(money.yProperty().subtract(5));
        money_shadow.setOpacity(0.4);
        money_shadow.textProperty().bind(money.textProperty());
        graphic.getChildren().add(surface);
        graphic.getChildren().add(money_shadow);
        graphic.getChildren().add(money);
    }

    private static DecimalFormat formatter = new DecimalFormat("#,###");

    public void setAmount(int amount) {
        money.setText(formatter.format(amount) + " ⬤");
        updatePosition();
    }

    private void updatePosition() {
        money.setX(surface.getX() + surface.getWidth() / 2 - money.getLayoutBounds().getWidth() / 2);
        money.setY(surface.getY() + surface.getHeight() / 2 + money.getLayoutBounds().getHeight() / 2);
    }

    @Override
    public void destruct() {
        money_shadow.textProperty().unbind();
        money_shadow.xProperty().unbind();
        money_shadow.yProperty().unbind();
        graphic.getChildren().clear();
    }
}
