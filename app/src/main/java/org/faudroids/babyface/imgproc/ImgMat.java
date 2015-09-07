package org.faudroids.babyface.imgproc;


import android.graphics.Bitmap;
import android.graphics.Color;


/**
 * Class to work with images on pixel level
 */
public class ImgMat {
    private final int rows;
    private final int cols;

    //Separate color channels
    private double[][] red;
    private double[][] green;
    private double[][] blue;
    private double[][] alpha;

    /**
     * Creates a new ImgMat sized rows*cols with solid black color
     * @param rows Image rows
     * @param cols Image columns
     */
    ImgMat(int rows, int cols) {
        this.cols = cols;
        this.rows = rows;

        this.red = new double[this.rows][this.cols];
        this.green = new double[this.rows][this.cols];
        this.blue = new double[this.rows][this.cols];
        this.alpha = new double[this.rows][this.cols];

        for(int y = 0; y < this.rows; ++y) {
            for(int x = 0; x < this.cols; ++x) {
                this.alpha[y][x] = 255;
                this.red[y][x] = 0;
                this.green[y][x] = 0;
                this.blue[y][x] = 0;
            }
        }
    }


    /**
     * Creates a new ImgMat initialized with given values
     * @param rows Image rows
     * @param cols Image columns
     * @param values Pixel values
     */
    ImgMat(int rows, int cols, int[] values) {
        this.rows = rows;
        this.cols = cols;

        this.red = new double[this.rows][this.cols];
        this.green = new double[this.rows][this.cols];
        this.blue = new double[this.rows][this.cols];
        this.alpha = new double[this.rows][this.cols];

        int index = 0;

        for(int y = 0; y < this.rows; ++y) {
            for(int x = 0; x < this.cols; ++x) {
                this.red[y][x] = (double)Color.red(values[index]);
                this.green[y][x] = (double)Color.green(values[index]);
                this.blue[y][x] = (double)Color.blue(values[index]);
                this.alpha[y][x] = (double)Color.alpha(values[index]);
                ++index;
            }
        }
    }


    /**
     * Creates an ImgMat object from a given {@link Bitmap}
     * @param input {@link Bitmap} input image
     * @return {@link ImgMat} object
     */
    public static ImgMat fromBitmap(Bitmap input) {
        int[] pixelValues = new int[input.getHeight() * input.getWidth()];
        input.getPixels(pixelValues, 0, 1, 0, 0, input.getWidth(), input.getHeight());

        return new ImgMat(input.getHeight(), input.getWidth(), pixelValues);
    }


    /**
     * Exports an {@link ImgMat} object to {@link Bitmap} image
     * @return {@link Bitmap} image
     */
    Bitmap toBitmap() {
        int index = 0;

        int[] pixelValues = new int[this.rows * this.cols];

        for(int y = 0; y < this.rows; ++y) {
            for(int x = 0; x < this.cols; ++x) {
                pixelValues[index] = Color.argb((int)this.alpha[y][x],
                                                (int)this.red[y][x],
                                                (int)this.green[y][x],
                                                (int)this.blue[y][x]);
                ++index;
            }
        }

        return Bitmap.createBitmap(pixelValues, this.cols, this.rows, Bitmap.Config
                .ARGB_8888);
    }


    /**
     * Returns a double array containing the ARGB values at pixel (row, col)
     * @param row y-coordinate
     * @param col x-coordinate
     * @return double array holding alpha, red, green and blue channel values
     */
    double[] at(int row, int col) {
        return new double[]{ this.alpha[row][col],
                             this.red[row][col],
                             this.green[row][col],
                             this.blue[row][col] };
    }


    /**
     * Sets the ARGB value at pixel (row, col)
     * @param row y-coordinate
     * @param col x-coordinate
     * @param values double array holding the new alpha, red, green and blue channel values
     */
    void at(int row, int col, double[] values) {
        this.alpha[row][col] = values[0];
        this.red[row][col] = values[1];
        this.green[row][col] = values[2];
        this.blue[row][col] = values[3];
    }


    /**
     * Returns the red channel
     * @return double[][] array containing the red channel values
     */
    double[][] red() {
        return this.red;
    }


    /**
     * Returns the green channel
     * @return double[][] array containing the green channel values
     */
    double[][] green() {
        return this.green;
    }


    /**
     * Returns the blue channel
     * @return double[][] array containing the blue channel values
     */
    double[][] blue() {
        return this.blue;
    }


    /**
     * Returns the alpha channel
     * @return double[][] array containing the alpha channel values
     */
    double[][] alpha() {
        return this.alpha;
    }
}
