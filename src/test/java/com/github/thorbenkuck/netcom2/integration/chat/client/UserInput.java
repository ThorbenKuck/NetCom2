package com.github.thorbenkuck.netcom2.integration.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserInput {

	private BufferedReader bufferedReader;

	public UserInput() {
		bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	}

	public String getNextLine() throws IOException {
		return bufferedReader.readLine();
	}

}
