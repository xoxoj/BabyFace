package org.faudroids.babyface.server.video;

import org.faudroids.babyface.server.utils.Log;
import org.faudroids.babyface.server.utils.StreamGobbler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

public class FFmpegCommand {

	private final String imageTemplate;
	private final int imageDuration;
	private final int framerate;
	private final File outputFile, logFile, progressFile;

	public FFmpegCommand(
			File rootDirectory,
			String imageTemplate,
			int imageDuration,
			int framerate,
			String outputFileName,
			String logFileName,
			String progressFileName) {

		this.imageTemplate = rootDirectory.getAbsolutePath() + "/" + imageTemplate;
		this.imageDuration = imageDuration;
		this.framerate = framerate;
		this.outputFile = new File(rootDirectory.getAbsolutePath(), outputFileName);
		this.logFile = new File(rootDirectory, logFileName);
		this.progressFile = new File(rootDirectory, progressFileName);
	}

	public boolean execute() throws Exception {
		String command = String.format(
				"ffmpeg -progress %s -framerate 1/%d -i %s -c:v libx264 -r %d -pix_fmt yuv420p %s",
				progressFile.getAbsolutePath(),
				imageDuration,
				imageTemplate,
				framerate,
				outputFile.getAbsolutePath());

		Log.i("executing " + command);

		// start process and save stdout + stderr
		Process process = Runtime.getRuntime().exec(command);
		OutputStream logFileStream = new FileOutputStream(logFile);
		new StreamGobbler("STDOUT", process.getInputStream(), logFileStream).start();
		new StreamGobbler("STDERR", process.getErrorStream(), logFileStream).start();
		int resultCode = process.waitFor();

		Log.i("done");
		return resultCode == 0;
	}

	/**
	 * @param totalImageCount the number of images being converted to video
	 * @return the progress as a percentage (0% - 100%)
	 */
	public float getProgress(int totalImageCount) {
		if (!progressFile.exists()) return 0;

		// amount of frames in this video
		int totalFrames = totalImageCount * framerate * imageDuration;

		// read progress file
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(progressFile));
			long currentFrame = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				currentFrame = Integer.valueOf(line.substring(6)); // reads first line "frame=<number>"
				for (int i = 0; i < 9; ++i) reader.readLine(); // reads the 9 additional progress lines
			}
			return ((float) currentFrame) / totalFrames;

		} catch (IOException e) {
			Log.e("failed to read progress", e);
			return 0;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e("failed to close reader", e);
				}
			}
		}
	}


	public static class Builder {

		private final File rootDirectory;
		private final String imageTemplate;
		private int imageDuration = 1;
		private int framerate = 25;
		private String
				outputFileName = "out.mp4",
				logFileName = "logs",
				progressFileName = "progress";

		public Builder(File rootDirectory, String imageTemplate) {
			this.rootDirectory = rootDirectory;
			this.imageTemplate = imageTemplate;
		}

		public Builder setImageDuration(int imageDuration) {
			this.imageDuration = imageDuration;
			return this;
		}

		public Builder setFramerate(int framerate) {
			this.framerate = framerate;
			return this;
		}

		public Builder setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
			return this;
		}

		public Builder setLogFileName(String logFileName) {
			this.logFileName = logFileName;
			return this;
		}

		public Builder setProgressFileName(String progressFileName) {
			this.progressFileName = progressFileName;
			return this;
		}

		public FFmpegCommand build() {
			return new FFmpegCommand(rootDirectory, imageTemplate, imageDuration, framerate, outputFileName, logFileName, progressFileName);
		}

	}

}
