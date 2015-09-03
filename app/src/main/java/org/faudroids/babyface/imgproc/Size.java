package org.faudroids.babyface.imgproc;

public class Size {
    private int width;
    private int height;

    public Size(int width, int height) {
        this.height = height;
        this.width = width;
    }


    public int width() {
        return this.width;
    }


    public void width(int newWidth) {
        this.width = newWidth;
    }


    public int height() {
        return this.height;
    }


    public void height(int newHeigt) {
        this.height = newHeigt;
    }
}
