package com.mohaine.brewcontroller.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

	public final static String getSHA1(File file) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return getDigest(fis, "SHA1");
		} finally {
			fis.close();
		}
	}

	public final static String getSHA1(InputStream fis) throws NoSuchAlgorithmException, IOException {
		return getDigest(fis, "SHA1");
	}

	private static String getDigest(InputStream fis, String algorithm) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		byte[] buffer = new byte[5000];
		while (true) {
			int read = fis.read(buffer);
			if (read > 0) {
				digest.update(buffer, 0, read);
			} else if (read < 0) {
				break;
			}
		}
		byte[] hash = digest.digest();
		StringBuffer hashStr = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			byte b = hash[i];
			hashStr.append(Integer.toHexString(0xFF & b));
		}
		return hashStr.toString();
	}

	public static String readFromFile(File file) throws IOException {
		return new String(readFileAsByteArray(file));

	}

	public static byte[] readFileAsByteArray(File file) throws FileNotFoundException, IOException {
		byte[] readStream;
		FileInputStream fis = new FileInputStream(file);
		try {
			readStream = StreamUtils.readStream(fis);
		} finally {
			fis.close();
		}
		return readStream;
	}

	public static void appendToFile(File file, String value) throws IOException {
		FileOutputStream fos = new FileOutputStream(file.getAbsolutePath(), true);
		try {
			fos.write(value.getBytes());
		} finally {
			fos.close();
		}
	}

	public static void writeToFile(String value, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(value.getBytes());
		} finally {
			fos.close();
		}
	}

}
