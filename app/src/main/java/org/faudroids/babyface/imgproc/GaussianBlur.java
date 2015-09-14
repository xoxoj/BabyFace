package org.faudroids.babyface.imgproc;

import android.graphics.Bitmap;

public class GaussianBlur extends Convolution {
    private final Gaussian2D gaussian2D;

    public GaussianBlur(int size, double sigma) {
        this.gaussian2D = new Gaussian2D(size, sigma);
    }

    public Bitmap filter(Bitmap input) {
        return convolve(ImgMat.fromBitmap(input), this.gaussian2D).toBitmap();
    }
}
