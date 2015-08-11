package org.faudroids.babyface.server.video;

import org.faudroids.babyface.server.utils.Log;
import org.faudroids.babyface.server.utils.StreamGobbler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FFmpegCommand {

	private final String imageTemplate;
	private final int imageDuration;
	private final int framerate;
	private final String outputFileName;

	public FFmpegCommand(String imageTemplate, int imageDuration, int framerate, String outputFileName) {
		this.imageTemplate = imageTemplate;
		this.imageDuration = imageDuration;
		this.framerate = framerate;
		this.outputFileName = outputFileName;
	}

	public boolean execute(File logFile) throws Exception {
		String command = String.format(
				"ffmpeg -framerate 1/%d -i %s -c:v libx264 -r %d -pix_fmt yuv420p %s",
				imageDuration,
				imageTemplate,
				framerate,
				outputFileName);

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

}
