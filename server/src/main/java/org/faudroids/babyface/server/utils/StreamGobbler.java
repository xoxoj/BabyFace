package org.faudroids.babyface.server.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class StreamGobbler extends Thread {

	private final String type;
	private final InputStream inStream;
	private final OutputStream outStream;

	public StreamGobbler(String type, InputStream inStream, OutputStream outStream) {
		this.type = type;
		this.inStream = inStream;
		this.outStream = outStream;
	}

	@Override
	public void run() {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outStream));
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				writer.println(type + ": " + line);
			}
		} catch (IOException ioe) {
			Log.e("failed to read stream " + type, ioe);
		} finally {
			writer.close();
		}
	}
}
