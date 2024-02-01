package ortus.extension.orm.tuplizer.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.type.CompositeType;

public class CFCHibernateProxyFactory implements ProxyFactory {

	private String	entityName;
	private String	nodeName;

	@Override
	@SuppressWarnings( "rawtypes" )
	public void postInstantiate( final String entityName, final Class persistentClass, final Set interfaces,
	    final Method getIdentifierMethod, final Method setIdentifierMethod, CompositeType componentIdType ) {
		int index = entityName.indexOf( '.' );
		this.nodeName	= entityName;
		this.entityName	= entityName.substring( index + 1 );
	}

	public void postInstantiate( PersistentClass pc ) {
		this.nodeName	= pc.getClassName();
		this.entityName	= pc.getEntityName();
	}

	@Override
	public HibernateProxy getProxy( final Serializable id, final SharedSessionContractImplementor session ) {
		try {
			return new CFCHibernateProxy( new CFCLazyInitializer( entityName, id, ( SessionImplementor ) session ) );
		} catch ( Exception t ) {
			return new CFCHibernateProxy( new CFCLazyInitializer( nodeName, id, ( SessionImplementor ) session ) );
		}
	}
}