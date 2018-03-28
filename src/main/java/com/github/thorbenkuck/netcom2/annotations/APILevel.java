package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This shows, that an annotated Method is exposed to be used only by the implementing developer. Therefore it should be
 * accessible, but NOT public, private or protected!
 * <p>
 * Further, any Method annotated with @APILevel should be used with great care by any developer outside of NetCom2!
 * <p>
 * If a Class is annotated, the whole Class should be wrapped or delegated to, but never exposed to an using developer!
 * The same is true for true for fields, parameters and Constructors!
 * <p>
 * If you use any Method annotated with this Annotation outside of this Frameworks internal modules, note that those
 * Methods are subject to Change (either by their behaviour or their design) even in an non-breaking update! Use at
 * your own risk!
 * <p>
 * Any method, class, parameter or field may be changed or deleted without any warning or without the use of the {@link Deprecated}
 * annotation.
 *
 * @version 1.0
 * @since 1.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface APILevel {
}
