package ortus.extension.orm;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.internal.SessionFactoryImpl;

import ortus.extension.orm.runtime.type.KeyImpl;
import ortus.extension.orm.event.EventListenerIntegrator;
import ortus.extension.orm.jdbc.DataSourceConfig;
import ortus.extension.orm.naming.CFCNamingStrategy;
import ortus.extension.orm.naming.DefaultNamingStrategy;
import ortus.extension.orm.naming.SmartNamingStrategy;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ConfigurationBuilder;
import ortus.extension.orm.util.ExceptionUtil;
import ortus.extension.orm.util.HibernateUtil;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.naming.NamingStrategy;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

public class SessionFactoryData {

    /**
     * Use during ORM initialization for tracking the in-progress list of Component entities.
     */
    public List<Component> tmpList;

    /**
     * HashMap of datasources by datasource name
     */
    private final Map<Key, DataSource> sources = new HashMap<>();
    private final Map<Key, Map<String, CFCInfo>> cfcs = new HashMap<>();
    private final Map<Key, DataSourceConfig> configurations = new HashMap<>();
    private final Map<Key, SessionFactory> factories = new HashMap<>();
    private final Map<Key, QueryPlanCache> queryPlanCaches = new HashMap<>();

    private final ORMConfiguration ormConf;
    private NamingStrategy namingStrategy;
    private final HibernateORMEngine engine;
    private Struct tableInfo = CommonUtil.createStruct();
    private String cfcNamingStrategy;

    /**
     * Assists in "integrating" Hibernate events with the CFML EventHandler and entity CFCs
     */
    private EventListenerIntegrator eventListenerIntegrator;

    public SessionFactoryData( HibernateORMEngine engine, ORMConfiguration ormConf ) {
        this.engine                  = engine;
        this.ormConf                 = ormConf;
        this.eventListenerIntegrator = new EventListenerIntegrator();
    }

    public ORMConfiguration getORMConfiguration() {
        return ormConf;
    }

    public HibernateORMEngine getEngine() {
        return engine;
    }

    public QueryPlanCache getQueryPlanCache( Key datasSourceName ) {
        QueryPlanCache qpc = queryPlanCaches.get( datasSourceName );
        if ( qpc == null ) {
            qpc = ( ( SessionFactoryImpl ) this.getFactory( datasSourceName ) ).getQueryPlanCache();
            queryPlanCaches.put( datasSourceName, qpc );
        }
        return qpc;
    }

    /**
     * Retrieve the configured naming strategy. Will introspect and check this.ormConf property to determine the naming
     * strategy.
     *
     * TODO: Convert this to set at constructor time! There's no point in checking and initializing the naming strategy
     * upon calling the getter.
     *
     * @return one of CFCNamingStrategy|SmartNamingStrategy|DefaultNamingStrategy
     *
     * @throws PageException
     */
    public NamingStrategy getNamingStrategy() throws PageException {
        if ( namingStrategy == null ) {
            String strNamingStrategy = ormConf.namingStrategy();
            if ( Util.isEmpty( strNamingStrategy, true ) ) {
                namingStrategy = DefaultNamingStrategy.INSTANCE;
            } else {
                strNamingStrategy = strNamingStrategy.trim();
                if ( "default".equalsIgnoreCase( strNamingStrategy ) )
                    namingStrategy = DefaultNamingStrategy.INSTANCE;
                else if ( "smart".equalsIgnoreCase( strNamingStrategy ) )
                    namingStrategy = SmartNamingStrategy.INSTANCE;
                else {
                    CFCNamingStrategy cfcNS = new CFCNamingStrategy(
                            cfcNamingStrategy == null ? strNamingStrategy : cfcNamingStrategy );
                    cfcNamingStrategy = cfcNS.getComponent().getPageSource().getComponentName();
                    namingStrategy    = cfcNS;

                }
            }
        }
        if ( namingStrategy == null )
            return DefaultNamingStrategy.INSTANCE;
        return namingStrategy;
    }

    public CFCInfo checkExistent( PageContext pc, Component cfc ) throws PageException {
        CFCInfo info = getCFC( HibernateCaster.getEntityName( cfc ), null );
        if ( info != null )
            return info;

        String message = String.format( "there is no mapping definition for component [%s]", cfc.getAbsName() );
        throw ExceptionUtil.createException( this, null, message, null );
    }

