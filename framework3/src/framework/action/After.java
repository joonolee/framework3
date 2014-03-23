/** 
 * @(#)After.java
 */
package framework.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 액션메소드가 호출된 후에 예외가 발생하지 않았을 때 호출할 메소드에 적용한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface After {
	/**
	 * 메소드 이름이 일치 할때 동작한다.
	 */
	String[] only() default {};

	/**
	 * 메소드 이름이 일치하지 않을때 동작한다.
	 */
	String[] unless() default {};

	/** 
	 * 우선순위
	 */
	int priority() default 0;
}