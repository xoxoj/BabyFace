package org.faudroids.babyface.utils;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

/**
 * Helper methods for dealing with IO.
 */
public class IOUtils {

	/**
	 * Copies everything form the input stream to the output stream (didn't see that coming,
	 * did you?).
	 */
	public void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		} finally {
			inStream.close();
			outStream.close();
		}
	}


	/**
	 * Creates a directory if necessary.
	 * @return the same dir
	 */
	public File assertDir(File dir) {
		if (!dir.exists() && !dir.mkdirs()) {
			Timber.e("failed to make dir " + dir.getAbsolutePath());
		}
		return dir;
	}


	/**
	 * Deletes the file (recursively if it is a directory).
	 */
	public void delete(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				delete(child);
			}
		}
		if (!file.delete()) Timber.e("failed to delete " + file.getAbsolutePath());
	}


	/**
	 * Close that closeable!
	 */
	public void close(Closeable closeable) {
		try {
			if (closeable != null) closeable.close();
		} catch (IOException e) {
			Timber.e(e, "failed to close stream");
		}
	}


	/**
	 * Returns the directory formatted as a tree.
	 */
	public String tree(File file) {
		StringBuilder builder = new StringBuilder();
		tree(builder, file, "");
		return builder.toString();
	}


	private void tree(StringBuilder builder, File file, String prefix) {
		builder.append(prefix).append(file.getName()).append('\n');
		if (file.isDirectory()) {
			prefix = "   " + prefix;
			for (File child : file.listFiles()) {
				tree(builder, child, prefix);
			}
		}
	}

}
