package com.mohaine.brewcontroller.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

	public static byte[] readStream(InputStream is) throws IOException {
		byte[] toByteArray;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		writeStream(is, byteArrayOutputStream);
		toByteArray = byteArrayOutputStream.toByteArray();
		return toByteArray;
	}

	public static void writeStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[5000];
		while (true) {
			int readSize = is.read(buffer);
			if (readSize > 0) {
				os.write(buffer, 0, readSize);
			} else if (readSize < 0) {
				break;
			}
		}
	}

	public static void close(OutputStream os) {
		try {
			if (os != null) {
				os.close();
			}
		} catch (Throwable e) {
			// Ignore
		}
	}

	public static void close(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (Throwable e) {
			// Ignore
		}
	}

}
