package com.ortussolutions.hibernate.tuplizer.proxy;

import java.io.Serializable;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.AbstractLazyInitializer;
import com.ortussolutions.hibernate.HibernatePageException;
import com.ortussolutions.hibernate.util.CommonUtil;

import lucee.runtime.Component;
import lucee.runtime.exp.PageException;

/**
 * Lazy initializer for "dynamic-map" entity representations. SLOW
 */
public class CFCLazyInitializer extends AbstractLazyInitializer implements Serializable {

    CFCLazyInitializer(String entityName, Serializable id, SessionImplementor session) {
        super(entityName, id, session);
    }

    public Component getCFC() {
        try {
            return CommonUtil.toComponent(getImplementation());
        } catch (PageException pe) {
            throw new HibernatePageException(pe);
        }
    }

    @Override
    public Class getPersistentClass() {
        throw new UnsupportedOperationException("dynamic-map entity representation");
    }

}
