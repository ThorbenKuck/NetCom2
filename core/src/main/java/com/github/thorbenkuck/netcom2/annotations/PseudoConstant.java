package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated Fields are considered constants.
 * <p>
 * This Annotation is needed, because normally a field is considered constant, if
 * the field is of an immutable type and looks the following:
 * <p>
 * <code>
 * static final VariableType VARIABLE_NAME;
 * </code>
 * <p>
 * There are however use-cases, where a field is not static, because it does not override
 * equals and is used for correctly adding and removing it to and from other objects.
 * <p>
 * Those fields may be Annotated with this Annotation, to signal a immutable or stateless variable,
 * that depends on the instance of the aggregating class.
 * <p>
 * Important is, that those fields annotated are stateless and immutable. So, nothing will change
 * the variable itself and the variable does not contain non stateless or non immutable variables.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PseudoConstant {
}
