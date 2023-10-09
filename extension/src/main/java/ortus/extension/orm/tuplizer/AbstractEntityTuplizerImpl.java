package ortus.extension.orm.tuplizer;

import java.io.Serializable;
import java.util.HashMap;

import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.Type;

import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.mapping.HBMCreator;
import ortus.extension.orm.tuplizer.accessors.CFCGetter;
import ortus.extension.orm.tuplizer.accessors.CFCSetter;
import ortus.extension.orm.tuplizer.proxy.CFCHibernateProxyFactory;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.HibernateUtil;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * Entity tuplizer for handling CFC getters, setters, identifiers, and such.
 */
public class AbstractEntityTuplizerImpl extends AbstractEntityTuplizer {

    public AbstractEntityTuplizerImpl( EntityMetamodel entityMetamodel, PersistentClass persistentClass ) {
        super( entityMetamodel, persistentClass );
    }

    @Override
    public Serializable getIdentifier( Object entity ) throws HibernateException {
        return toIdentifier( super.getIdentifier( entity ) );
    }

    @Override
    public void setIdentifier( final Object entity, final Serializable id, final SharedSessionContractImplementor session ) {
        super.setIdentifier( entity, toIdentifier( id ), session );
    }

    private Serializable toIdentifier( Serializable id ) {
        if ( id instanceof Component ) {
            HashMap<String, Object> map = new HashMap<>();
            Component cfc = ( Component ) id;
            ComponentScope scope = cfc.getComponentScope();
            lucee.runtime.component.Property[] props = HibernateUtil.getIDProperties( cfc, true, true );
            lucee.runtime.component.Property p;
            String name;
            Object value;
            for ( int i = 0; i < props.length; i++ ) {
                p     = props[ i ];
                name  = p.getName();
                value = scope.get( CommonUtil.createKey( name ), null );
                String type = p.getType();
                Object o = p.getMetaData();
                Struct meta = o instanceof Struct ? ( Struct ) o : null;
                // ormtype
                if ( meta != null ) {
                    String tmp = CommonUtil.toString( meta.get( "ormtype", null ), null );
                    if ( !Util.isEmpty( tmp ) )
                        type = tmp;
                }

                // generator
                if ( meta != null && CommonUtil.isAnyType( type ) ) {
                    type = "string";
                    try {
                        String gen = CommonUtil.toString( meta.get( "generator", null ), null );
                        if ( !Util.isEmpty( gen ) ) {
                            type = HBMCreator.getDefaultTypeForGenerator( gen, "string" );
                        }
                    } catch ( Throwable t ) {
                        if ( t instanceof ThreadDeath )
                            throw ( ThreadDeath ) t;
                    }
                }
                try {
                    value = HibernateCaster.toHibernateValue( CFMLEngineFactory.getInstance().getThreadPageContext(), value,
                            type );
                } catch ( PageException pe ) {
                    // TODO: for 7.0, log this!
                }

                map.put( name, value );
            }
            return map;
        }
        return id;
    }

    @Override
    protected Instantiator buildInstantiator( EntityMetamodel entityMetamodel, PersistentClass persistentClass ) {
        return new CFCInstantiator( entityMetamodel, persistentClass );
    }

    @Override
    protected Getter buildPropertyGetter( Property mappedProperty, PersistentClass mappedEntity ) {
        Type type = null;
        if ( mappedProperty.getValue() != null )
            type = mappedProperty.getType();
        return new CFCGetter( mappedProperty.getName(), type, mappedEntity.getEntityName() );
    }

    @Override
    protected Setter buildPropertySetter( Property mappedProperty, PersistentClass mappedEntity ) {
        Type type = null;
        if ( mappedProperty.getValue() != null )
            type = mappedProperty.getType();
        return new CFCSetter( mappedProperty.getName(), type, mappedEntity.getEntityName() );
    }

    @Override
    protected ProxyFactory buildProxyFactory( PersistentClass pc, Getter arg1, Setter arg2 ) {
        CFCHibernateProxyFactory pf = new CFCHibernateProxyFactory();
        pf.postInstantiate( pc );

        return pf;
    }

    @Override
    public String determineConcreteSubclassEntityName( Object entityInstance, SessionFactoryImplementor factory ) {
        return CFCEntityNameResolver.INSTANCE.resolveEntityName( entityInstance );
    }

    @Override
    public EntityNameResolver[] getEntityNameResolvers() {
        return new EntityNameResolver[] { CFCEntityNameResolver.INSTANCE };
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getConcreteProxyClass() {
        return Component.class;// ????
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getMappedClass() {
        return Component.class; // ????
    }

    @Override
    public EntityMode getEntityMode() {
        return EntityMode.MAP;
    }
}
