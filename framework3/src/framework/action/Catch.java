package framework.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 액션메소드에서 예외가 발행한 후에 호출할 메소드에 적용한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Catch {
	/**
	 * 예외클래스가 일치하거나 하위 클래스일 때 동작한다.
	 * @return 적용할 예외 클래스 배열
	 */
	Class<? extends Exception>[] value() default {};

	/**
	 * 우선순위
	 * @return 우선순위
	 */
	int priority() default 0;
}