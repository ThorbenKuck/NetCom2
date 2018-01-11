package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This shows, that an Annotated Method is exposed to be used only by the implementing developer. Therefor it should be
 * accessible, but NOT public, private or protected!
 *
 * Further, any Method annotated with @APILevel should be used with great care by any developer outside of NetCom2!
 *
 * If a Class is annotated, the whole Class should be wrapped or delegated to, but never exposed to an using developer!
 * The same is true for true for fields, parameters and Constructors!
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface APILevel {
}
