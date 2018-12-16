package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class RegisterAnnotationProcessor extends AbstractProcessor {

	private final List<? super Element> alreadyProcessed = new ArrayList<>();
	private Types types;
	private Elements elements;
	private Filer filer;
	private CompilationLogger logger;
	private TypeElement sessionElement;
	private TypeElement connectionContextElement;

	private List<ExecutableElement> filter(Set<? extends Element> input) {
		List<ExecutableElement> value = new ArrayList<>();

		for (Element element : input) {
			if (!(element instanceof ExecutableElement)) {
				logger.warn("Found wrongly annotated element");
				continue;
			}
			ExecutableElement toUse = (ExecutableElement) element;
			if (!alreadyProcessed.contains(toUse)) {
				logger.log("Will process " + toUse.getSimpleName(), toUse);
				value.add(toUse);
			}
		}

		return value;
	}

	private VariableElement classify(List<? extends VariableElement> parameters) {
		VariableElement returnValue = null;

		if (parameters.size() > 3) {
			return null;
		}

		for (VariableElement variableElement : parameters) {
			System.out.println("checking " + variableElement.asType() + " against " + sessionElement + " and " + connectionContextElement);
			TypeMirror type = variableElement.asType();
			if (!type.equals(sessionElement.asType()) && !type.equals(connectionContextElement.asType())) {
				if (returnValue == null) {
					returnValue = variableElement;
				} else {
					return null;
				}
			} else {
				System.out.println("Is either Session or ConnectionContext");
			}
		}

		return returnValue;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<>();
		annotations.add(Register.class.getCanonicalName());
		return annotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		logger = new CompilationLogger(processingEnv.getMessager());

		sessionElement = elements.getTypeElement(Session.class.getCanonicalName());
		connectionContextElement = elements.getTypeElement(ConnectionContext.class.getCanonicalName());
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		boolean error = false;
		List<ExecutableElement> toProcess = filter(roundEnv.getElementsAnnotatedWith(Register.class));
		ReceiveHandlerGenerator receiveHandlerGenerator = new ReceiveHandlerGenerator(filer);

		for (ExecutableElement method : toProcess) {
			List<? extends VariableElement> parameters = method.getParameters();
			VariableElement type = classify(parameters);
			alreadyProcessed.add(method);

			if (type == null) {
				error = true;
				logger.error("A method annotated with @Register has to have exactly one parameter that is not a Session nor a ConnectionContext", method);
				continue;
			}

			TypeElement typeElement = (TypeElement) method.getEnclosingElement();

			// At this point, all we need
			// to do is to generate the
			// corresponding implementation
			receiveHandlerGenerator.generate(method.getSimpleName().toString() + typeElement.getSimpleName() + "ReceiveHandler", method, typeElement, type);
		}

		return error;
	}
}
