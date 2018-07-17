package com.github.thorbenkuck.netcom2.file;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileServer {

	public static void main(String[] args) {
		ServerStart serverStart = ServerStart.at(9999);

		serverStart.getCommunicationRegistration()
				.register(FileRequest.class)
				.addFirst(FileServer::writeFile);

		try {
			serverStart.launch();
			serverStart.acceptAllNextClients();
		} catch (StartFailedException | ClientConnectionFailedException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(Session session, FileRequest fileRequest) {
		try {
			System.out.println("Received FileRequest. Reading test.pdf");
			File file = new File("/home/thorben/Dropbox/test.pdf");
			if (!file.exists()) {
				throw new IllegalStateException("Could not find /home/thorben/Dropbox/test.pdf");
			}
			System.out.println("Fetching byte data");
			byte[] array = Files.readAllBytes(file.toPath());
			System.out.println("Sending FileResponse");
			session.send(new FileResponse(array));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
