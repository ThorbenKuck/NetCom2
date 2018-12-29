package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;

final class Auto {

	static AnnotationSpec annotation(Class<?> serviceType) {
		return AnnotationSpec.builder(AutoService.class)
				.addMember("value", "$T.class", serviceType)
				.build();
	}

}
