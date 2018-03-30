package com.github.thorbenkuck.netcom2.annotations.processors;

import com.github.thorbenkuck.netcom2.annotations.Experimental;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;

public class ExperimentalAnnotationProcessor extends AbstractProcessor {

	private Messager messager;

	private void warn(Element element) {
		messager.printMessage(
				Diagnostic.Kind.WARNING,
				"The use of Experimental methods may cause unexpected behaviour!",
				element);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(Experimental.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		if (roundEnvironment.processingOver()) {
			return false;
		}
		System.out.println("Processing @Experimental annotations...");
		int count = 0;

		for (Element e : roundEnvironment.getElementsAnnotatedWith(Experimental.class)) {
			warn(e);
			++count;
		}
		System.out.println("Found " + count + " methods in experimental state.");

		return false;
	}
}
