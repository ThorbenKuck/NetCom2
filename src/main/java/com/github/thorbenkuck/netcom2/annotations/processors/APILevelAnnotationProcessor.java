package com.github.thorbenkuck.netcom2.annotations.processors;

import com.github.thorbenkuck.keller.annotations.APILevel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;

/**
 * This AnnotationProcessor can be used, to process the APILevel Annotation, which should ensure that any Method annotated
 * with @{@link APILevel} is used only for internal development.
 */
public class APILevelAnnotationProcessor extends AbstractProcessor {

	private final static String ERROR_MESSAGE = "Methods annotated with @APILevel should not be public, private, protected or abstract!";
	private Messager messager;

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(APILevel.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		messager = processingEnv.getMessager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Processing ..");
		for (Element element : roundEnv.getElementsAnnotatedWith(APILevel.class)) {
			for (Modifier modifier : element.getModifiers()) {
				if (modifier.equals(Modifier.ABSTRACT)
						|| modifier.equals(Modifier.PRIVATE)
						|| modifier.equals(Modifier.PUBLIC)
						|| modifier.equals(Modifier.PROTECTED)) {
					messager.printMessage(Diagnostic.Kind.WARNING, ERROR_MESSAGE, element);
				}
			}
		}
		return true;
	}
}
