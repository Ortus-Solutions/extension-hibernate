package ortus.extension.orm.event;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.internal.Nullability;
import org.hibernate.engine.internal.Nullability.NullabilityCheckType;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.ClearEvent;
import org.hibernate.event.spi.ClearEventListener;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.DirtyCheckEvent;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.mapping.CFConstants;
import ortus.extension.orm.util.CommonUtil;

/**
 * Integrate Hibernate events with the EventHandler and component listener UDFs.
 *
 * For example, a <code>postInsert()</code> method in a <code>User</code> entity will be invoked prior to the entity's
 * insertion into the database. This same method will also be invoked on the EventHandler component, if configured.
 */
public class EventListenerIntegrator
    implements Integrator, PreInsertEventListener, PostInsertEventListener, PreDeleteEventListener, PostDeleteEventListener,
    DeleteEventListener, PreUpdateEventListener, PostUpdateEventListener, PreLoadEventListener, PostLoadEventListener,
    FlushEventListener, AutoFlushEventListener, ClearEventListener, DirtyCheckEventListener, EvictEventListener {

	private static final long	serialVersionUID	= -5954121166467541422L;

	private static final Logger	logger				= LoggerFactory.getLogger( EventListenerIntegrator.class );

	/**
	 * The EventHandler CFC defined in the application's `this.ormSettings.eventHandler`.
	 */
	private Component			globalEventListener;

	public static final Key		ON_EVICT			= CommonUtil.createKey( "onEvict" );
	public static final Key		ON_DIRTY_CHECK		= CommonUtil.createKey( "onDirtyCheck" );
	public static final Key		ON_DELETE			= CommonUtil.createKey( "onDelete" );
	public static final Key		ON_CLEAR			= CommonUtil.createKey( "onClear" );
	public static final Key		ON_AUTO_FLUSH		= CommonUtil.createKey( "onAutoFlush" );
	public static final Key		ON_FLUSH			= CommonUtil.createKey( "onFlush" );
	public static final Key		PRE_INSERT			= CommonUtil.createKey( "preInsert" );
	public static final Key		PRE_UPDATE			= CommonUtil.createKey( "preUpdate" );
	public static final Key		POST_LOAD			= CommonUtil.createKey( "postLoad" );
	public static final Key		PRE_LOAD			= CommonUtil.createKey( "preLoad" );
	public static final Key		POST_DELETE			= CommonUtil.createKey( "postDelete" );
	public static final Key		PRE_DELETE			= CommonUtil.createKey( "preDelete" );
	public static final Key		POST_UPDATE			= CommonUtil.createKey( "postUpdate" );
	public static final Key		POST_INSERT			= CommonUtil.createKey( "postInsert" );

	@Override
	public void integrate( Metadata metadata, SessionFactoryImplementor sessionFactory,
	    SessionFactoryServiceRegistry serviceRegistry ) {
		EventListenerRegistry eventListenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );

		eventListenerRegistry.prependListeners( EventType.PRE_INSERT, this );
		eventListenerRegistry.prependListeners( EventType.POST_INSERT, this );

		eventListenerRegistry.prependListeners( EventType.PRE_DELETE, this );
		eventListenerRegistry.prependListeners( EventType.POST_DELETE, this );
		eventListenerRegistry.prependListeners( EventType.DELETE, this );

		eventListenerRegistry.prependListeners( EventType.PRE_UPDATE, this );
		eventListenerRegistry.prependListeners( EventType.POST_UPDATE, this );

		eventListenerRegistry.prependListeners( EventType.PRE_LOAD, this );
		eventListenerRegistry.prependListeners( EventType.POST_LOAD, this );

		eventListenerRegistry.prependListeners( EventType.AUTO_FLUSH, this );
		eventListenerRegistry.prependListeners( EventType.FLUSH, this );

		eventListenerRegistry.prependListeners( EventType.EVICT, this );
		eventListenerRegistry.prependListeners( EventType.CLEAR, this );

		eventListenerRegistry.prependListeners( EventType.DIRTY_CHECK, this );
	}

	@Override
	public void disintegrate( SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry ) {
		// Not used, call by Hibernate to shutdown listeners. We don't need to do anything here.
	}

	/**
	 * Set the "Global" EventHandler to fire on all events in the application.
	 * Called by {@link ortus.extension.orm.HibernateORMEngine#configureEventHandler()}
	 *
	 * @param globalEventListener Instantiated Lucee Component object.
	 */
	public void setGlobalEventListener( Component globalEventListener ) {
		this.globalEventListener = globalEventListener;
	}

	/**
	 * @see https://docs.jboss.org/hibernate/orm/5.4/javadocs/org/hibernate/event/spi/PostCommitUpdateEventListener.html
	 */
	@Override
	public boolean requiresPostCommitHanding( EntityPersister persister ) {
		// Indicate whether post-commit handling is required
		return false;
	}

	/**
	 * Fired on a pre-insert event.
	 *
	 * @see https://docs.jboss.org/hibernate/orm/5.4/javadocs/org/hibernate/event/spi/PreInsertEvent.html
	 */
	@Override
	public boolean onPreInsert( PreInsertEvent event ) {
		// The properties of the entity in declared order, important, the same as the state[] array.
		String[]	propertyNames	= event.getPersister().getEntityMetamodel().getPropertyNames();
		Struct		state			= entityStateToStruct( propertyNames, event.getState() );
		Component	entityCFC		= CommonUtil.toComponent( event.getEntity(), null );

		fireEventOnGlobalListener( EventListenerIntegrator.PRE_INSERT, event.getEntity(), event, state );
		fireOnEntity( entityCFC, EventListenerIntegrator.PRE_INSERT, event, null );

		// Convert state changes from CFML -> Java
		Object[] stateValues = event.getState();
		persistEntityChangesToState( stateValues, propertyNames, entityCFC );

		// This is done to provide nullability checks for new entities and it's relationships
		// Implements the algorithm for validating property values for illegal null values
		new Nullability( event.getSession() )
		    .checkNullability( stateValues, event.getPersister(), NullabilityCheckType.CREATE );

		return false;
	}

	@Override
	public void onPostInsert( PostInsertEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.POST_INSERT, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.POST_INSERT, event, null );
	}

	// PreDeleteEventListener
	@Override
	public boolean onPreDelete( PreDeleteEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.PRE_DELETE, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.PRE_DELETE, event, null );
		return false;
	}

	// PostDeleteEventListener
	@Override
	public void onPostDelete( PostDeleteEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.POST_DELETE, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.POST_DELETE, event, null );
	}

	// PreUpdateEventListener
	@Override
	public boolean onPreUpdate( PreUpdateEvent event ) {
		// The properties of the entity in declared order, important, the same as the state[] array.
		String[]	propertyNames	= event.getPersister().getEntityMetamodel().getPropertyNames();
		Struct		oldState		= entityStateToStruct( propertyNames, event.getOldState() );
		Component	entityCFC		= CommonUtil.toComponent( event.getEntity(), null );

		fireEventOnGlobalListener( EventListenerIntegrator.PRE_UPDATE, event.getEntity(), event, oldState );
		fireOnEntity( entityCFC, EventListenerIntegrator.PRE_UPDATE, event, oldState );

		// Convert state changes from CFML -> Java
		Object[] stateValues = event.getState();
		persistEntityChangesToState( stateValues, propertyNames, entityCFC );

		// This is done to provide nullability checks for new entities and it's relationships
		// Implements the algorithm for validating property values for illegal null values
		new Nullability( event.getSession() )
		    .checkNullability( stateValues, event.getPersister(), NullabilityCheckType.CREATE );

		return false;
	}

	// PostUpdateEventListener
	@Override
	public void onPostUpdate( PostUpdateEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.POST_UPDATE, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.POST_UPDATE, event, null );
	}

	// PreLoadEventListener
	@Override
	public void onPreLoad( PreLoadEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.PRE_LOAD, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.PRE_LOAD, event, null );
	}

	// PostLoadEventListener
	@Override
	public void onPostLoad( PostLoadEvent event ) {
		fireEventOnGlobalListener( EventListenerIntegrator.POST_LOAD, event.getEntity(), event, null );
		fireOnEntity( event.getEntity(), EventListenerIntegrator.POST_LOAD, event, null );
	}

	@Override
	public void onFlush( FlushEvent event ) throws HibernateException {
		// Sadly, the FlushEvent does not allow / provide a method to retrieve the entity.
		Object entity = null;
		fireEventOnGlobalListener( EventListenerIntegrator.ON_FLUSH, entity, event, null );
	}

	@Override
	public void onAutoFlush( AutoFlushEvent event ) throws HibernateException {
		// Sadly, the AutoFlushEvent does not allow / provide a method to retrieve the entity.
		Object entity = null;
		fireEventOnGlobalListener( EventListenerIntegrator.ON_AUTO_FLUSH, entity, event, null );
	}

	@Override
	public void onClear( ClearEvent event ) {
		// Sadly, the ClearEvent does not allow / provide a method to retrieve the entity.
		Object entity = null;
		fireEventOnGlobalListener( EventListenerIntegrator.ON_CLEAR, entity, event, null );
	}

	@Override
	public void onDelete( DeleteEvent event ) throws HibernateException {
		Object entity = event.getObject();
		fireEventOnGlobalListener( EventListenerIntegrator.ON_DELETE, entity, event, null );
	}

	@Override
	@SuppressWarnings( "rawtypes" )
	public void onDelete( DeleteEvent event, Set transientEntities ) throws HibernateException {
		Object entity = event.getObject();
		// @TODO: handle transientEntities
		fireEventOnGlobalListener( EventListenerIntegrator.ON_DELETE, entity, event, null );
	}

	@Override
	public void onDirtyCheck( DirtyCheckEvent event ) throws HibernateException {
		// Sadly, the DirtyCheckEvent does not allow / provide a method to retrieve the entity.
		Object entity = null;
		fireEventOnGlobalListener( EventListenerIntegrator.ON_DIRTY_CHECK, entity, event, null );
	}

	@Override
	public void onEvict( EvictEvent event ) throws HibernateException {
		// Sadly, the EvictEvent does not allow / provide a method to retrieve the entity.
		Object entity = null;
		fireEventOnGlobalListener( EventListenerIntegrator.ON_EVICT, entity, event, null );
	}

	/**
	 * Retrieve the configured "Global" event listener.
	 *
	 * i.e., the Component configured in `this.ormSettings.eventHandler`.
	 *
	 * @return The configured Component to use as this application's event handler.
	 */
	public Component getGlobalEventListener() {
		return globalEventListener;
	}

	/**
	 * Fire the event listener UDF on the configured global EventHandler listener.
	 * <p>
	 * If no global event handler is configured, will exit.
	 *
	 * @param name
	 *               event type name to fire, for example "preInsert" or "preDelete"
	 * @param entity
	 *               the entity from event.getEntity()
	 * @param event
	 *               the Hibernate event object.
	 * @param data
	 *               A struct of data to pass to the event
	 */
	public void fireEventOnGlobalListener( Key name, Object entity, AbstractEvent event, Struct data ) {
		if ( globalEventListener == null ) {
			return;
		}

		if ( logger.isInfoEnabled() ) {
			logger.atInfo().log( String.format( "Firing event %s listener method on global listener %s", name, globalEventListener.getName() ) );
		}

		fireOnComponent( getGlobalEventListener(), name, entity, data, event );
	}

	/**
	 * Fire the event listener UDF, if found, on the entity component
	 *
	 * @param listener the Lucee Component on which to fire this listener method
	 * @param name     event type name to fire, for example "preInsert" or "preDelete"
	 * @param event    the Hibernate event object.
	 * @param data     A struct of data to pass to the event
	 */
	public void fireOnEntity( Object entity, Key name, AbstractEvent event, Struct data ) {

		// if entity is already a Component, just use it, else convert it to a component
		Component listener = ( entity instanceof Component ) ? ( Component ) entity : CommonUtil.toComponent( entity, null );

		if ( listener != null ) {
			if ( logger.isInfoEnabled() ) {
				logger.atInfo().log( String.format( "Firing event %s listener method on entity %s", name, listener.getName() ) );
			}
			fireOnComponent( listener, name, data, event );
		}
	}

	/**
	 * See if the given component has a method matching the given name.
	 *
	 * @param comp
	 *                   Lucee Component to look on, for example events.EventHandler.
	 * @param methodName
	 *                   Method name to look for, for example "preInsert"
	 *
	 * @return true if method found
	 */
	private boolean componentHasMethod( Component comp, Collection.Key methodName ) {
		return comp.get( methodName, null ) instanceof UDF;
	}

	private void fireOnComponent( Component cfc, Key name, Object... args ) {
		if ( !componentHasMethod( cfc, name ) ) {
			return;
		}
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		try {
			PageContext pc = engine.getThreadPageContext();
			cfc.call( pc, name, args );
		} catch ( PageException pe ) {
			throw engine.getCastUtil().toPageRuntimeException( pe );
		}
	}

	/**
	 * Merge the provided arrays of properties and values into a CFML-friendly struct.
	 *
	 * @param properties Array of property names, usually retrieved from <code>event.getPersister().getPropertyNames()</code>
	 * @param values     Array of property values, either retrieved from <code>event.getPersister().getPropertyValues()</code>,
	 *                   <code>event.getState()</code> or <code>event.getOldState()</code>
	 *
	 * @return A struct
	 */
	private Struct entityStateToStruct( String[] properties, Object[] values ) {
		Struct entityState = CommonUtil.createStruct();

		if ( values != null && properties != null && values.length == properties.length ) {
			for ( int i = 0; i < values.length; i++ ) {
				entityState.setEL( CommonUtil.createKey( properties[ i ] ), values[ i ] );
			}
		}

		return entityState;
	}

	/**
	 * Loop over the provided state properties and persist any entity changes to the state object.
	 * <p>
	 * Useful in the case of a "Pre database operation event", where the state to be committed has already been
	 * recorded, and any changes made during a `onPreInsert()` or `onPreUpdate()` event will need to be made in the
	 * `state` object in order to affect a change in what will be persisted.
	 * <p>
	 * Currently used in onPreInsert and onPreUpdate.
	 *
	 * See http://anshuiitk.blogspot.com/2010/11/hibernate-pre-database-opertaion-event.html
	 *
	 * @param state           The entity state to persist, from event.getState(). {@link org.hibernate.event.spi.PreInsertEvent}
	 * @param stateProperties Array of properties to update, matching the order of the `state[]` fields.
	 * @param entity          The entity to pull a potentially altered value from.
	 */
	private void persistEntityChangesToState( Object[] state, String[] stateProperties, Component entity ) {
		if ( logger.isDebugEnabled() ) {
			logger.atDebug().log( String.format( "persisting entity state changes on state properties %s", Arrays.toString( stateProperties ) ) );
		}

		try {
			Property[] cfcProperties = entity.getProperties( true, true, false, false );

			if ( logger.isDebugEnabled() ) {
				String propNames = Arrays.stream( cfcProperties ).map( Property::getName ).collect( Collectors.joining( "," ) );
				logger.atDebug().log( String.format( "persisting entity state changes for entity properties %s", propNames ) );
			}

			for ( int n = 0; n < stateProperties.length; ++n ) {
				// Find the appropriate CFC entity property from the Hibernate data
				final String		currentProperty	= stateProperties[ n ];
				Optional<Property>	property		= Arrays.stream( cfcProperties )
				    .filter( prop -> prop.getName().equalsIgnoreCase( currentProperty ) )
				    .filter( prop -> !isRelationshipField( prop ) )
				    .findFirst();
				// Cast it to the right value
				if ( property.isPresent() ) {
					state[ n ] = HibernateCaster.toHibernateValue( entity, property.get() );
				}
			}

		} catch ( Exception e ) {
			throw new RuntimeException(
			    String.format( "Error populating event state for persistance in [%s] entity pre-event listener method: %s", entity.getName(), e.getMessage() ),
			    e );
		}
	}

	/**
	 * Determine if the given property is a relationship field.
	 *
	 * @param prop The entity CFC property to check
	 *
	 * @return true if the property is a relationship field, false otherwise.
	 */
	private boolean isRelationshipField( Property prop ) {
		Struct	meta		= ( Struct ) prop.getMetaData();
		String	fieldType	= CommonUtil.toString( meta.get( CommonUtil.FIELDTYPE, null ), null );
		return fieldType != null && CFConstants.Relationships.isRelationshipType( fieldType );
	}
}
