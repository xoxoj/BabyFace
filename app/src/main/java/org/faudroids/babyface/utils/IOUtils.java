package org.faudroids.babyface.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper methods for dealing with IO.
 */
public class IOUtils {

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

}
