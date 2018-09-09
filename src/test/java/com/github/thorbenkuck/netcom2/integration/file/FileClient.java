package com.github.thorbenkuck.netcom2.integration.file;

import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.Sender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileClient {

	private static Synchronize synchronize = Synchronize.createDefault();

	public static void main(String[] args) {

		ClientStart clientStart = ClientStart.at("localhost", 9999);

		clientStart.getCommunicationRegistration()
				.register(FileResponse.class)
				.addFirst(FileClient::handleReceive);

		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		Sender sender = Sender.open(clientStart);

		sender.objectToServer(new FileRequest());

		try {
			synchronize.synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void handleReceive(FileResponse fileResponse) {
		System.out.println("Received FileResponse. Finding target file.");
		File file = new File("example.pdf");
		try {
			if (file.exists()) {
				System.out.println("File already exists. dropping");
				file.delete();
			}
			System.out.println("Creating target File");
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Opening OutputStream to file");
		try (FileOutputStream fileOutputStream = new FileOutputStream("example.pdf")) {
			System.out.println("Writing byte Data");
			fileOutputStream.write(fileResponse.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Releasing waiting Thread");

		synchronize.goOn();
	}

}
