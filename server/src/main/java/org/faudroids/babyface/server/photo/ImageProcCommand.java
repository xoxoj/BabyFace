package org.faudroids.babyface.server.photo;

import org.faudroids.babyface.server.utils.Log;
import org.faudroids.babyface.server.utils.StreamGobbler;

import java.io.File;

public class ImageProcCommand {

	private final File photoFile;

	public ImageProcCommand(File photoFile) {
		this.photoFile = photoFile;
	}

	public boolean execute() throws Exception {
		String command = String.format("./../imgproc/detector/detector -p %s -o %s", photoFile.getAbsolutePath(), photoFile.getAbsolutePath());
		Log.i("executing " + command);

		// start process and save stdout + stderr
		Process process = Runtime.getRuntime().exec(command);
		new StreamGobbler("STDOUT", process.getInputStream(), System.out).start();
		new StreamGobbler("STDERR", process.getErrorStream(), System.err).start();
		int resultCode = process.waitFor();

		Log.i("done");
		return resultCode == 0;
	}

}
