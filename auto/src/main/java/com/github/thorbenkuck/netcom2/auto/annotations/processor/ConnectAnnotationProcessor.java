package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.annotations.Connect;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

@AutoService(Processor.class)
public class ConnectAnnotationProcessor extends AnnotationProcessor {

	private TypeElement clientElement;
	private ClientConnectedGenerator generator;

	@Override
	protected void pre() {
		clientElement = elements.getTypeElement(Client.class.getCanonicalName());
		generator = new ClientConnectedGenerator(filer);
	}

	@Override
	protected Class<? extends Annotation> supported() {
		return Connect.class;
	}

	@Override
	protected boolean process(Element element) {
		if (!(element instanceof ExecutableElement)) {
			return false;
		}

		ExecutableElement method = (ExecutableElement) element;

		if (!ConnectVerification.verifyParameters(method, clientElement)) {
			logger.error("Client connected methods have to have exactly one argument of the Client type", method);
			return false;
		}

		TypeElement typeElement = (TypeElement) method.getEnclosingElement();

		generator.generate(typeElement, method);

		return true;
	}
}
