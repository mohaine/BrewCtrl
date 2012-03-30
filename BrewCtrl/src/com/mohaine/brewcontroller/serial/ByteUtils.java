/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.serial;

public class ByteUtils {

	private final boolean bigEndian;

	public static void print(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			System.out.println(b[i]);
		}
	}

	public void putChar(byte[] b, int off, char val) {
		if (isBigEndian()) {
			b[off + 1] = (byte) (val >>> 0);
			b[off + 0] = (byte) (val >>> 8);
		} else {
			b[off + 0] = (byte) (val >>> 0);
			b[off + 1] = (byte) (val >>> 8);
		}

	}

	public void putByte(byte[] b, int off, byte val) {
		b[off] = val;
	}

	public byte getByte(byte[] b, int off) {
		return b[off];
	}

	public void putShort(byte[] b, int off, short val) {
		if (isBigEndian()) {
			b[off + 1] = (byte) (val >>> 0);
			b[off + 0] = (byte) (val >>> 8);
		} else {
			b[off + 0] = (byte) (val >>> 0);
			b[off + 1] = (byte) (val >>> 8);
		}
	}

	public void putInt(byte[] b, int off, int val) {
		if (isBigEndian()) {
			b[off + 3] = (byte) (val >>> 0);
			b[off + 2] = (byte) (val >>> 8);
			b[off + 1] = (byte) (val >>> 16);
			b[off + 0] = (byte) (val >>> 24);
		} else {
			b[off + 0] = (byte) (val >>> 0);
			b[off + 1] = (byte) (val >>> 8);
			b[off + 2] = (byte) (val >>> 16);
			b[off + 3] = (byte) (val >>> 24);
		}
	}

	public void putFloat(byte[] b, int off, float val) {
		int i = Float.floatToIntBits(val);

		if (isBigEndian()) {
			b[off + 3] = (byte) (i >>> 0);
			b[off + 2] = (byte) (i >>> 8);
			b[off + 1] = (byte) (i >>> 16);
			b[off + 0] = (byte) (i >>> 24);
		} else {
			b[off + 0] = (byte) (i >>> 0);
			b[off + 1] = (byte) (i >>> 8);
			b[off + 2] = (byte) (i >>> 16);
			b[off + 3] = (byte) (i >>> 24);
		}
	}

	public void putLong(byte[] b, int off, long val) {

		if (isBigEndian()) {
			b[off + 7] = (byte) (val >>> 0);
			b[off + 6] = (byte) (val >>> 8);
			b[off + 5] = (byte) (val >>> 16);
			b[off + 4] = (byte) (val >>> 24);
			b[off + 3] = (byte) (val >>> 32);
			b[off + 2] = (byte) (val >>> 40);
			b[off + 1] = (byte) (val >>> 48);
			b[off + 0] = (byte) (val >>> 56);
		} else {
			b[off + 0] = (byte) (val >>> 0);
			b[off + 1] = (byte) (val >>> 8);
			b[off + 2] = (byte) (val >>> 16);
			b[off + 3] = (byte) (val >>> 24);
			b[off + 4] = (byte) (val >>> 32);
			b[off + 5] = (byte) (val >>> 40);
			b[off + 6] = (byte) (val >>> 48);
			b[off + 7] = (byte) (val >>> 56);
		}

	}

	public void putDouble(byte[] b, int off, double val) {
		long j = Double.doubleToLongBits(val);

		if (isBigEndian()) {
			b[off + 7] = (byte) (j >>> 0);
			b[off + 6] = (byte) (j >>> 8);
			b[off + 5] = (byte) (j >>> 16);
			b[off + 4] = (byte) (j >>> 24);
			b[off + 3] = (byte) (j >>> 32);
			b[off + 2] = (byte) (j >>> 40);
			b[off + 1] = (byte) (j >>> 48);
			b[off + 0] = (byte) (j >>> 56);
		} else {
			b[off + 0] = (byte) (j >>> 0);
			b[off + 1] = (byte) (j >>> 8);
			b[off + 2] = (byte) (j >>> 16);
			b[off + 3] = (byte) (j >>> 24);
			b[off + 4] = (byte) (j >>> 32);
			b[off + 5] = (byte) (j >>> 40);
			b[off + 6] = (byte) (j >>> 48);
			b[off + 7] = (byte) (j >>> 56);
		}

	}

	public boolean getBoolean(byte[] b, int off) {
		return b[off] != 0;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public char getChar(byte[] b, int off) {
		if (isBigEndian()) {
			return (char) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
		} else {
			return (char) (((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8));
		}
	}

	public short getShort(byte[] b, int off) {
		if (isBigEndian()) {
			return (short) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
		} else {
			return (short) (((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8));
		}
	}

	public int getInt(byte[] b, int off) {
		if (isBigEndian()) {
			return ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF) << 24);
		} else {
			return ((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8) + ((b[off + 2] & 0xFF) << 16) + ((b[off + 3] & 0xFF) << 24);
		}
	}

	public long getLong(byte[] b, int off) {

		if (isBigEndian()) {
			return ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24) + ((b[off + 3] & 0xFFL) << 32)
					+ ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
		} else {
			return ((b[off + 0] & 0xFFL) << 0) + ((b[off + 1] & 0xFFL) << 8) + ((b[off + 2] & 0xFFL) << 16) + ((b[off + 3] & 0xFFL) << 24) + ((b[off + 4] & 0xFFL) << 32)
					+ ((b[off + 5] & 0xFFL) << 40) + ((b[off + 6] & 0xFFL) << 48) + ((b[off + 7] & 0xFFL) << 56);
		}

	}

	public float getFloat(byte[] b, int off) {
		int i;
		if (isBigEndian()) {
			i = ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF) << 24);
		} else {
			i = ((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8) + ((b[off + 2] & 0xFF) << 16) + ((b[off + 3] & 0xFF) << 24);
		}
		return Float.intBitsToFloat(i);

	}

	public double getDouble(byte[] b, int off) {
		long j;

		if (isBigEndian()) {
			j = ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24) + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40)
					+ ((b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
		} else {
			j = ((b[off + 0] & 0xFFL) << 0) + ((b[off + 1] & 0xFFL) << 8) + ((b[off + 2] & 0xFFL) << 16) + ((b[off + 3] & 0xFFL) << 24) + ((b[off + 4] & 0xFFL) << 32) + ((b[off + 5] & 0xFFL) << 40)
					+ ((b[off + 6] & 0xFFL) << 48) + ((b[off + 7] & 0xFFL) << 56);
		}

		return Double.longBitsToDouble(j);
	}

	public ByteUtils(boolean endian) {
		bigEndian = endian;
	}

	public ByteUtils() {
		bigEndian = false;
	}

}