    public List<String> getEntityNames() {
        List<String> names = new ArrayList<>();
        for ( Map<String, CFCInfo> entityTypes : cfcs.values() ) {
            for ( CFCInfo entityType : entityTypes.values() ) {
                names.add( HibernateCaster.getEntityName( entityType.getCFC() ) );
            }
        }
        return names;
    }

    public Component getEntityByEntityName( String entityName, boolean unique ) throws PageException {
        Component cfc;

        // first check cfcs for this entity
        CFCInfo info = getCFC( entityName, null );
        if ( info != null ) {
            cfc = info.getCFC();
            return unique ? ( Component ) cfc.duplicate( false ) : cfc;
        }

        // if parsing is in progress, the cfc can be found here
        if ( hasTempCFCs() ) {
            for ( Component entityType : tmpList ) {
                if ( HibernateCaster.getEntityName( entityType ).equalsIgnoreCase( entityName ) )
                    return unique ? ( Component ) entityType.duplicate( false ) : entityType;
            }
        }
        String message = String.format( "entity [%s] does not exist", entityName );
        throw ExceptionUtil.createException( ( ORMSession ) null, null, message, null );
    }

    public Component getEntityByCFCName( String cfcName, boolean unique ) throws PageException {
        String name = cfcName;
        int pointIndex = cfcName.lastIndexOf( '.' );
        if ( pointIndex != -1 ) {
            name = cfcName.substring( pointIndex + 1 );
        } else
            cfcName = null;

        Component cfc;
        List<String> names = new ArrayList<>();

        if ( hasTempCFCs() ) {
            for ( Component entity : tmpList ) {
                names.add( entity.getName() );
                if ( HibernateUtil.isEntity( ormConf, entity, cfcName, name ) ) // if(cfc.equalTo(name))
                    return unique ? ( Component ) entity.duplicate( false ) : entity;
            }
        } else {
            // search cfcs
            Iterator<Map<String, CFCInfo>> it = cfcs.values().iterator();
            Map<String, CFCInfo> _cfcs;
            while ( it.hasNext() ) {
                _cfcs = it.next();
                Iterator<CFCInfo> _it = _cfcs.values().iterator();
                while ( _it.hasNext() ) {
                    cfc = _it.next().getCFC();
                    names.add( cfc.getName() );
                    if ( HibernateUtil.isEntity( ormConf, cfc, cfcName, name ) ) // if(cfc.instanceOf(name))
                        return unique ? ( Component ) cfc.duplicate( false ) : cfc;
                }
            }
        }

        CFCInfo info = getCFC( name, null );
        if ( info != null ) {
            cfc = info.getCFC();
            return unique ? ( Component ) cfc.duplicate( false ) : cfc;
        }

        String cfcNameClause = Util.isEmpty( cfcName ) ? "" : String.format( "with cfc name [%s]", cfcName );
        String message = String.format( "entity [%s] %s does not exist, existing  entities are [%s]", name, cfcNameClause,
                CFMLEngineFactory.getInstance().getListUtil().toList( names, ", " ) );
        throw ExceptionUtil.createException( message );

    }

    /**
     * Get the Hibernate configuration for the given datasource name.
     *
     * @param ds
     *           Datasource object
     *
     * @return an instance of the {@link ortus.extension.orm.jdbc.DataSourceConfig} object
     */
    public DataSourceConfig getConfiguration( DataSource ds ) {
        return configurations.get( CommonUtil.toKey( ds.getName() ) );
    }

    /**
     * Get the Hibernate configuration for the given datasource name.
     *
     * @param key
     *            Datasource name, as a Lucee collection key
     *
     * @return an instance of the {@link ortus.extension.orm.jdbc.DataSourceConfig} object
     */
    public DataSourceConfig getConfiguration( Key key ) {
        return configurations.get( key );
    }

