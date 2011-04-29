package au.com.gaiaresources.bdrs.db;

import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

public class BDRSSessionFactoryBean extends AnnotationSessionFactoryBean {

    @Override
    protected org.hibernate.SessionFactory wrapSessionFactoryIfNecessary(
            org.hibernate.SessionFactory rawSf) {
        
        return new au.com.gaiaresources.bdrs.db.SessionFactory(rawSf);
    }
}
