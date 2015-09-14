package org.faudroids.babyface.imgproc;

public class Gaussian1D implements Kernel1D {
    private final int size;
    private final double sigma;

    private final double[] kernel;

    public Gaussian1D(int size, double sigma) {
        this.sigma = sigma;
        this.size = (size % 2 == 0) ? size + 1 : size;
        this.kernel = new double[this.size];
    }


    @Override
    public double[] create() {
        int coord = -(this.size/2);

        double preFactor = 1/(2 * Math.PI * this.sigma * this.sigma);

            for(int x = 0; x <= this.size/2; ++x) {
                this.kernel[x] = preFactor * Math.exp(-1 * (coord * coord)/(2 * this.sigma * this.sigma));
                this.kernel[this.kernel.length - 1 - x] = this.kernel[x];
                ++coord;
            }

        return this.kernel;
    }

    @Override
    public int getSize() {
        return this.size;
    }
}
