package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * Shows that this annotated class is not yet tested thoroughly.
 * <p>
 * It might still function as proposed, but it is not recommended to rely on its behaviour since it might be subject to change.
 * Further, you might encounter unexpected behaviour upon calling it even though it should not happen. It would be wonderful if you would report any bug to github.com/ThorbenKuck/NetCom2
 * <p>
 * Experimental Methods or Classes should become tested (and therefore lose this annotation) with the next major release.
 * Though this is aimed for, it can not be guarantied every time. If any subject stays annotated longer with this annotation,
 * you should consider looking for some other way.
 * <p>
 * Future: There might be a warning, when a class, annotated with Experimental is compiled, so that using developers know
 * what they're up against. This should be possible with an annotationProcessor.
 *
 * @version 1.0
 * @since 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Experimental {
}
