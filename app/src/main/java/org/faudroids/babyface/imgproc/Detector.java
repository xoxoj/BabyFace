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

	private final Context context;
    private final ImageProcessor imageProcessor;

	@Inject
    Detector(Context context, ImageProcessor imageProcessor) {
		this.context = context;
		this.imageProcessor = imageProcessor;
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
            int centerY = (int)face.getPosition().y + (int)face.getHeight()/2;
            int maxHeight = (centerY < (input.getHeight() - centerY)) ? centerY : input.getHeight
                    () - centerY;

            Rect roi = new Rect(centerX - maxWidth,
                    centerY - maxHeight,
                    centerX + maxWidth,
                    centerY + maxHeight);

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
        final Frame imageFrame = new Frame.Builder().setBitmap(inputImage).build();
		final FaceDetector detector = createDetector();

        final SparseArray<Face> faces = detector.detect(imageFrame);

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

		detector.release();
        return largestFace;
    }


	// TODO check this at some point
	/**
	 * @return if false this detector CANNOT be used!
	 */
	public boolean isOperational() {
		final FaceDetector detector = createDetector();
		final boolean isOperational = detector.isOperational();
		detector.release();
		return isOperational();
	}


	private FaceDetector createDetector() {
		return new FaceDetector.Builder(context)
				.setProminentFaceOnly(true) // there should only be one large face
				.setTrackingEnabled(false) // no need for live updates
				.setClassificationType(FaceDetector.NO_CLASSIFICATIONS) // no need for smiling etc.
				.setMode(FaceDetector.ACCURATE_MODE) // position is more important than speed
				.setLandmarkType(FaceDetector.NO_LANDMARKS) // eyes etc. position doesn't matter
				.build();
	}

}
