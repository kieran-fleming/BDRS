package au.com.gaiaresources.bdrs.validation;

import org.springframework.validation.Errors;

import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public abstract class Validator<T> implements org.springframework.validation.Validator {
    
    @Override
    public boolean supports(@SuppressWarnings("unchecked") Class clazz) {
        return getSupportedClass().isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void validate(Object target, Errors errors) {
        internalValidate((T) target, errors);
    }
    
    protected abstract void internalValidate(T target, Errors errors);
    
    protected RequestContext getRequestContext() {
        return RequestContextHolder.getContext();
    }
    
    protected abstract Class<T> getSupportedClass();

}
