package Utilities;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;

public class ViewSwitcher {
    private ImageView imageView;

    private int frameCount;

    private int columns;
    private int[] size;
    private int[] offset;
    private int index = 0;
    public ViewSwitcher(ImageView imageView, int frameCount, int columns, int[] size, int[] offset) {
        this.imageView = imageView;
        this.frameCount = frameCount;
        this.columns = columns;
        this.size = size;
        this.offset = offset;
        switchViewPort(0);
    }

    public ViewSwitcher(Sprite texture){
        this.imageView = new ImageView(texture.getImage());
        this.frameCount = texture.getFrameCount();
        this.columns = texture.getAnimationColumns();
        this.size = texture.getFrameSize();
        this.offset = texture.getOffset();
        switchViewPort(0);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setIndex(int index){
        if(index < frameCount && index >= 0){
            this.index = index;
            switchViewPort(index);
        }
    }

    private void switchViewPort(int index){
        final int x = (index % columns) * size[0] + offset[0];
        final int y = (index / columns) * size[1] + offset[1];
        imageView.setViewport(new Rectangle2D(x, y, size[0], size[1]));
    }

    public void switchToNextFrame(){
        index = Math.min(frameCount -1, index+1);
        switchViewPort(index);
    }

    public void switchToPreviousFrame(){
        index = Math.max(0, index-1);
        switchViewPort(index);
    }
}
