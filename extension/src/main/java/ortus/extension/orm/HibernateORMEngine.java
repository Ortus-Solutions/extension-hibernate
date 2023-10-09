package ortus.extension.orm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.EntityMode;
import org.hibernate.tuple.entity.EntityTuplizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ortus.extension.orm.event.EventListenerIntegrator;
import ortus.extension.orm.mapping.HBMCreator;
import ortus.extension.orm.tuplizer.AbstractEntityTuplizerImpl;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ExceptionUtil;
import ortus.extension.orm.util.HibernateUtil;
import ortus.extension.orm.util.ExtensionUtil;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.type.Collection.Key;

import ch.qos.logback.classic.Level;

public class HibernateORMEngine implements ORMEngine {

    private Map<String, SessionFactoryData> factories = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HibernateORMEngine.class);

    static {
        /**
         * Workaround for certain jaxb-api jars not setting the context factory location. See LDEV-4276.
         *
         * The system property we need to set is different based on which java / JRE version we are running, hence the
         * call to getJVMVersion.
         */
        String jaxbContextProperty = ExtensionUtil.getJVMVersion() < 11 ? "javax.xml.bind.context.factory"
                : "javax.xml.bind.JAXBContextFactory";
        System.setProperty( jaxbContextProperty, "com.sun.xml.bind.v2.ContextFactory" );
    }

    public HibernateORMEngine() {
    }

    /**
     * Instantiate the Hibernate session and factory data.
     *
     * @param pc
     *           PageContext
     */
    @Override
    public void init( PageContext pc ) throws PageException {
        getOrBuildSessionFactoryData( pc );
    }

    @Override
    public ORMSession createSession( PageContext pc ) throws PageException {
        return new HibernateORMSession( pc, getSessionFactory( pc.getApplicationContext().getName() ) );
    }

    /**
     * Reload the ORM session.
     *
     * Will NOT reload if force is false and the given pageContext already has a session factory.
     *
     * @param pc
     *              The current page context object
     * @param force
     *              Force reload all session factory data.
     */

    @Override
    public boolean reload( PageContext pc, boolean force ) throws PageException {
        String applicationName = pc.getApplicationContext().getName();
        if ( force || !isInitializedForApplication( applicationName ) ) {
            logger.atInfo().log("Reloading ORM");
            clearSessionFactory( applicationName );
            buildSessionFactoryData( pc );
            return false;
        }
        return false;
    }

    private boolean isInitializedForApplication( String applicationName ) {
        return factories.containsKey( applicationName );
    }

    /**
     * Get the SessionFactoryData by application name.
     *
     * @param applicationName
     *                        Lucee application name, retrieve from {@link lucee.runtime.listener.ApplicationContext#getName()}
     */
    private SessionFactoryData getSessionFactory( String applicationName ) {
        return factories.get( applicationName );
    }

    /**
     * Retrieve a SessionFactoryData() if configured for this application. If not, build one and retrieve that.
     *
     * @param pc
     *           Lucee PageContext object.
     *
     * @return extension SessionFactoryData object.
     *
     * @throws PageException
     */
    private SessionFactoryData getOrBuildSessionFactoryData( PageContext pc ) throws PageException {
        String applicationName = pc.getApplicationContext().getName();
        if ( !isInitializedForApplication( applicationName ) ) {
            SessionFactoryData data = buildSessionFactoryData( pc );
            data.init();
        }
        return getSessionFactory( applicationName );
    }

    /**
     * Add a new session factory specific to this application.
     *
     * @param applicationName
     *                        Lucee application name, retrieve from {@link lucee.runtime.listener.ApplicationContext#getName()}
     * @param factory
     *                        the SessionFactoryData object which houses the application-level Hibernate session factory
     */
    private void setSessionFactory( String applicationName, SessionFactoryData factory ) {
        factories.put( applicationName, factory );
    }

    /**
     * Wipe the SessionFactoryData object for this Lucee application name from memory.
     *
     * @param applicationName
     *                        The Lucee application name.
     */
    private void clearSessionFactory( String applicationName ) {
        SessionFactoryData data = getSessionFactory( applicationName );
        if ( data != null ) {
            data.reset();
            factories.remove( applicationName );
        }
    }

    /**
     * Reload all ORM configuration and entities and reload the HIbernate ORM session factory.
     *
     * @param pc
     *           Lucee PageContext
     *
     * @return SessionFactoryData
     *
     * @throws PageException
     */
    private SessionFactoryData buildSessionFactoryData( PageContext pc ) throws PageException {
        ApplicationContext appContext = pc.getApplicationContext();
        if ( !appContext.isORMEnabled() )
            throw ExceptionUtil.createException( ( ORMSession ) null, null, "ORM is not enabled", "" );

        // datasource
        ORMConfiguration ormConf = appContext.getORMConfiguration();

        new LoggingConfigurator( Level.ERROR, ormConf.logSQL() ).configure();

        SessionFactoryData data = new SessionFactoryData( this, ormConf );
        setSessionFactory( pc.getApplicationContext().getName(), data );

        // config
        try {
            /**
             * 1. Find persistent components 2. create an XML mapping for each component 3. store component info and XML
             * mapping in CFCInfo object
             *
             */
            EntityFinder finder = new EntityFinder( ormConf.getCfcLocations(), !ormConf.skipCFCWithError() );
            synchronized ( data ) {

                data.tmpList = finder.loadComponents( pc );
                data.clearCFCs();

                // load entities
                if ( data.hasTempCFCs() ) {
                    // TODO: Set naming strategy in constructor based on ORM config
                    data.getNamingStrategy();// called here to make sure, it is called in the right context the
                                             // first one

                    // creates CFCInfo objects
                    for ( Component persistentComponent : data.tmpList ) {
                        createMapping( pc, persistentComponent, ormConf, data );
                    }

                    /**
                     * check for duplicate entity names and throw if any are dupes. Could this be moved into the above
                     * loop?
                     */
                    if ( data.tmpList.size() != data.sizeCFCs() ) {
                        Map<String, String> names = new HashMap<>();
                        for ( Component cfc : data.tmpList ) {
                            String name = HibernateCaster.getEntityName( cfc );
                            if ( names.containsKey( name.toLowerCase() ) ) {
                                String message = String.format(
                                        "Entity Name [%s] is ambigous, [%s] and [%s] use the same entity name.", name,
                                        names.get( name.toLowerCase() ), cfc.getPageSource().getDisplayPath() );
                                throw ExceptionUtil.createException( data, null, message, null );
                            }
                            names.put( name.toLowerCase(), cfc.getPageSource().getDisplayPath() );
                        }
                    }
                }
            }
        } finally {
            data.tmpList = null;
        }

        Log log = pc.getConfig().getLog( "orm" );

        /**
         * SET CONFIGURATION PER DATASOURCE
         */
        for ( Entry<Key, String> datasourceMappings : HibernateSessionFactory.assembleMappingsByDatasource( data ).entrySet() ) {
            Key datasourceName = datasourceMappings.getKey();
            String mappingXML = datasourceMappings.getValue();
            if ( data.getConfiguration( datasourceName ) != null )
                continue;

            try {
                data.setConfiguration( log, mappingXML, data.getDataSource( datasourceName ), null, null,
                        appContext == null ? "" : appContext.getName() );
            } catch ( Exception ex ) {
                throw ExceptionUtil.toPageException( ex );
            }

            EntityTuplizerFactory tuplizerFactory = data.getConfiguration( datasourceName ).config.getEntityTuplizerFactory();
            tuplizerFactory.registerDefaultTuplizerClass( EntityMode.MAP, AbstractEntityTuplizerImpl.class );
            tuplizerFactory.registerDefaultTuplizerClass( EntityMode.POJO, AbstractEntityTuplizerImpl.class );

            data.buildSessionFactory( datasourceName );
        }
        configureEventHandler( pc, data );

        return data;
    }

    /**
     * Attach our event integrator object to the Hibernate configuration.
     *
     * @param pc
     *             Lucee PageContext object for retrieving the event handler
     * @param data
     *             SessionFactoryData object - houses the ORM configuration and the event integrator.
     *
     * @throws PageException
     */
    private static void configureEventHandler( PageContext pc, SessionFactoryData data ) throws PageException {
        if ( !data.getORMConfiguration().eventHandling() )
            return;
        String eventHandlerPath = data.getORMConfiguration().eventHandler();

        EventListenerIntegrator integrator = data.getEventListenerIntegrator();
        if ( eventHandlerPath != null && !eventHandlerPath.trim().isEmpty() ) {
            Component eventHandler = pc.loadComponent( eventHandlerPath.trim() );
            if ( eventHandler != null ) {
                integrator.setGlobalEventListener( eventHandler );
            }
        }
    }

    /**
     * Build or load the XML mapping string for the provided entity type. If `autogenmap` is enabled, will generate the
     * XML mapping. If autogenmap is false, will attempt to load the XML mapping from disk.
     *
     * @param pc
     *                Lucee PageContext object
     * @param cfc
     *                A persistent Component for which we wish to generate an XML mapping.
     * @param ormConf
     *                ORM configuration for this CFML application.
     * @param data
     *                the SessionFactoryData object housing the ORM application data.
     *
     * @throws PageException
     */
    public void createMapping( PageContext pc, Component cfc, ORMConfiguration ormConf, SessionFactoryData data )
            throws PageException {
        String entityName = HibernateCaster.getEntityName( cfc );
        CFCInfo info = data.getCFC( entityName, null );
        String xml;
        if ( info == null || ( CommonUtil.equals( info.getCFC(), cfc ) ) ) {
            DataSource ds = CommonUtil.getDataSource( pc, cfc );

            if ( ormConf.autogenmap() ) {
                pc.addPageSource( cfc.getPageSource(), true );

                /**
                 * TODO: Create a map of connections per datasource. Then we can grab and reuse existing connections
                 * based on the component's datasource annotation. This should save a good bit of time from opening and
                 * releasing connections hundreds of times for a single ORM reload.
                 */
                DatasourceConnection dc = CommonUtil.getDatasourceConnection( pc, ds, null, null, false );
                try {
                    logger.atInfo().log( String.format("Creating XML mapping for entity %s", entityName) );
                    xml = HBMCreator.toMappingString( HBMCreator.createXMLMapping( pc, dc, cfc, data ) );
                    if ( ormConf.saveMapping() ) {
                        HBMCreator.saveMapping( cfc, xml );
                    }
                } catch ( Exception e ) {
                    throw ExceptionUtil.toPageException( e );
                } finally {
                    pc.removeLastPageSource( true );
                    CommonUtil.releaseDatasourceConnection( pc, dc, false );
                }
            }
            // load
            else {
                try {
                    logger.atInfo().log( String.format("Loading XML mapping for entity %s", entityName) );
                    xml = HBMCreator.loadMapping( cfc );
                } catch ( Exception e ) {
                    throw ExceptionUtil.toPageException( e );
                }

            }
            data.addCFC( entityName, new CFCInfo( HibernateUtil.getCompileTime( pc, cfc.getPageSource() ), xml, cfc, ds ) );
        }

    }

    @Override
    public int getMode() {
        // @TODO: implement
        return MODE_LAZY;
    }

    @Override
    public String getLabel() {
        return "Hibernate";
    }

    /**
     * Get the ORM configuration for the given PageContext
     *
     * @param pc
     *           PageContext object
     *
     * @return ORMConfiguration
     */
    @Override
    public ORMConfiguration getConfiguration( PageContext pc ) {
        ApplicationContext ac = pc.getApplicationContext();
        if ( !ac.isORMEnabled() )
            return null;
        return ac.getORMConfiguration();
    }

    /**
     * @param pc
     * @param session
     * @param entityName
     *                   name of the entity to get
     * @param unique
     *                   create a unique version that can be manipulated
     *
     * @return Lucee Component
     *
     * @throws PageException
     */
    public Component create( PageContext pc, HibernateORMSession session, String entityName, boolean unique )
            throws PageException {
        SessionFactoryData data = session.getSessionFactoryData();
        // get existing entity
        Component cfc = createComponentFromDataMap( pc, entityName, unique, data );
        if ( cfc != null )
            return cfc;

        ORMConfiguration ormConf = pc.getApplicationContext().getORMConfiguration();
        Resource[] locations = ormConf.getCfcLocations();

        String detail = String.format( "component are searched in the following directories [%s]", toString( locations ) );
        String message = String.format( "No entity (persistent component) with name [%s] found, available entities are [%s]",
                entityName, CFMLEngineFactory.getInstance().getListUtil().toList( data.getEntityNames(), ", " ) );
        throw ExceptionUtil.createException( data, null, message, detail );

    }

    private String toString( Resource[] locations ) {
        if ( locations == null )
            return "";
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < locations.length; i++ ) {
            if ( i > 0 )
                sb.append( ", " );
            sb.append( locations[ i ].getAbsolutePath() );
        }
        return sb.toString();
    }

    private static Component createComponentFromDataMap( PageContext pc, String entityName, boolean unique,
            SessionFactoryData data ) throws PageException {
        CFCInfo info = data.getCFC( entityName, null );
        if ( info != null ) {
            Component cfc = info.getCFC();
            if ( unique ) {
                cfc = ( Component ) cfc.duplicate( false );
                if ( cfc.contains( pc, CommonUtil.INIT ) )
                    cfc.call( pc, "init", new Object[] {} );
            }
            return cfc;
        }
        return null;
    }
}

class CFCInfo {

    private String xml;
    private long modified;
    private Component cfc;
    private DataSource ds;

    public CFCInfo( long modified, String xml, Component cfc, DataSource ds ) {
        this.modified = modified;
        this.xml      = xml;
        this.cfc      = cfc;
        this.ds       = ds;
    }

    /**
     * @return the cfc
     */
    public Component getCFC() {
        return cfc;
    }

    /**
     * @return the xml
     */
    public String getXML() {
        return xml;
    }

    /**
     * @return the modified
     */
    public long getModified() {
        return modified;
    }

    public DataSource getDataSource() {
        return ds;
    }

}
