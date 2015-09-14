package org.faudroids.babyface.imgproc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import javax.inject.Inject;

class ImageProcessor {

	@Inject
	ImageProcessor() { }

    /**
     * Scales a whole bitmap to given {@link Size}. If one of the {@link Size} parameters is <= 0,
     * the image will be scaled preserving the aspect ratio
     * @param input Input {@link Bitmap}
     * @param newSize {@link Size} after scaling
     * @return Scaled {@link Bitmap}
     */
    public Bitmap scaleImage(Bitmap input, Size newSize) {
        double scaleFactor = (double)(input.getWidth())/input.getHeight();

        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return input;
        } else if(newSize.height() <= 0) {
            int newHeight = (int)(newSize.width() / scaleFactor);
            return Bitmap.createScaledBitmap(input, newSize.width(), newHeight, true);
        } else if(newSize.width() <= 0) {
            int newWidth = (int)(newSize.height() * scaleFactor);

            return Bitmap.createScaledBitmap(input, newWidth, newSize.height(), true);
        }

        return Bitmap.createScaledBitmap(input, newSize.width(), newSize.height(), true);
    }


    /**
     * Scales a given {@link Bitmap} such that a given {@link Rect} fits the new {@link Size}
     * @param input {@Link Bitmap} to scale
     * @param roi {@link Rect} which specifies the region of interest
     * @param newSize {@link Size} specifying the desired size
     * @return Scaled {@link Bitmap}
     */
    public Bitmap scaleImageROI(Bitmap input, Rect roi, Size newSize) {
        double aspectRatio = (double)(input.getWidth())/(double)(input.getHeight());
        int newWidth;
        int newHeight;

        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return input;
        } else if(newSize.height() <= 0) {
            double scaling = (double)(newSize.width())/roi.width();
            newWidth = (int)(input.getWidth() * scaling);
            newHeight = (int)(newWidth/aspectRatio);
        } else if(newSize.width() <= 0) {
            double scaling = (double)(newSize.height())/roi.height();
            newHeight = (int)(input.getHeight() * scaling);
            newWidth = (int)(newHeight * aspectRatio);
        } else {
            double scalingX = (double)(newSize.width())/roi.width();
            double scalingY = (double)(newSize.height())/roi.height();

            newWidth = (int)(input.getWidth() * scalingX);
            newHeight = (int)(input.getHeight() * scalingY);
        }
        return Bitmap.createScaledBitmap(input, newWidth, newHeight, true);
    }


    /**
     * Scales a {@link Rect} to a given {@link Size}. If one of the parameters of the {@link
     * Size} argument is <= 0 the {@link Rect} object is scaled preserving its aspect ratio
     * @param roi {@link Rect} to scale
     * @param newSize {@link Size} specifying the new size
     * @return Scaled {@link Rect}
     */
    public Rect scaleROI(Rect roi, Size newSize) {
        double scaleFactor = (double)(roi.width())/(double)(roi.height());

        int offsetX;
        int offsetY;

        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return roi;
        } else if(newSize.height() <= 0) {
            int newHeight = (int)(newSize.width() / scaleFactor);
            offsetY = newHeight/2;
            offsetX = newSize.width()/2;
        } else if(newSize.width() <= 0) {
            int newWidth = (int)(newSize.height() * scaleFactor);
            offsetY = newSize.height()/2;
            offsetX = newWidth/2;
        } else {
            offsetX = newSize.width()/2;
            offsetY = newSize.height()/2;
        }

        return new Rect(roi.centerX() - offsetX, roi.centerY() - offsetY, roi.centerX() +
                offsetX, roi.centerY() + offsetY);
    }


    /**
     * Crops a {@link Bitmap} to a region of interest given as {@link Rect}
     * @param input {@link Bitmap} to crop
     * @param roi {@link Rect} specifying the ROI
     * @return Cropped @{@link Bitmap}
     */
    public Bitmap cropImage(Bitmap input, Rect roi) {
        int left, top, width, height;

        int borderX = (input.getWidth() - roi.centerX() < roi.centerX()) ? input.getWidth() - roi
                .centerX() : roi.centerX();
        int borderY = (input.getHeight() - roi.centerY() < roi.centerY()) ? input.getHeight() -
                roi.centerY() : roi.centerY();

        left = roi.centerX() - borderX;
        width = 2 * borderX;
        top = roi.centerY() - borderY;
        height = 2 * borderY;

        return Bitmap.createBitmap(input, left, top, width, height);
    }

    /**
     * Creates a padded image with custom sized borders
     * @param input Input {@link Bitmap}
     * @param paddingLeft Padding on the left side of the image
     * @param paddingTop Padding at the top of the image
     * @param paddingRight Padding on the right side of the image
     * @param paddingBottom Padding at the bottom of the image
     * @return Padded {@link Bitmap}
     */
    public Bitmap createPaddedBitmap(Bitmap input,
                                     int paddingLeft,
                                     int paddingTop,
                                     int paddingRight,
                                     int paddingBottom) {
        if(paddingLeft < 0 || paddingTop < 0 || paddingRight < 0 || paddingBottom < 0) {
            throw new RuntimeException("Error! Padding can't be negative.");
        }
        Bitmap result = Bitmap.createBitmap(input.getWidth() + paddingLeft + paddingRight, input
                .getHeight() + paddingBottom + paddingTop, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(result);
        c.drawBitmap(input, (float)paddingLeft, (float)paddingTop, null);

        return result;
    }

    /**
     * Creates a border around an image to match the given dimensions
     * @param input Input {@link Bitmap}
     * @param newWidth New width of resulting image
     * @param newHeight New height of resulting image
     * @return Padded {@link Bitmap}
     */
    public Bitmap createPaddedBitmap(Bitmap input,
                                     int newWidth,
                                     int newHeight) {
        if(newWidth < input.getWidth() || newHeight < input.getHeight()) {
            throw new RuntimeException("Error! Padded image size has to be greater or equal to " +
                    "original image size.");
        }
        int offsetX = newWidth - input.getWidth();
        int offsetY = newHeight - input.getHeight();

        int left = offsetX/2;
        int top = offsetY/2;

        int right = ((input.getWidth() + 2*left) % 2 == 0) ? left : left + 1;
        int bottom = ((input.getHeight() + 2*top) % 2 == 0) ? top : top + 1;

        return createPaddedBitmap(input, left, top, right, bottom);
    }


    public Bitmap blurImage(Bitmap input,
                            int size,
                            double sigma) {
        GaussianBlur gaussianBlur = new GaussianBlur(size, sigma);
        return gaussianBlur.filter(input);
    }
}
