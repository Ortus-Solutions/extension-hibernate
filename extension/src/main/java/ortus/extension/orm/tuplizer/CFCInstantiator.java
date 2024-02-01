package ortus.extension.orm.tuplizer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.EntityMetamodel;
import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.HibernateORMEngine;
import ortus.extension.orm.HibernateORMSession;
import ortus.extension.orm.HibernatePageException;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.HibernateUtil;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class CFCInstantiator implements Instantiator {

	private String			entityName;
	private Set<String>		isInstanceEntityNames	= new HashSet<>();
	private EntityMetamodel	entityMetamodel;

	public CFCInstantiator() {
		this.entityName = null;
	}

	/**
	 * Constructor of the class
	 *
	 * @param entityMetamodel
	 *                        Hibernate EntityMetamodel object
	 * @param mappingInfo
	 *                        Hibernate PersistentClass mapping info for this CFC
	 */
	public CFCInstantiator( EntityMetamodel entityMetamodel, PersistentClass mappingInfo ) {
		this.entityName			= mappingInfo.getEntityName();
		this.entityMetamodel	= entityMetamodel;
		isInstanceEntityNames.add( entityName );
		if ( mappingInfo.hasSubclasses() ) {
			Iterator<PersistentClass> itr = mappingInfo.getSubclassClosureIterator();
			while ( itr.hasNext() ) {
				final PersistentClass subclassInfo = itr.next();
				isInstanceEntityNames.add( subclassInfo.getEntityName() );
			}
		}
	}

	@Override
	public final Object instantiate( Serializable id ) {
		return instantiate();
	}

	@Override
	public final Object instantiate() {
		try {
			PageContext			pc		= CommonUtil.pc();
			HibernateORMSession	session	= ( HibernateORMSession ) pc.getORMSession( true );
			HibernateORMEngine	engine	= ( HibernateORMEngine ) session.getEngine();
			Component			c		= engine.create( pc, session, entityName, true );
			c.setEntity( true );
			return c;
		} catch ( PageException pe ) {
			throw new HibernatePageException( pe );
		}
	}

	@Override
	public final boolean isInstance( Object object ) {
		Component cfc = CommonUtil.toComponent( object, null );
		if ( cfc == null )
			return false;
		return isInstanceEntityNames.contains( HibernateCaster.getEntityName( cfc ) );
	}
}