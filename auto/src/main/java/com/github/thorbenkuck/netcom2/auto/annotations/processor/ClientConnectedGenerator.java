package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.ClientConnectedWrapper;
import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

final class ClientConnectedGenerator {

	private static final String INNER_CLASS_NAME = "InnerClientConnectedHandler";
	private final Filer filer;

	ClientConnectedGenerator(Filer filer) {
		this.filer = filer;
	}

	private TypeSpec createInnerClass(ExecutableElement method, TypeElement type) {
		return TypeSpec.classBuilder(INNER_CLASS_NAME)
				.addSuperinterface(ClientConnectedHandler.class)
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

	void generate(TypeElement type, ExecutableElement method) {
		MethodSpec acceptMethod = MethodSpec.methodBuilder("apply")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addParameter(ServerStart.class, "serverStart", Modifier.FINAL)
				.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
				.addAnnotation(Override.class)
				.addCode(CodeBlock.builder()
						.addStatement("serverStart.addClientConnectedHandler(new $L(objectRepository))", INNER_CLASS_NAME)
						.build())
				.build();

		TypeSpec innerClass = createInnerClass(method, type);

		String name = method.getSimpleName().toString() + type.getSimpleName().toString() + "ClientConnectedHandler";

		TypeSpec.Builder builder = TypeSpec.classBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(TypeName.get(ClientConnectedWrapper.class))
				.addType(innerClass)
				.addAnnotation(Auto.annotation(ClientConnectedWrapper.class))
				.addMethod(acceptMethod);

		PackageElement packageElement = (PackageElement) type.getEnclosingElement();

		try {
			JavaFile.builder(packageElement.getQualifiedName().toString(), builder.build())
					.addFileComment("This file has been auto generated")
					.indent("	")
					.build().writeTo(filer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
