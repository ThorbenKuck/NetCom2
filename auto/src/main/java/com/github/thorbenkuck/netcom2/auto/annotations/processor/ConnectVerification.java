package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

final class ConnectVerification {

	static boolean verifyParameters(ExecutableElement method, TypeElement clientElement) {
		List<? extends VariableElement> parameters = method.getParameters();
		if (parameters.size() != 1) {
			return false;
		}

		VariableElement element = parameters.get(0);
		if (element.asType().equals(clientElement.asType())) {
			return true;
		}

		return false;
	}

}
