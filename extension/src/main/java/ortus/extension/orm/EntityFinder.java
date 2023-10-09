package ortus.extension.orm;

import java.util.ArrayList;
import java.util.List;

import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.HibernateUtil;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.util.ResourceUtil;
import lucee.runtime.Component;
import lucee.runtime.InterfacePage;
import lucee.runtime.Mapping;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.util.TemplateUtil;

/**
 * I assist with finding persistent components to map as Hibernate entitites within the given CFML application.
 */
public class EntityFinder {

    /**
     * The directory to search for Components.
     *
     * Maps to ORMConfiguration.getCFCLocations().
     */
    private Resource[] locations;

    /**
     * Should we throw an error upon failing to parse or load a certain CFC?
     *
     * This is the inverse of the ORMCOnfiguration's `skipCFCWithError` value.
     */
    private Boolean failOnError;

    /**
     * Lucee's TemplateUtil used for loading the component from source.
     *
     * Acquired from the CFML engine == not testable. 😢
     */
    private TemplateUtil templateUtil;

    /**
     * Lucee's ResourceUtil used for various file path util methods.
     *
     * Acquired from the CFML engine == not testable. 😢
     */
    private ResourceUtil resourceUtil;

    /**
     * A Lucee resource filter used to filter the scanned files on disk to just .cfc files.
     */
    private ResourceFilter filter;

    public EntityFinder( Resource[] locations, Boolean failOnError ) {
        this.locations    = locations;
        this.failOnError  = failOnError;
        this.templateUtil = CFMLEngineFactory.getInstance().getTemplateUtil();
        this.resourceUtil = CFMLEngineFactory.getInstance().getResourceUtil();
        this.filter       = this.resourceUtil.getExtensionResourceFilter( "cfc", true );
    }

    /**
     * Find, load, and return all persistent entities in this CFML application
     *
     * @param pc
     *           Lucee PageContext object
     *
     * @throws PageException
     */
    public List<Component> loadComponents( PageContext pc ) throws PageException {

        List<Component> components = new ArrayList<>();
        loadComponents( pc, components );
        return components;
    }

    /**
     * Load persistent entities from the given directory
     *
     * @param pc
     *                   Lucee PageContext object
     * @param components
     *                   The current list of components. Any discovered components will be appended to this list.
     *
     * @throws PageException
     */
    private void loadComponents( PageContext pc, List<Component> components ) throws PageException {
        Mapping[] mappings = createFileMappings( pc, this.locations );
        ApplicationContext ac = pc.getApplicationContext();
        Mapping[] existing = ac.getComponentMappings();
        if ( existing == null )
            existing = new Mapping[ 0 ];
        try {
            Mapping[] tmp = new Mapping[ existing.length + 1 ];
            for ( int i = 1; i < tmp.length; i++ ) {
                tmp[ i ] = existing[ i - 1 ];
            }
            ac.setComponentMappings( tmp );
            for ( int i = 0; i < this.locations.length; i++ ) {
                if ( this.locations[ i ] != null && this.locations[ i ].isDirectory() ) {
                    tmp[ 0 ] = mappings[ i ];
                    ac.setComponentMappings( tmp );
                    loadComponents( pc, mappings[ i ], components, this.locations[ i ] );
                }
            }
        } finally {
            ac.setComponentMappings( existing );
        }
    }

    /**
     * Load persistent entities from the given cfclocation Mapping directory
     *
     * @param pc
     *                    Lucee PageContext object
     * @param cfclocation
     *                    Lucee {@link lucee.runtime.Mapping} pointing to a directory where .cfc Components are located.
     * @param components
     *                    The current list of components. Any discovered components will be appended to this list.
     * @param res
     *                    The directory to search for Components, OR the file to (potentially) import into the Hibernate
     *                    configuration.
     *
     * @throws PageException
     */
    private void loadComponents( PageContext pc, Mapping cfclocation, List<Component> components, Resource res )
            throws PageException {
        if ( res == null )
            return;

        if ( res.isDirectory() ) {
            Resource[] children = res.listResources( this.filter );

            // first load all files
            for ( int i = 0; i < children.length; i++ ) {
                if ( children[ i ].isFile() )
                    loadComponents( pc, cfclocation, components, children[ i ] );
            }

            // and then invoke subfiles
            for ( int i = 0; i < children.length; i++ ) {
                if ( children[ i ].isDirectory() )
                    loadComponents( pc, cfclocation, components, children[ i ] );
            }
        } else if ( res.isFile() ) {
            if ( !HibernateUtil.isApplicationName( res.getName() ) ) {
                try {
                    PageSource ps = getPageSource( pc, cfclocation, res );

                    Page p = this.templateUtil.loadPage( pc, ps, true );
                    if ( ! ( p instanceof InterfacePage ) ) {
                        String name = res.getName();
                        name = getFilenameNoExtension( name, name );
                        Component cfc = this.templateUtil.loadComponent( pc, p, name, true, true, false, true );
                        if ( cfc.isPersistent() ) {
                            components.add( cfc );
                        }
                    }
                } catch ( PageException e ) {
                    if ( this.failOnError )
                        throw e;
                    // e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find and get the PageSource object represented by the Resource object at location X.
     *
     * @cfclocation Lucee Mapping to the directory containing this file resource
     *
     * @res Lucee Resource (file object) for a possible CFML component.
     */
    private PageSource getPageSource( PageContext pc, Mapping cfclocation, Resource res ) {
        // @TODO: still a bad solution
        PageSource ps = pc.toPageSource( res, null );
        if ( ps == null || ps.getComponentName().indexOf( ".." ) != -1 ) {
            PageSource ps2 = null;
            Resource root = cfclocation.getPhysical();
            String path = this.resourceUtil.getPathToChild( res, root );
            if ( !Util.isEmpty( path, true ) ) {
                ps2 = cfclocation.getPageSource( path );
            }
            if ( ps2 != null )
                ps = ps2;
        }
        return ps;
    }

    /**
     * Create CF mappings for locating persistent entities.
     * <p>
     * Used when importing persistent entities from the configured <code>this.ormsettings.cfclocation</code> array.
     *
     * @param pc
     *                  Lucee PageContext
     * @param resources
     *                  Array of Resource objects, i.e. a file path
     *
     * @return a Mapping object used to locate a file resource
     */
    private Mapping[] createFileMappings( PageContext pc, Resource[] resources ) {
        Mapping[] mappings = new Mapping[ resources.length ];
        Config config = pc.getConfig();
        for ( int i = 0; i < mappings.length; i++ ) {
            mappings[ i ] = CommonUtil.createMapping( config, "/", resources[ i ].getAbsolutePath() );
        }
        return mappings;
    }

    private String getFilenameNoExtension( String filename, String defaultValue ) {
        int index = filename.lastIndexOf( '.' );
        if ( index == -1 )
            return defaultValue;
        return filename.substring( 0, index );
    }

}
