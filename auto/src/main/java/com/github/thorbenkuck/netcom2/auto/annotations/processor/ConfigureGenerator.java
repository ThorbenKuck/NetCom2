package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.ClientPreConfiguration;
import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.ServerPreConfiguration;
import com.github.thorbenkuck.netcom2.auto.annotations.Configure;
import com.github.thorbenkuck.netcom2.network.client.ClientStart;
import com.github.thorbenkuck.netcom2.network.server.ServerStart;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class ConfigureGenerator {

	private final Writer writer;

	ConfigureGenerator(Filer filer) {
		writer = new Writer(filer);
	}

	static String getName(ExecutableElement method, TypeElement clazz, String identifier) {
		Configure register = method.getAnnotation(Configure.class);

		String set = register.name();
		if (set.isEmpty()) {
			String methodName = method.getSimpleName().toString();
			return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + clazz.getSimpleName().toString() + identifier + "Configuration";
		} else {
			return set;
		}
	}

	private TypeSpec.Builder start(String className) {
		return TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
	}

	void serverSide(CodeBlock.Builder mainCode, String name, TypeElement type, Configure configure, ExecutableElement method) {
		TypeSpec.Builder typeSpec = start(name)
				.addMethod(MethodSpec.methodBuilder("accept")
						.addParameter(ServerStart.class, "serverStart", Modifier.FINAL)
						.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addCode(mainCode
								.addStatement("$T toExecuteOn = objectRepository.get($T.class)", ClassName.get(type), ClassName.get(type))
								.addStatement("toExecuteOn.$L(toUse)", method.getSimpleName())
								.build())
						.build())
				.addSuperinterface(ServerPreConfiguration.class);

		writer.write(typeSpec, type, ServerPreConfiguration.class, configure.autoLoad());
	}

	void clientSide(CodeBlock.Builder mainCode, String name, TypeElement type, Configure configure, ExecutableElement method) {
		TypeSpec.Builder typeSpec = start(name)
				.addMethod(MethodSpec.methodBuilder("accept")
						.addParameter(ClientStart.class, "clientStart", Modifier.FINAL)
						.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addCode(mainCode
								.addStatement("$T toExecuteOn = objectRepository.get($T.class)", ClassName.get(type), ClassName.get(type))
								.addStatement("toExecuteOn.$L(toUse)", method.getSimpleName())
								.build())
						.build())
				.addSuperinterface(ClientPreConfiguration.class);

		writer.write(typeSpec, type, ClientPreConfiguration.class, configure.autoLoad());
	}

}
