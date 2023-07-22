package ortus.extension.orm.event;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
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
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import ortus.extension.orm.runtime.type.KeyImpl;
import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.util.CommonUtil;

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

    private static final long serialVersionUID = -5954121166467541422L;

    /**
     * The EventHandler CFC defined in the application's `this.ormSettings.eventHandler`.
     */
    private Component globalEventListener;

    public static final Key ON_EVICT = new KeyImpl( "onEvict" );
    public static final Key ON_DIRTY_CHECK = new KeyImpl( "onDirtyCheck" );
    public static final Key ON_DELETE = new KeyImpl( "onDelete" );
    public static final Key ON_CLEAR = new KeyImpl( "onClear" );
    public static final Key ON_AUTO_FLUSH = new KeyImpl( "onAutoFlush" );
    public static final Key ON_FLUSH = new KeyImpl( "onFlush" );
    public static final Key PRE_INSERT = new KeyImpl( "preInsert" );
    public static final Key PRE_UPDATE = new KeyImpl( "preUpdate" );
    public static final Key POST_LOAD = new KeyImpl( "postLoad" );
    public static final Key PRE_LOAD = new KeyImpl( "preLoad" );
    public static final Key POST_DELETE = new KeyImpl( "postDelete" );
    public static final Key PRE_DELETE = new KeyImpl( "preDelete" );
    public static final Key POST_UPDATE = new KeyImpl( "postUpdate" );
    public static final Key POST_INSERT = new KeyImpl( "postInsert" );

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

    }

    /**
     * Set the "Global" EventHandler to fire on all events in the application.
     *
     * @param globalEventListener
     *                            Instantiated Lucee Component object.
     */
    public void setGlobalEventListener( Component globalEventListener ) {
        this.globalEventListener = globalEventListener;
    }

    @Override
    public boolean requiresPostCommitHanding( EntityPersister arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onPreInsert( PreInsertEvent event ) {
        String[] propertyNames = event.getPersister().getEntityMetamodel().getPropertyNames();
        Struct state = entityStateToStruct( propertyNames, event.getState() );
        fireEventOnGlobalListener( EventListenerIntegrator.PRE_INSERT, event.getEntity(), event, state );

        fireOnEntity( event.getEntity(), EventListenerIntegrator.PRE_INSERT, event, null );

        Component entity = CommonUtil.toComponent( event.getEntity(), null );
        if ( entity != null )
            persistEntityChangesToState( event.getState(), propertyNames, entity );
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
        String[] propertyNames = event.getPersister().getEntityMetamodel().getPropertyNames();
        Struct oldState = entityStateToStruct( propertyNames, event.getOldState() );
        fireEventOnGlobalListener( EventListenerIntegrator.PRE_UPDATE, event.getEntity(), event, oldState );

        fireOnEntity( event.getEntity(), EventListenerIntegrator.PRE_UPDATE, event, oldState );
        Component entity = CommonUtil.toComponent( event.getEntity(), null );
        if ( entity != null )
            persistEntityChangesToState( event.getState(), propertyNames, entity );
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
    public void onDelete( DeleteEvent event, Set transientEntities ) throws HibernateException {
        Object entity = event.getObject();
        // TODO: handle transientEntities
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

        fireOnComponent( getGlobalEventListener(), name, entity, data, event );
    }

    /**
     * Fire the event listener UDF, if found, on the entity component
     *
     * @param listener
     *                 the Lucee Component on which to fire this listener method
     * @param name
     *                 event type name to fire, for example "preInsert" or "preDelete"
     * @param event
     *                 the Hibernate event object.
     * @param data
     *                 A struct of data to pass to the event
     */
    public void fireOnEntity( Object entity, Key name, AbstractEvent event, Struct data ) {
        Component listener = CommonUtil.toComponent( entity, null );
        if ( listener != null ) {
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
     * @param properties
     *                   Array of property names, usually retrieved from <code>event.getPersister().getPropertyNames()</code>
     * @param values
     *                   Array of property values, either retrieved from <code>event.getPersister().getPropertyValues()</code>,
     *                   <code>event.getState()</code> or <code>event.getOldState()</code>
     *
     * @return A struct
     */
    private Struct entityStateToStruct( String[] properties, Object[] values ) {
        Struct entityState = CommonUtil.createStruct();

        if ( values != null && properties != null && values.length == properties.length ) {
            for ( int i = 0; i < values.length; i++ ) {
                entityState.setEL( new KeyImpl( properties[ i ] ), values[ i ] );
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
     * @param state
     *                        The entity state to persist, from event.getState(). {@link org.hibernate.event.spi.PreInsertEvent}
     * @param stateProperties
     *                        Array of properties to update, matching the order of the `state[]` fields.
     * @param entity
     *                        The entity to pull a potentially altered value from.
     */
    private void persistEntityChangesToState( Object[] state, String[] stateProperties, Component entity ) {
        try {
            Property[] properties = entity.getProperties( true, false, false, false );
            for ( int n = 0; n < stateProperties.length; ++n ) {
                final String currentProperty = stateProperties[ n ];
                Optional<Property> property = Arrays.stream( properties )
                        .filter( prop -> prop.getName().equals( currentProperty ) ).findFirst();
                if ( property.isPresent() ) {
                    Property theprop = property.get();
                    if ( theprop.getValue() != null ) {
                        Object value = HibernateCaster.toHibernateValue( entity, theprop );
                        state[ n ] = value;
                    }
                }
            }
        } catch ( Exception e ) {
            throw new RuntimeException(
                    "Error populating event state for persistance in pre-event listener method: " + e.getMessage(), e );
        }
    }
}
