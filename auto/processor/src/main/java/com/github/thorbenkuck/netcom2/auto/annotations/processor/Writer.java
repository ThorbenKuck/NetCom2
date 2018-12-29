package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

final class Writer {

	private final Filer filer;

	Writer(Filer filer) {
		this.filer = filer;
	}

	void write(TypeSpec.Builder builder, TypeElement type, Class<?> serviceType, boolean autoLoad) {
		if (autoLoad) {
			builder.addAnnotation(Auto.annotation(serviceType));
		}

		JavaFile.Builder fileBuilder;

		PackageElement packageElement = (PackageElement) type.getEnclosingElement();

		if (!type.getModifiers().contains(Modifier.PUBLIC)) {
			fileBuilder = JavaFile.builder(packageElement.getQualifiedName().toString(), builder.build());
		} else {
			fileBuilder = JavaFile.builder(packageElement.getQualifiedName().toString() + ".generated", builder.build());
		}


		try {
			fileBuilder.addFileComment("This file has been auto generated")
					.indent("	")
					.build().writeTo(filer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
