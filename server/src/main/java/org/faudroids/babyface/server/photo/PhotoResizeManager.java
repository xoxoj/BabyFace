package org.faudroids.babyface.server.photo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;

public class PhotoResizeManager {

	private static final Resolution TARGET_RESOLUTION = new Resolution(1920, 1080);

	@Inject
	PhotoResizeManager() { }


	public void resizeAndCropPhotos(List<File> photoFiles) throws IOException {
		for (File photoFile : photoFiles) {
			BufferedImage originalPhoto = ImageIO.read(photoFile);

			BufferedImage resizedPhoto = resizeAndCropImage(originalPhoto);
			ImageIO.write(resizedPhoto, "jpg", photoFile);
		}
	}


	private BufferedImage resizeAndCropImage(BufferedImage originalPhoto) {
		// get scale factors
		Resolution originalResolution = new Resolution(originalPhoto.getWidth(), originalPhoto.getHeight());
		float widthScale = ((float) originalResolution.width) / TARGET_RESOLUTION.width;
		float heightScale = ((float) originalResolution.height) / TARGET_RESOLUTION.height;

		// find non cropped resolution
		Resolution resizedResolution;
		if (widthScale > heightScale) {
			resizedResolution = new Resolution((int) (originalResolution.width / heightScale), TARGET_RESOLUTION.height);
		} else {
			resizedResolution = new Resolution(TARGET_RESOLUTION.width, (int) (originalResolution.height / widthScale));
		}

		// resize image
		BufferedImage resizedImage = new BufferedImage(resizedResolution.width, resizedResolution.height, originalPhoto.getType());
		Graphics2D graphics = resizedImage.createGraphics();
		graphics.drawImage(originalPhoto, 0, 0, resizedResolution.width, resizedResolution.height, null);
		graphics.dispose();

		// crop image (center)
		int widthOffset = (resizedResolution.width - TARGET_RESOLUTION.width) / 2;
		int heightOffset = (resizedResolution.height - TARGET_RESOLUTION.height) / 2;
		BufferedImage croppedImage = resizedImage.getSubimage(
				widthOffset,
				heightOffset,
				TARGET_RESOLUTION.width,
				TARGET_RESOLUTION.height);

		return croppedImage;
	}


	private static class Resolution {

		private final int width, height;

		public Resolution(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return width + " x " + height;
		}

	}
}
