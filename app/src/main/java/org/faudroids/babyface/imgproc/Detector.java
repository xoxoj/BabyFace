package org.faudroids.babyface.imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import javax.inject.Inject;

import timber.log.Timber;

public class Detector {

    private final FaceDetector detector;
    private final ImageProcessor imageProcessor;

	@Inject
    Detector(Context ctx) {
        this.detector = new FaceDetector.Builder(ctx)
				.setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
				.setMode(FaceDetector.ACCURATE_MODE)
				.build();
		// TODO check if detector is available
		if (!detector.isOperational()) Timber.w("face processor is not operational!");
        this.imageProcessor = new ImageProcessor();
    }

    /**
     * Detects the largest face in a {@link Bitmap} and returns a scaled, cropped and padded
     * {@link Bitmap} with 1920x1080 pixels
     * @param input {@link Bitmap} to process
     * @return {@link Bitmap} containing the largest face in an image
     */
    public Bitmap process(Bitmap input) {
        Face face = findFace(input);
        if(face != null) {
            int centerX = (int)face.getPosition().x + (int)face.getWidth()/2;
            int maxWidth = (centerX < (input.getWidth() - centerX)) ? centerX : input.getWidth()
                    - centerX;
            Rect roi = new Rect(centerX - maxWidth,
                    (int) face.getPosition().y,
                    centerX + maxWidth,
                    (int) face.getPosition().y + (int) face.getHeight());

            Bitmap croppedInput = this.imageProcessor.cropImage(input, roi);

            Bitmap scaledInput;
            if (croppedInput.getWidth() > croppedInput.getHeight()) {
                scaledInput = this.imageProcessor.scaleImage(croppedInput, new Size(1920, 0));
            } else {
                scaledInput = this.imageProcessor.scaleImage(croppedInput, new Size(0, 1080));
            }

            return this.imageProcessor.createPaddedBitmap(scaledInput, 1920, 1080);
        } else {
            return input;
        }
    }

    private Face findFace(Bitmap inputImage) {
        Frame imageFrame = new Frame.Builder().setBitmap(inputImage).build();
        SparseArray<Face> faces = this.detector.detect(imageFrame);
        Face largestFace = null;
        Size maxSize = new Size(0, 0);
		Timber.d("found " + faces.size() + " faces in photo");

        for(int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            if(face.getWidth() > maxSize.width() && face.getHeight() > maxSize.height()) {
                largestFace = face;
                maxSize.width((int)face.getWidth());
                maxSize.height((int) face.getHeight());
            }
        }

        return largestFace;
    }
}
