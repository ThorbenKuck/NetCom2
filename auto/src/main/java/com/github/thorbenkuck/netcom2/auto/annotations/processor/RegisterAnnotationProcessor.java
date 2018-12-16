package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoService(Processor.class)
public class RegisterAnnotationProcessor extends AnnotationProcessor {

	private TypeElement sessionElement;
	private TypeElement connectionContextElement;
	private ReceiveHandlerGenerator receiveHandlerGenerator;

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
	protected void pre() {
		sessionElement = elements.getTypeElement(Session.class.getCanonicalName());
		connectionContextElement = elements.getTypeElement(ConnectionContext.class.getCanonicalName());
		receiveHandlerGenerator = new ReceiveHandlerGenerator(filer);
	}

	@Override
	protected Class<? extends Annotation> supported() {
		return Register.class;
	}

	@Override
	protected boolean process(Element element) {
		if (!(element instanceof ExecutableElement)) {
			return false;
		}

		ExecutableElement method = (ExecutableElement) element;

		List<? extends VariableElement> parameters = method.getParameters();
		VariableElement type = classify(parameters);

		if (type == null) {
			logger.error("A method annotated with @Register has to have exactly one parameter that is not a Session nor a ConnectionContext", method);
			return false;
		}

		TypeElement typeElement = (TypeElement) method.getEnclosingElement();

		// At this point, all we need
		// to do is to generate the
		// corresponding implementation
		receiveHandlerGenerator.generate(method, typeElement, type);
		return true;
	}
}
