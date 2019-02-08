package Utilities;

import javafx.scene.image.Image;

public class Sprite {

    private final Image image;
    private final int[] offset;
    private final int[] frameSize;

    private final int[] relativeOffset;

    private final Integer frameCount;

    private final Double animationLength;
    private final Integer animationColumns;
    public Sprite(Image image,
                  int[] offset, int[] frameSize,
                  int[] relativeOffset,
                  Integer frameCount, Integer animationColumns,
                  Double animationLength) {
        this.image = image;
        this.offset = offset;
        this.frameSize = frameSize;
        this.relativeOffset = relativeOffset;
        this.frameCount = frameCount;

        this.animationLength = animationLength;
        this.animationColumns = animationColumns;
    }

    public int[] getRelativeOffset() {
        return relativeOffset;
    }

    public Image getImage() {
        return image;
    }

    public int[] getOffset() {
        return offset;
    }

    public int[] getFrameSize() {
        return frameSize;
    }

    public Integer getFrameCount() {
        return frameCount;
    }

    public Double getAnimationLength() {
        return animationLength;
    }

    public int getAnimationColumns() {
        return animationColumns;
    }
}
