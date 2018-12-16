package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AnnotationProcessor extends AbstractProcessor {

	private final List<Element> alreadyProcessed = new ArrayList<>();
	protected Types types;
	protected Elements elements;
	protected Filer filer;
	protected CompilationLogger logger;

	private List<? extends Element> filter(Set<? extends Element> input) {
		List<Element> value = new ArrayList<>();

		for (Element element : input) {
			if (!alreadyProcessed.contains(element)) {
				logger.log("Will process " + element.getSimpleName(), element);
				alreadyProcessed.add(element);
				value.add(element);
			}
		}

		return value;
	}

	protected abstract void pre();

	protected abstract Class<? extends Annotation> supported();

	protected abstract boolean process(Element element);

	protected void post(List<Element> handled) {
	}

	@Override
	public final Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<>();
		annotations.add(supported().getCanonicalName());
		return annotations;
	}

	@Override
	public final SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public final synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		logger = new CompilationLogger(processingEnv.getMessager());
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		pre();

		boolean error = false;
		List<? extends Element> toProcess = filter(roundEnv.getElementsAnnotatedWith(supported()));

		for (Element element : toProcess) {
			boolean success;
			try {
				success = process(element);
			} catch (Throwable throwable) {
				logger.error(throwable.getMessage(), element);
				throwable.printStackTrace();
				success = false;
			}

			if (!success) {
				error = true;
			}
		}

		post(alreadyProcessed);

		return !error;
	}
}