    /**
     * Use the ConfigurationBuilder to build a configuration object using the provided arguments.
     *
     * @param log
     *                               Lucee logger to use for logging
     * @param mappings
     *                               A string of generated or loaded Hibernate mapping XML.
     * @param ds
     *                               The datasource to use
     * @param user
     *                               The username credential to use for the given datasource
     * @param pass
     *                               The password credential to use for the given datasource
     * @param applicationContextName
     *                               Name of the CFML application, used to ensure ORM configuration is only built once per application, and
     *                               only cleared on ORMReload.
     *
     * @throws PageException
     * @throws SQLException
     * @throws IOException
     */
    public void setConfiguration( Log log, String mappings, DataSource ds, String user, String pass,
            String applicationContextName ) throws PageException, SQLException, IOException {

        Configuration configuration = new ConfigurationBuilder().withDatasource( ds ).withDatasourceCreds( user, pass )
                .withORMConfig( getORMConfiguration() ).withEventListener( getEventListenerIntegrator() )
                .withApplicationName( applicationContextName ).withXMLMappings( mappings ).withLog( log ).build();
        configurations.put( CommonUtil.toKey( ds.getName() ), new DataSourceConfig( ds, configuration ) );
        HibernateSessionFactory.schemaExport( log, configuration, ds, user, pass, this );
    }

    /**
     * Build a Hibernate SessionFactory for this datasource name.
     *
     * Uses the Thread Context ClassLoader, presumably to allow Hibernate classes running inside the Hibernate OSGI bundle
     * to talk to the lucee components outside that lucee bundle.
     *
     * @param datasSourceName
     *                        Name of the datasource for which to build and configure a session factory.
     */
    public SessionFactory buildSessionFactory( Key datasSourceName ) {
        DataSourceConfig dsc = getConfiguration( datasSourceName );
        if ( dsc == null )
            throw new RuntimeException( "cannot build factory because there is no configuration" ); // this should never
                                                                                                    // happen

        /**
         * TODO: Investigate OSGISessionFactoryService
         * https://docs.jboss.org/hibernate/orm/5.4/javadocs/org/hibernate/osgi/OsgiSessionFactoryService.html
         */
        Thread thread = Thread.currentThread();
        ClassLoader old = thread.getContextClassLoader();
        SessionFactory sf;
        try {
            // use the core classloader
            thread.setContextClassLoader( CFMLEngineFactory.getInstance().getClass().getClassLoader() );
            sf = dsc.config.buildSessionFactory();
        } finally {
            // reset
            thread.setContextClassLoader( old );
        }

        factories.put( datasSourceName, sf );
        return sf;
    }

    /**
     * Get the Hibernate SessionFactory for this datasource name. If not found, a new SessionFactory will be configured
     * and built.
     *
     * @param datasSourceName
     *                        Name of the datasource for which to retrieve the SessionFactory.
     */
    public SessionFactory getFactory( Key datasSourceName ) {
        SessionFactory factory = factories.get( datasSourceName );
        if ( factory != null && factory.isClosed() )
            factory = null;
        if ( factory == null && getConfiguration( datasSourceName ) != null )
            factory = buildSessionFactory( datasSourceName );// this should never be happen
        return factory;
    }

    /**
     * Reset the session factory and clear all known configuration.
     */
    public void reset() {
        configurations.clear();
        for ( SessionFactory factory : factories.values() ) {
            factory.close();
        }
        factories.clear();
        // namingStrategy=null; because the ormconf not change, this has not to change
        // as well
        tableInfo = CommonUtil.createStruct();
    }

    /**
     * Retrieve metadata from database table for use in generating a hibernate mapping to match the existing database
     * structure. Typically only used if `useDBForMapping` is enabled in the ORM configuration.
     *
     * Once queried and built, will store table metadata in <code>this.tableInfo</code> for faster retrieval.
     *
     * @param dc
     *                  Datasource connection object
     * @param tableName
     *                  Table name to retrieve / build entity mapping from.
     *
     * @return Struct of table information.
     *
     * @throws PageException
     */
    public Struct getTableInfo( DatasourceConnection dc, String tableName ) throws PageException {
        Collection.Key keyTableName = new KeyImpl( tableName );
        Struct columnsInfo = ( Struct ) tableInfo.get( keyTableName, null );
        if ( columnsInfo != null )
            return columnsInfo;

        columnsInfo = HibernateUtil.checkTable( dc, tableName, this );
        tableInfo.setEL( keyTableName, columnsInfo );
        return columnsInfo;
    }

