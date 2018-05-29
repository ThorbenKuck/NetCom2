package com.github.thorbenkuck.netcom2.system.example.inter;

import java.util.List;

public interface Internationalization {

	List<Language> getAvailableLanguages();

	List<String> getAvailableIdentifier();

	String getInLanguage(String id, Language language);

}
