package com.github.thorbenkuck.netcom2.integration.example.inter;

import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.client.RemoteObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class RMIClient {

	private final ClientStart clientStart;

	public RMIClient(String address, int port) {
		this.clientStart = ClientStart.at(address, port);
	}

	public static void main(String[] args) {
		RMIClient client = new RMIClient("localhost", 4444);
		client.run();
	}

	public void run() {
		try {
			clientStart.launch();
		} catch (StartFailedException e) {
			e.printStackTrace();
			return;
		}

		RemoteObjectFactory factory = RemoteObjectFactory.open(clientStart);

		Internationalization languages = factory.create(Internationalization.class, new LocalInternationalization());

		final List<Language> languagesList = languages.getAvailableLanguages();
		final List<String> identifiers = languages.getAvailableIdentifier();

		System.out.println(languagesList);
		System.out.println(identifiers);

		for (String id : identifiers) {
			for (Language language : languagesList) {
				System.out.println(language + "(" + id + ") = " + languages.getInLanguage(id, language));
			}
		}
	}

	private class LocalInternationalization implements Internationalization {

		@Override
		public List<Language> getAvailableLanguages() {
			return new ArrayList<>();
		}

		@Override
		public List<String> getAvailableIdentifier() {
			return new ArrayList<>();
		}

		@Override
		public String getInLanguage(String id, Language language) {
			return "No connection to Server";
		}
	}
}
