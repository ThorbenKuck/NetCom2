package com.github.thorbenkuck.netcom2.system.example.inter;

import java.util.*;

public class ServerLanguageTable {

	private static final Language GERMAN = new Language("de", "Deutsch");
	private static final Language ENGLISH = new Language("en", "English");
	private static final List<Language> languages = Arrays.asList(GERMAN, ENGLISH);
	private static final List<String> identifier = new ArrayList<>();
	private static final Map<String, Map<Language, String>> table;
	private static final String UNKNOWN_WORD = "UNKNOWN";
	private static final String UNKNOWN_LANGUAGE = "UNKNOWN_LANGUAGE";

	static {
		table = new HashMap<>();
		Map<Language, String> greet = new HashMap<>();
		greet.put(GERMAN, "Hallo");
		greet.put(ENGLISH, "Hello");
		table.put("greet", greet);

		Map<Language, String> loggedIn = new HashMap<>();
		loggedIn.put(GERMAN, "Sie haben sich erfolgreich angemeldet!");
		loggedIn.put(ENGLISH, "You successfully logged in!");
		table.put("loggedIn", loggedIn);

		identifier.add("greet");
		identifier.add("loggedIn");
	}

	public List<Language> getAllAvailableLanguages() {
		return languages;
	}

	public String lookUp(String textIdentifier, Language language) {
		Map<Language, String> text = table.get(textIdentifier);
		if (text == null) {
			return UNKNOWN_WORD;
		}

		return text.getOrDefault(language, UNKNOWN_LANGUAGE);
	}

	public List<String> getAvailableIdentifier() {
		return new ArrayList<>(identifier);
	}
}
