package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * Shows that a Class is thoroughly tested.
 * <p>
 * You may provide the String, which represents the unit Test. With that, it should be absolutely clear, which Test
 * is responsible for the correct functionality of the annotated class.
 * <p>
 * This is a String, because the Test-Class is not accessible at compile of the production-code.
 * <p>
 * Also, you may provide a boolean, whether or not the annotated Class is an unit-Test or not.
 * <p>
 * It is important to note that this classes RetentionPolicy is only Source. This means, it is not queried or
 * maintained at Runtime and therefore uninteresting for performance. It is only meant to show that the annotated Class
 * is tested. Further this annotation should not be relied upon by using developers.
 * <p>
 * If you have multiple Tests for the same Class, you may annotate the Class in the following way:
 * <p>
 * <pre><code>
 * {@literal @}Tested(responsibleTest = "package.of.unit.test.TestOne"),
 * {@literal @}Tested(responsibleTest = "package.of.unit.test.TestTwo"),
 * {@literal @}Tested(responsibleTest = "package.of.integration.test.Test", unitTest = false)
 * public class ModuleThatIsTested {
 *     // ...
 * }
 * </code></pre>
 * <p>
 * This makes the code clean and readable. Please look at the {@link Tests} annotation, for further information.
 *
 * @version 1.0
 * @see Tests
 * @since 1.0
 */
@APILevel
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Tests.class)
public @interface Tested {

	/**
	 * You may provide the fully qualified Name fo the Test, which tests the annotated Class using this Method
	 * <p>
	 * The Name is not checked to be correct or not. This is just for developers to see, which Test they should look at
	 *
	 * @return the fully qualified Name to the Test, responsible for the annotated Class
	 */
	String responsibleTest();

	/**
	 * Describes, whether or not, the Test, responsible for testing the annotated Class, is a unit-test or not.
	 *
	 * @return true, if the responsible Test is a unit test or not.
	 */
	boolean unitTest() default true;

}
