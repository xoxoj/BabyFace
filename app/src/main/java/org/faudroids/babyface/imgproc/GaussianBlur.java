package org.faudroids.babyface.imgproc;

public class GaussianBlur extends Convolution {
    private final Gaussian gaussian;

    public GaussianBlur(int size, double sigma) {
        this.gaussian = new Gaussian(size, sigma);
    }
}