    /**
     * Store a CFCInfo object as a known entity/entity mapping combination. The CFCs are stored in a map where the key
     * is the datasource, so if this datasource is not found in the map it will be populated as a new inner map of
     * entity mappings by datasource.
     *
     * @param entityName
     *                   Name of the entity to store. This will be the key for the inner hashmap.
     * @param info
     *                   CFCInfo object, containing datasource, component data, and XML mapping string.
     */
    public void addCFC( String entityName, CFCInfo info ) {
        DataSource ds = info.getDataSource();
        Key dsn = CommonUtil.toKey( ds.getName() );

        Map<String, CFCInfo> map = cfcs.get( dsn );
        if ( map == null )
            cfcs.put( dsn, map = new HashMap<>() );
        map.put( HibernateUtil.sanitizeEntityName( entityName ), info );
        sources.put( dsn, ds );
    }

    /**
     * Retrieve the CFCInfo object for this entity name from the known entities across all datasources.
     *
     * @param entityName
     *                     Name of entity to retrieve.
     * @param defaultValue
     *                     Default CFCInfo object to return. (Unused, consider deleting.)
     *
     * @return
     */
    CFCInfo getCFC( String entityName, CFCInfo defaultValue ) {
        Iterator<Map<String, CFCInfo>> it = cfcs.values().iterator();
        while ( it.hasNext() ) {
            CFCInfo info = it.next().get( HibernateUtil.sanitizeEntityName( entityName ) );
            if ( info != null )
                return info;
        }
        return defaultValue;
    }

    /**
     * Getter for map of entity types by datasource name.
     *
     * @return Map where key=dsn, value = Map (where key=entity name, value=CFCInfo object containing the component and
     *         the XML mapping.)
     */
    public Map<Key, Map<String, CFCInfo>> getCFCs() {
        return cfcs;
    }

    /**
     * Retrieve all known entity types for the provided datasource name.
     *
     * @param datasSourceName
     *                        Datasource Key name to filter on.
     *
     * @return HashMap of entities by entity name.
     */
    public Map<String, CFCInfo> getCFCs( Key datasSourceName ) {
        Map<String, CFCInfo> rtn = cfcs.get( datasSourceName );
        if ( rtn == null )
            return new HashMap<>();
        return rtn;
    }

    /**
     * Reset the map of known entity types and datasource names for this ORM application.
     *
     * This should ONLY be called at startup!
     */
    public void clearCFCs() {
        cfcs.clear();
    }

    /**
     * Get a count of all known entity types, including entity children.
     */
    public int sizeCFCs() {
        Iterator<Map<String, CFCInfo>> it = cfcs.values().iterator();
        int size = 0;
        while ( it.hasNext() ) {
            size += it.next().size();
        }
        return size;
    }

    /**
     * Retrieve an array of known datasources configured in persistent entities.
     *
     * @return An array populated with all known datasources.
     */
    public DataSource[] getDataSources() {
        return sources.values().toArray( new DataSource[ sources.size() ] );
    }

    /**
     * Call all SessionFactory objects to ensure they're all built
     */
    public void init() {
        Iterator<Key> it = cfcs.keySet().iterator();
        while ( it.hasNext() ) {
            getFactory( it.next() );
        }
    }

    /**
     * Get datasource configuration by datasource name.
     *
     * @param datasSourceName
     *                        Key matching name of datasource to retrieve.
     *
     * @return Fully configured datasource object.
     */
    public DataSource getDataSource( Key datasSourceName ) {
        return sources.get( datasSourceName );
    }

    /**
     * Retrieve the event integrator configured for this ORM application.
     */
    public EventListenerIntegrator getEventListenerIntegrator() {
        return eventListenerIntegrator;
    }

    /**
     * See if we have loaded entities. (ORM-ish CFML Components)
     *
     * @return True if entity list is not null and not empty.
     */
    public boolean hasTempCFCs() {
        return tmpList != null && !tmpList.isEmpty();
    }
}
