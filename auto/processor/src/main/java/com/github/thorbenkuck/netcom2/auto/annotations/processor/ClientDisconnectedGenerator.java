package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.ClientDisconnectedWrapper;
import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.annotations.Disconnect;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class ClientDisconnectedGenerator {

	private static final String INNER_CLASS_NAME = "InnerDisconnectedHandler";
	private final Writer writer;

	ClientDisconnectedGenerator(Filer filer) {
		this.writer = new Writer(filer);
	}

	private TypeSpec createInnerClass(ExecutableElement method, TypeElement type) {
		return TypeSpec.classBuilder(INNER_CLASS_NAME)
				.addModifiers(Modifier.PRIVATE)
				.addSuperinterface(ClientDisconnectedHandler.class)
				.addField(ObjectRepository.class, "objectRepository", Modifier.PRIVATE, Modifier.FINAL)
				.addMethod(MethodSpec.constructorBuilder()
						.addParameter(ObjectRepository.class, "objectRepository")
						.addCode(CodeBlock.builder().addStatement("this.objectRepository = objectRepository").build())
						.build())
				.addMethod(MethodSpec.methodBuilder("accept")
						.addAnnotation(Override.class)
						.addParameter(Client.class, "client", Modifier.FINAL)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addCode(CodeBlock.builder()
								.addStatement("$T toExecuteOn = objectRepository.get($T.class)", TypeName.get(type.asType()), TypeName.get(type.asType()))
								.addStatement("toExecuteOn.$L(client)", method.getSimpleName())
								.build())
						.build())
				.build();
	}

	private String getName(ExecutableElement method, TypeElement clazz) {
		Disconnect annotation = method.getAnnotation(Disconnect.class);

		String set = annotation.name();
		if (set.isEmpty()) {
			String methodName = method.getSimpleName().toString();
			return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + clazz.getSimpleName().toString() + "ClientConnectedHandler";
		} else {
			return set;
		}
	}

	private CodeBlock generateClientHandler(Disconnect annotation) {
		CodeBlock.Builder builder = CodeBlock.builder();

		if (annotation.forClient()) {
			builder.addStatement("clientStart.addDisconnectedHandler(new $L(objectRepository))", INNER_CLASS_NAME);
		}

		return builder.build();
	}

	private CodeBlock generateServerHandler(Disconnect annotation) {
		CodeBlock.Builder builder = CodeBlock.builder();

		if (annotation.forServer()) {
			builder.addStatement("serverStart.addClientConnectedHandler(client -> client.addDisconnectedHandler(new $L(objectRepository)))", INNER_CLASS_NAME);
		}

		return builder.build();
	}

	void generate(TypeElement type, ExecutableElement method) {
		Disconnect annotation = method.getAnnotation(Disconnect.class);

		MethodSpec applyClientMethod = MethodSpec.methodBuilder("apply")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addParameter(ClientStart.class, "clientStart", Modifier.FINAL)
				.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
				.addAnnotation(Override.class)
				.addCode(generateClientHandler(annotation))
				.build();

		MethodSpec applyServerMethod = MethodSpec.methodBuilder("apply")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addParameter(ServerStart.class, "serverStart", Modifier.FINAL)
				.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
				.addAnnotation(Override.class)
				.addCode(generateServerHandler(annotation))
				.build();

		TypeSpec innerClass = createInnerClass(method, type);

		String name = getName(method, type);

		TypeSpec.Builder builder = TypeSpec.classBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(TypeName.get(ClientDisconnectedWrapper.class))
				.addType(innerClass)
				.addMethod(applyClientMethod)
				.addMethod(applyServerMethod);

		writer.write(builder, type, ClientDisconnectedWrapper.class, annotation.autoLoad());
	}

}
