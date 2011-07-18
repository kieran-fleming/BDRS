package au.com.gaiaresources.bdrs.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// used to indicate the name of a member after serialization / flattening in PersistentImpl.flatten()
@Retention(RetentionPolicy.RUNTIME)
public @interface MobileField {
	String name();
}
