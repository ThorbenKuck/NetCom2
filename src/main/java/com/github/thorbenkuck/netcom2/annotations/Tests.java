package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This Annotation is used to signal multiple different tests for the same Class.
 * <p>
 * If you had an integration-test, testing the correct Communication between 2 Clients and a Server, this would
 * not be a unit test. But you may be testing this specific module that the integration test handles locally. Then, you
 * would do the following:
 * <p>
 * <pre><code>
 * {@literal @}Tests({
 *     {@literal @}Tested(responsibleTest = "package.of.unit.test.TestOne"),
 *     {@literal @}Tested(responsibleTest = "package.of.unit.test.TestTwo"),
 *     {@literal @}Tested(responsibleTest = "package.of.integration.test.Test", unitTest = false)
 *     })
 * public class ModuleThatIsTested {
 *     // ...
 * }
 * </code></pre>
 * <p>
 * This ensures better readability.
 * <p>
 * This annotation is only used at the RetentionPolicy of Source. It is neither meant to be analyzed at Runtime, nor at
 * compile-time. All this class does, is to be more transparent about the Tests for the developers. The test-package
 * should not be of need if this Framework is used.
 * <p>
 * Because the {@link Tested} annotation is annotated with the meta-annotation of {@link Repeatable}, you do not have
 * to manually write it this way. The following is also perfectly okay:
 * <p>
 * <pre><code>
 * {@literal @}Tested(responsibleTest = "package.of.unit.test.TestOne"),
 * {@literal @}Tested(responsibleTest = "package.of.unit.test.TestTwo"),
 * {@literal @}Tested(responsibleTest = "package.of.integration.test.Test", unitTest = false)
 * public class ModuleThatIsTested {
 *     // ...
 * }
 * </code></pre>
 *
 * @version 1.0
 * @see Tested
 * @since 1.0
 */
@Deprecated
@APILevel
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Tests {

	/**
	 * In here, the Tests, that are responsible for the annotated class, should be given.
	 *
	 * @return all Tests, that are responsible for the annotated class.
	 * @see Tested
	 */
	Tested[] value();

}
