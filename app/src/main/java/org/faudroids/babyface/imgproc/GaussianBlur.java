package org.faudroids.babyface.imgproc;

import android.graphics.Bitmap;

public class GaussianBlur extends Convolution {
    private final Gaussian gaussian;

    public GaussianBlur(int size, double sigma) {
        this.gaussian = new Gaussian(size, sigma);
    }

    public Bitmap filter(Bitmap input) {
        return convolve(ImgMat.fromBitmap(input), this.gaussian).toBitmap();
    }
}
