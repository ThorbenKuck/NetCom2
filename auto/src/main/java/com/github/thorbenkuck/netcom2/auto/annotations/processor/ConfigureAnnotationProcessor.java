package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.annotations.Configure;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoService(Processor.class)
public class ConfigureAnnotationProcessor extends AnnotationProcessor {

	private static final Map<String, CodeBlock.Builder> SERVER_MAPPING;
	private static final Map<String, CodeBlock.Builder> CLIENT_MAPPING;

	static {
		final Map<String, CodeBlock.Builder> tmp = new HashMap<>();
		tmp.put(ServerStart.class.getSimpleName(), CodeBlock.builder()
				.addStatement("$T toUse = serverStart", ServerStart.class));
		tmp.put(CommunicationRegistration.class.getSimpleName(), CodeBlock.builder()
				.addStatement("$T toUse = serverStart.getCommunicationRegistration()", CommunicationRegistration.class));
		SERVER_MAPPING = Collections.unmodifiableMap(tmp);

		final Map<String, CodeBlock.Builder> tmp2 = new HashMap<>();
		tmp2.put(ClientStart.class.getSimpleName(), CodeBlock.builder()
				.addStatement("$T toUse = clientStart", ClientStart.class));
		tmp2.put(CommunicationRegistration.class.getSimpleName(), CodeBlock.builder()
				.addStatement("$T toUse = clientStart.getCommunicationRegistration()", CommunicationRegistration.class));
		CLIENT_MAPPING = Collections.unmodifiableMap(tmp2);
	}

	private ConfigureGenerator configureGenerator;

	@Override
	protected void pre() {
		configureGenerator = new ConfigureGenerator(filer);
	}

	@Override
	protected Class<? extends Annotation> supported() {
		return Configure.class;
	}

	@Override
	protected boolean process(Element element) {
		if (!(element instanceof ExecutableElement)) {
			return false;
		}

		ExecutableElement method = (ExecutableElement) element;
		List<? extends VariableElement> parameters = method.getParameters();

		if (parameters.size() != 1) {
			logger.error("Exactly one parameter has be provided. This parameter has to be reachable through the respective NetworkInterface", element);
			return false;
		}

		Configure annotation = element.getAnnotation(Configure.class);

		VariableElement parameter = parameters.get(0);
		TypeElement parameterType = (TypeElement) types.asElement(parameter.asType());
		String used = parameterType.getSimpleName().toString();

		CodeBlock.Builder serverCode = SERVER_MAPPING.get(used);
		CodeBlock.Builder clientCode = CLIENT_MAPPING.get(used);

		if (serverCode == null && clientCode == null) {
			logger.error("Sorry, but configuration of " + used + " is not supported at the moment..");
			return false;
		}

		if ((serverCode != null && annotation.forServer()) && (clientCode != null && annotation.forClient())) {
			if (!annotation.name().isEmpty()) {
				logger.error("Custom class names are only supported, if the scope is unambiguously.\n" +
						"In this case, we need to create two different classes for the ServerStart and the ClientStart, which is not possible with only one name.");
				return false;
			}
		}

		TypeElement typeElement = (TypeElement) method.getEnclosingElement();

		if (serverCode != null && annotation.forServer()) {
			configureGenerator.serverSide(serverCode, ConfigureGenerator.getName(method, typeElement, "Server"), typeElement, annotation, method);
		}

		if (clientCode != null && annotation.forClient()) {
			configureGenerator.clientSide(clientCode, ConfigureGenerator.getName(method, typeElement, "Client"), typeElement, annotation, method);
		}

		return true;
	}
}
