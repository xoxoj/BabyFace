package org.faudroids.babyface.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.roboguice.shaded.goole.common.base.Optional;

import javax.inject.Inject;

import timber.log.Timber;

public class PhotoProcessor {

	private static final Paint
			DEFAULT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG),
			BLACK_PAINT = new Paint();

	static {
		BLACK_PAINT.setColor(Color.BLACK);
	}

	public static final int
			OUTPUT_WIDTH = 1920,
			OUTPUT_HEIGHT = 1080;

	private static final float
			FACE_OUTPUT_HEIGHT = 0.8f; // many many % of the final image height the face should cover

	private final Context context;

	@Inject
	PhotoProcessor(Context context) {
		this.context = context;
    }


    /**
     * Detects the largest face in a {@link Bitmap} and returns a scaled, cropped and padded
     * {@link Bitmap} with 1920x1080 pixels
     * @param input {@link Bitmap} to process
     */
    public Bitmap findFaceAndCrop(Bitmap input) {
        Optional<Face> face = findLargestFace(input);

		// setup canvas and draw black background
		final Bitmap output = Bitmap.createBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT, Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(output);
		canvas.drawRect(0, 0, OUTPUT_WIDTH, OUTPUT_HEIGHT, BLACK_PAINT);

		// get target position of face
		float faceScale;
		int centerX, centerY;

		if (face.isPresent()) {
			faceScale = FACE_OUTPUT_HEIGHT * OUTPUT_HEIGHT / face.get().getHeight(); // how much the face should be scaled
			centerX = (int) (face.get().getPosition().x + face.get().getWidth() / 2.0f);
			centerY = (int) (face.get().getPosition().y + face.get().getHeight() / 2.0f);
		} else {
			faceScale = OUTPUT_HEIGHT / (float) input.getHeight();
			centerX = input.getWidth() / 2;
			centerY = input.getHeight() / 2;
		}

		// scale + translate face
		Matrix matrix = new Matrix();
		matrix.postScale(faceScale, faceScale);
		matrix.postTranslate(OUTPUT_WIDTH / 2 - centerX * faceScale, OUTPUT_HEIGHT / 2 - centerY * faceScale);
		canvas.drawBitmap(input, matrix, DEFAULT_PAINT);

		return output;
    }


    private Optional<Face> findLargestFace(Bitmap inputImage) {
		final FaceDetector detector = createDetector();
        final SparseArray<Face> faces = detector.detect(new Frame.Builder().setBitmap(inputImage).build());

        Face largestFace = null;
		float largestSize = 0f;
		Timber.d("found " + faces.size() + " faces in photo");

        for (int i = 0; i < faces.size(); ++i) {
            final Face face = faces.valueAt(i);
			final float faceSize = face.getHeight() * face.getWidth();
			if (faceSize > largestSize) {
				largestFace = face;
				largestSize = faceSize;
			}
        }

		detector.release();
		return Optional.fromNullable(largestFace);
    }


	/**
	 * @return if false this detector CANNOT be used!
	 */
	public boolean isOperational() {
		final FaceDetector detector = createDetector();
		final boolean isOperational = detector.isOperational();
		detector.release();
		return isOperational;
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
