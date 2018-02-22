package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * Shows that this annotated class is not yet tested thoroughly.
 *
 * It might still function as proposed, but it is not recommended to rely on its behaviour since it might be subject to change.
 * Further, you might encounter unexpected Behaviour upon calling it even tho it should not happen. It would be wonderful if you would report any Bug to github.com/ThorbenKuck/NetCom2
 *
 * Experimental Methods or Classes should become tested (and therefor loose this annotation) with the next major release.
 * Tho this is aimed for, it can not be guarantied every time. If any subject stays longer annotated with this annotation,
 * you should consider looking for some other way.
 *
 * Future: There might be a warning, when an class, annotated with Experimental is compiled, so that using developers know
 * what there up against. This should be possible with an annotationProcessor.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Experimental {
}
