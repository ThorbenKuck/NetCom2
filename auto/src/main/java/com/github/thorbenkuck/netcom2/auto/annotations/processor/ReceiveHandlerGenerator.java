package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.github.thorbenkuck.netcom2.auto.ObjectRepository;
import com.github.thorbenkuck.netcom2.auto.OnReceiveWrapper;
import com.github.thorbenkuck.netcom2.auto.annotations.Register;
import com.github.thorbenkuck.netcom2.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

final class ReceiveHandlerGenerator {

	private final Writer writer;

	ReceiveHandlerGenerator(Filer filer) {
		this.writer = new Writer(filer);
	}

	private CodeBlock invoke(ExecutableElement method, TypeElement clazz) {
		// TODO Last thing
		List<? extends VariableElement> parameters = method.getParameters();
		CodeBlock.Builder builder = CodeBlock.builder()
				.addStatement("$T toExecuteOn = objectRepository.get($T.class)", TypeName.get(clazz.asType()), TypeName.get(clazz.asType()));

		if (parameters.size() == 1) {
			builder.addStatement("toExecuteOn.$L(variable)", method.getSimpleName());
		} else if (parameters.size() == 2) {
			builder.addStatement("toExecuteOn.$L(session, variable)", method.getSimpleName());
		} else if (parameters.size() == 3) {
			builder.addStatement("toExecuteOn.$L(connectionContext, session, variable)", method.getSimpleName());
		} else {
			builder.addStatement("throw new $T($S)", IllegalStateException.class, "Illegal arrangements");
		}

		return builder.build();
	}

	private TypeSpec createInnerClass(VariableElement parameter, ExecutableElement method, TypeElement clazz) {
		return TypeSpec.classBuilder("InnerOnReceiveTriple")
				.addSuperinterface(ParameterizedTypeName.get(ClassName.get(OnReceiveTriple.class), TypeName.get(parameter.asType())))
				.addField(ObjectRepository.class, "objectRepository", Modifier.PRIVATE, Modifier.FINAL)
				.addMethod(MethodSpec.constructorBuilder()
						.addParameter(ObjectRepository.class, "objectRepository")
						.addCode(CodeBlock.builder().addStatement("this.objectRepository = objectRepository").build())
						.build())
				.addMethod(MethodSpec.methodBuilder("accept")
						.addAnnotation(Override.class)
						.addParameter(ConnectionContext.class, "connectionContext", Modifier.FINAL)
						.addParameter(Session.class, "session", Modifier.FINAL)
						.addParameter(TypeName.get(parameter.asType()), "variable", Modifier.FINAL)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addCode(invoke(method, clazz))
						.build())
				.build();
	}

	private String getName(ExecutableElement method, TypeElement clazz) {
		Register register = method.getAnnotation(Register.class);

		String set = register.name();
		if (set.isEmpty()) {
			String methodName = method.getSimpleName().toString();
			return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + clazz.getSimpleName() + "ReceiveHandler";
		} else {
			return set;
		}
	}

	void generate(ExecutableElement method, TypeElement clazz, VariableElement parameter) {
		Register annotation = method.getAnnotation(Register.class);

		MethodSpec acceptMethod = MethodSpec.methodBuilder("apply")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addParameter(NetworkInterface.class, "networkInterface", Modifier.FINAL)
				.addParameter(ObjectRepository.class, "objectRepository", Modifier.FINAL)
				.addAnnotation(Override.class)
				.addCode(CodeBlock.builder()
						.addStatement("$T communicationRegistration = networkInterface.getCommunicationRegistration()", CommunicationRegistration.class)
						.addStatement("communicationRegistration.register($T.class).addFirst(new $L(objectRepository))", parameter, "InnerOnReceiveTriple")
						.build())
				.build();

		TypeSpec onReceive = createInnerClass(parameter, method, clazz);

		String name = getName(method, clazz);

		TypeSpec.Builder builder = TypeSpec.classBuilder(name)
				.addSuperinterface(TypeName.get(OnReceiveWrapper.class))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addType(onReceive)
				.addMethod(acceptMethod);

		writer.write(builder, clazz, OnReceiveWrapper.class, annotation.autoLoad());
	}

}
