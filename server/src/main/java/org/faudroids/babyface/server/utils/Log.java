package org.faudroids.babyface.server.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

	private Log() {
	}


	public static void d(String msg) {
		getLogger().debug(msg);
	}


	public static void d(String msg, Throwable throwable) {
		getLogger().debug(msg, throwable);
	}


	public static void i(String msg) {
		getLogger().info(msg);
	}


	public static void i(String msg, Throwable throwable) {
		getLogger().info(msg, throwable);
	}


	public static void w(String msg) {
		getLogger().warn(msg);
	}


	public static void w(String msg, Throwable throwable) {
		getLogger().warn(msg, throwable);
	}


	public static void e(String msg) {
		getLogger().error(msg);
	}


	public static void e(String msg, Throwable throwable) {
		getLogger().error(msg, throwable);
	}


	private static Logger getLogger() {
		StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
		return LoggerFactory.getLogger(stackElements[3].getClassName());
	}

}
