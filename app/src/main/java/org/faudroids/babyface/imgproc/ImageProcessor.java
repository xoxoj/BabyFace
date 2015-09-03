package org.faudroids.babyface.imgproc;

import android.graphics.Bitmap;
import android.graphics.Rect;

import timber.log.Timber;

public class ImageProcessor {
    public ImageProcessor() {

    }


    /**
     * Scales a whole bitmap to given {@link Size}. If one of the {@link Size} parameters is <= 0,
     * the image will be scaled preserving the aspect ratio
     * @param input Input {@link Bitmap}
     * @param newSize {@link Size} after scaling
     * @return Scaled {@link Bitmap}
     */
    public Bitmap scaleImage(Bitmap input, Size newSize) {
        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return input;
        } else if(newSize.height() <= 0) {
            double scaleFactor = input.getHeight()/input.getWidth();

            int newHeight = (int)(newSize.width() * scaleFactor);

            return Bitmap.createScaledBitmap(input, newSize.width(), newHeight, true);
        } else if(newSize.width() <= 0) {
            double scaleFactor = input.getWidth()/input.getHeight();

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
        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return input;
        } else if(newSize.height() <= 0) {
            int newWidth = (int)(input.getWidth() * (double)(newSize.width()/roi.width()));
            int newHeight = (int)(input.getHeight() * (double)(input.getHeight()/input
                    .getWidth()));

            return Bitmap.createScaledBitmap(input, newWidth, newHeight, true);
        } else if(newSize.width() <= 0) {
            int newHeight = (int)(input.getHeight() * (double)(newSize.height()/roi.height()));
            int newWidth = (int)(input.getWidth() * (double)(input.getWidth()/input
                    .getHeight()));

            return Bitmap.createScaledBitmap(input, newWidth, newHeight, true);
        } else {
            int newWidth = (int)(input.getWidth() * (double)(newSize.width() / roi.width()));
            int newHeight = (int)(input.getHeight() * (double)(newSize.height() / roi.height()));

            return Bitmap.createScaledBitmap(input, newWidth, newHeight, true);
        }
    }


    /**
     * Scales a {@link Rect} to a given {@link Size}. If one of the parameters of the {@link
     * Size} argument is <= 0 the {@link Rect} object is scaled preserving its aspect ratio
     * @param roi {@link Rect} to scale
     * @param newSize {@link Size} specifying the new size
     * @return Scaled {@link Rect}
     */
    public Rect scaleROI(Rect roi, Size newSize) {
        if(newSize.height() <= 0 && newSize.width() <= 0) {
            return roi;
        } else if(newSize.height() <= 0) {
            double scaleFactor = roi.height()/roi.width();

            int newHeight = (int)(roi.height() * scaleFactor);
            int offsetY = newHeight/2;
            int offsetX = newSize.width()/2;

            return new Rect(roi.centerX() - offsetX, roi.centerY() - offsetY, roi.centerX() +
                    offsetX, roi.centerY() + offsetY);
        } else if(newSize.width() <= 0) {
            double scaleFactor = roi.width()/roi.height();

            int newWidth = (int)(roi.width() * scaleFactor);
            int offsetX = newSize.height()/2;
            int offsetY = newWidth/2;

            return new Rect(roi.centerX() - offsetX, roi.centerY() - offsetY, roi.centerX() +
                    offsetX, roi.centerY() + offsetY);
        } else {
            int offsetX = newSize.width()/2;
            int offsetY = newSize.height()/2;

            return new Rect(roi.centerX() - offsetX, roi.centerY() - offsetY, roi.centerX() +
                    offsetX, roi.centerY() + offsetY);
        }
    }


    /**
     * Crops a {@link Bitmap} to a region of interest given as {@link Rect}
     * @param input {@link Bitmap} to crop
     * @param roi {@link Rect} specifying the ROI
     * @return Cropped @{@link Bitmap}
     */
    public Bitmap cropImage(Bitmap input, Rect roi) {
        int left, top, width, height;

        left = (roi.left < 0) ? 0 : roi.left;
        top = (roi.top < 0)  ? 0 : roi.top;
        width = (roi.right > input.getWidth()) ? input.getWidth() - left : roi.right - left;
        height = (roi.bottom > input.getHeight()) ? input.getHeight() - top : roi.bottom - top;

        return Bitmap.createBitmap(input, left, top, width, height);
    }
}
