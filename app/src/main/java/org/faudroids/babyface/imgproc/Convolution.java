package org.faudroids.babyface.imgproc;


public class Convolution {
    public Convolution() {

    }


    /**
     * Executes a 2D separable convolution
     * @param input {@link ImgMat} to filter
     * @param filter {@link Kernel1D} for separable convolution
     * @return {@link ImgMat} filtered image
     */
    public ImgMat convolve(ImgMat input, Kernel1D filter) {
        //Filter kernel
        double[] filterKernel = filter.create();

        int offset = filter.getSize()/2;
        int rows = input.rows();
        int cols = input.cols();

        for(ImageChannel channel : ImageChannel.values()) {
            //First convolve in x direction
            for (int y = 0; y < rows; ++y) {
                for (int x = 0; x < cols; ++x) {
                    double newVal = 0.0;

                    for (int i = -offset; i < offset; ++i) {
                    }
                }
            }
        }

        return null;
    }


    /**
     * Executes a 2D convolution
     * @param input {@link ImgMat} to filter
     * @param filter {@link Kernel2D} for convolution
     * @return {@link ImgMat} filtered image
     */
    public ImgMat convolve(ImgMat input, Kernel2D filter) {
        double[][] filterKernel = filter.create();

        int offset = filter.getSize()/2;

        int rows = input.rows();
        int cols = input.cols();

        for(ImageChannel channel : ImageChannel.values()) {
            double[][] channelData = input.getChannel(channel);

            for (int y = 0; y < rows; ++y) {
                for (int x = 0; x < cols; ++x) {
                    double newVal = 0.0;

                    for (int kernelY = -offset; kernelY <= offset; ++kernelY) {
                        for (int kernelX = -offset; kernelX <= offset; ++kernelX) {
                            int tmpX = wrapValue(input.cols(), x - kernelX);
                            int tmpY = wrapValue(input.rows(), y - kernelY);
                            newVal += filterKernel[kernelY + offset][kernelX + offset] *
                                    channelData[tmpY][tmpX];
                        }
                    }
                    channelData[y][x] = newVal;
                }
            }
            switch (channel) {
                case CHANNEL_RED:
                    input.red(channelData);
                    break;
                case CHANNEL_GREEN:
                    input.green(channelData);
                    break;
                case CHANNEL_BLUE:
                    input.blue(channelData);
                    break;
            }
        }

        return input;
    }


    private int wrapValue(int size, int coord) {
        if(coord < 0) {
            return coord + size;
        } else if(coord >= size) {
            return size - 1;
        }

        return coord;
    }


    private int noBorder(int size, int coord) {
        if(coord < 0) {
            return 0;
        } else if(coord >= size) {
            return size - 1;
        }

        return coord;
    }
}
