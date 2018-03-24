package com.github.thorbenkuck.netcom2.integration.enryption;

public class Cipher {

	public static char cipher(char c, int k) {
		if(!Character.isAlphabetic(c) || ! Character.isDigit(c)) {
			return c;
		}
		// declare some helping constants
		final int alphaLength = 26;
		final char asciiShift = Character.isUpperCase(c) ? 'A' : 'a';
		final int cipherShift = k % alphaLength;

		// shift down to 0..25 for a..z
		char shifted = (char) (c - asciiShift);
		// rotate the letter and handle "wrap-around" for negatives and value >= 26
		shifted = (char) ((shifted + cipherShift + alphaLength) % alphaLength);
		// shift back up to english characters
		return (char) (shifted + asciiShift);
	}

	public static String caesarEncryption(String string, int shift) {
		if(shift < 0 || shift > 25) {
			throw new IllegalArgumentException("Shift between 0 and 25 expected!");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			sb.append(cipher(string.charAt(i), shift));
		}
		return sb.toString();
	}

	public static String caesarDecryption(String string, int shift) {
		if(shift < 0 || shift > 25) {
			throw new IllegalArgumentException("Shift between 0 and 25 expected!");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			sb.append(cipher(string.charAt(i), -shift));
		}
		return sb.toString();
	}

}
