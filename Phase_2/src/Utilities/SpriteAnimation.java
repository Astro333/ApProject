package Utilities;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class SpriteAnimation extends Transition {

    private final List<Sprite> textures;
    private final List<ImageView> views;
    private List<Integer> lastIndexes;
    private double relativeRate = 1;

    public SpriteAnimation(DoubleProperty rate, Sprite texture, ImageView view) {
        this.textures = new ArrayList<>();
        this.textures.add(texture);
        views = new ArrayList<>();
        views.add(view);
        initiateViewPorts();
        lastIndexes = new ArrayList<>();
        lastIndexes.add(0);
        rate.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                setRate(newValue.doubleValue()*relativeRate);
            }
        });
        setInterpolator(Interpolator.LINEAR);
        setCycleDuration(Duration.seconds(texture.getAnimationLength()));
        interpolate(0);
    }

    public SpriteAnimation(DoubleProperty rate, Duration duration){
        this.textures = new ArrayList<>();
        this.views = new ArrayList<>();
        lastIndexes = new ArrayList<>();
        lastIndexes.add(0);
        rate.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                setRate(newValue.doubleValue()*relativeRate);
            }
        });
        setInterpolator(Interpolator.LINEAR);
        setCycleDuration(duration);
    }

    public void setRelativeRate(double v){
        setRate(getRate()*v/relativeRate);
        relativeRate = v;
    }

    private void initiateViewPorts() {
        for (int i = 0; i < views.size(); ++i) {
            initiateViewPort(textures.get(i), views.get(i));
        }
    }

    private void initiateViewPort(Sprite texture, ImageView view) {
        final int x = texture.getOffset()[0];
        final int y = texture.getOffset()[1];
        Rectangle2D viewPort = new Rectangle2D(x, y, texture.getFrameSize()[0], texture.getFrameSize()[1]);
        view.setViewport(viewPort);
    }

    public List<Sprite> getTextures() {
        return textures;
    }

    @Override
    protected void interpolate(double k) {
        for (int i = 0; i < textures.size(); ++i) {
            Sprite texture = textures.get(i);
            int lastIndex = lastIndexes.get(i);
            int frame_count = texture.getFrameCount();
            final int index = Math.min((int) Math.floor(k * frame_count), frame_count-1);
            if (index != lastIndex) {
                int columns = texture.getAnimationColumns();
                int[] size = texture.getFrameSize();
                int[] offset = texture.getOffset();
                final int x = (index % columns) * size[0] + offset[0];
                final int y = (index / columns) * size[1] + offset[1];
                Rectangle2D viewPort = new Rectangle2D(x, y, size[0], size[1]);
                views.get(i).setViewport(viewPort);
                lastIndexes.set(i, index);
            }
        }
    }

    @Override
    public void stop() {
        interpolate(0);
        super.stop();
    }

    public List<ImageView> getImageViews() {
        return views;
    }

    public void addTexture(Sprite texture, ImageView view) {
        textures.add(texture);
        views.add(view);
        initiateViewPort(texture, view);
        lastIndexes.add(0);
    }

    public void clear() {
        textures.clear();
        views.clear();
        lastIndexes.clear();
    }
}