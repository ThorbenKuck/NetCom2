package com.github.thorbenkuck.netcom2.integration;

import org.junit.Before;

import java.util.concurrent.Semaphore;

public abstract class IntegrationTest {

	private int prefixDepth = 1;
	protected static Class<?> testType;
	private static boolean informed = false;
	private static final Semaphore informLock = new Semaphore(1);

	private String constructPrefix() {
		StringBuilder stringBuilder = new StringBuilder();

		if (prefixDepth == 0) {
			return "";
		}

		for (int i = 0; i < prefixDepth; i++) {
			stringBuilder.append(" #");
		}
		stringBuilder.append(" ");
		return stringBuilder.toString();
	}

	protected void printRaw(String s) {
		System.out.println(s);
	}

	protected void print(String s) {
		printRaw(constructPrefix() + s);
	}

	@Before
	public void inform() {
		try {
			informLock.acquire();
			if (informed) {
				return;
			}
			printRaw("Starting the " + testType.getSimpleName());
			printRaw("This Test is an Integration-Test. This means, you may encounter unexpected behaviour");
			printRaw("In particular, this Test requires ports to be accessible.");
			printRaw("#############################");
			printRaw("# NOT MEANT FOR AUTOMATION! #");
			printRaw("#############################\n\n");
			informed = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			informLock.release();
		}
	}
}