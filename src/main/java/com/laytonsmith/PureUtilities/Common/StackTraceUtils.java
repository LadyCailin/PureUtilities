package com.laytonsmith.PureUtilities.Common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 *
 */
public final class StackTraceUtils {

	private StackTraceUtils() {
	}

	/**
	 * Gets the stacktrace that would be printed out when doing {@link Throwable#printStackTrace()} as a String.
	 * @param t
	 * @return 
	 */
	public static String GetStacktrace(Throwable t) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		boolean first = true;
		Throwable tt = t;
		do {
			if(!first) {
				printWriter.append("Caused by: ");
			}
			first = false;
			tt.printStackTrace(printWriter);
		} while((tt = tt.getCause()) != null);
		return result.toString();
	}

	/**
	 * Gets the class that called the method above the one making this method call. The implementation of this
	 * method accounts for the fact that it is itself being called from the method that called it.
	 * @return 
	 */
	public static Class<?> getCallingClass() {
		try {
			// This is the class that called us. Calls may bounce around that class,
			// but we ultimately want to return that class that called the original
			// method within this class.
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			String doNotReturn = st[2].getClassName();
			for(int i = 3; i < st.length; i++) {
				if(!st[i].getClassName().equals(doNotReturn)) {
					return Class.forName(st[i].getClassName());
				}
			}
			// The only way this can get here is if this were the bootstrap class.
			// I doubt the JVM is calling us first thing, so just throw an Error.
			throw new Error();
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}
}
