package org.faudroids.babyface.imgproc;


public class Gaussian implements Kernel {
    private final int size;
    private final double sigma;

    private final double[][] kernel;

    public Gaussian(int size, double sigma) {
        this.sigma = sigma;
        this.size = (size % 2 == 0) ? size + 1 : size;
        this.kernel = new double[this.size][this.size];
    }

    @Override
    public double[][] create() {
        int xCoord = -(this.size/2);
        int yCoord = -(this.size/2);

        double preFactor = 1/(2 * Math.PI * this.sigma * this.sigma);

        for(int y = 0; y <= this.size/2; ++y) {
            for(int x = 0; x <= this.size/2; ++x) {
                this.kernel[y][x] = preFactor * Math.exp(-1 * (xCoord * xCoord + yCoord * yCoord)/(2 *
                        this.sigma * this.sigma));
                this.kernel[this.kernel.length - 1 - y][this.kernel.length - 1 - x] = this
                        .kernel[y][x];
                this.kernel[y][this.kernel.length - 1 - x] = this.kernel[y][x];
                this.kernel[this.kernel.length - 1 - y][x] = this.kernel[y][x];
                ++xCoord;
            }
            ++yCoord;
            xCoord = -(this.size/2);
        }

        return this.kernel;
    }
}
