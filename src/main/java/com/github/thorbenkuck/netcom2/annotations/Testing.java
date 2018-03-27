package com.github.thorbenkuck.netcom2.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used for Tests to show which class is tested.
 * <p>
 * This is done, so that the Test may be associated with a class.
 * <p>
 * Normally, the name of the Test should reflect that. However, sometimes it is not that clear, or multiple classes are
 * instantiated, so that the Test correctly tests the defined module.
 * <p>
 * For that, you should use this annotation and show, which Class you are testing.
 */
@APILevel
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Testing {

	/**
	 * This method will show, which classes are tested by this test.
	 * <p>
	 * In nearly all Tests, only one class will be tested. However, sometimes, there are multiple classes that are tested,
	 * because the unit is just that big. For that, you may give multiple classes.
	 *
	 * @return all classes, tested by this Test.
	 */
	Class[] value();

}
