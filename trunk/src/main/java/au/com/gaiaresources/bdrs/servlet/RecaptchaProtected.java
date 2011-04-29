package au.com.gaiaresources.bdrs.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a handler class as protected by reCAPTCHA.
 * It would be nice if we could apply this to the method level but for now it applies to
 * all handler methods in the controller class.
 * Unfortunately, Spring's <code>AnnotationMethodHandlerAdapter</code> is not quite flexibile enough to allow it.
 * @author Tim Carpenter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecaptchaProtected {

}
