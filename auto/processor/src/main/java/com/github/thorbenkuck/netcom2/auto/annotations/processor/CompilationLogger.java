package com.github.thorbenkuck.netcom2.auto.annotations.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

final class CompilationLogger {

	private final Messager messager;

	CompilationLogger(Messager messager) {
		this.messager = messager;
	}

	public void error(String msg, Element element, AnnotationMirror mirror) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, element, mirror);
	}

	public void error(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, element);
	}

	public void error(String msg) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg);
	}

	public void log(String msg) {
		messager.printMessage(Diagnostic.Kind.NOTE, msg);
	}

	public void log(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
	}

	public void warn(String msg) {
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
	}

	public void warn(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, element);
	}
}
