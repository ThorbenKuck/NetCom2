package com.github.thorbenkuck.netcom2.annotations.processors;

import com.github.thorbenkuck.netcom2.annotations.Exposed;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * This AnnotationProcessor can be used, to process the Exposed Annotation, which should ensure that any Method annotated
 * with @{@link Exposed} is used only for internal development.
 */
@SupportedAnnotationTypes("com.github.thorbenkuck.netcom2.annotations.Exposed")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExposedAnnotationProcessor extends AbstractProcessor {

	private final static String ERROR_MESSAGE =
			"Methods annotated with @Exposed should not be public, private, protected or abstract!";

	/**
	 * {@inheritDoc}
	 *
	 * @param annotations
	 * @param roundEnv
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(Exposed.class);
		System.out.println("Processing ..");
		for (Element element : root) {
			for (Modifier modifier : element.getModifiers()) {
				if (modifier.equals(Modifier.ABSTRACT)
						|| modifier.equals(Modifier.PRIVATE)
						|| modifier.equals(Modifier.PUBLIC)
						|| modifier.equals(Modifier.PROTECTED)) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ERROR_MESSAGE, element);
					return false;
				}
			}
		}

		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ERROR_MESSAGE, null);
		return true;
	}
}
