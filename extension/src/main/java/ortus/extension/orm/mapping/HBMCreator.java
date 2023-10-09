package ortus.extension.orm.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import ortus.extension.orm.ColumnInfo;
import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.SessionFactoryData;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ExceptionUtil;
import ortus.extension.orm.util.HibernateUtil;
import ortus.extension.orm.util.ORMConfigurationUtil;
import ortus.extension.orm.util.XMLUtil;

import lucee.runtime.type.Struct;
import lucee.commons.io.res.Resource;

/**
 * Hibernate XML mapping string generator.
 *
 * This will be deprecated in the future due to Hibernate itself deprecating the use of hbm.xml mappings in v6.x. See
 * https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html#_deprecation_of_hbm_xml_mappings
 */
public class HBMCreator {

    private HBMCreator() {
        throw new IllegalStateException( "Utility class; please don't instantiate!" );
    }

    /**
     * Hibernate DOCTYPE mapping ID
     *
     * @see <a href=
     *      "https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#schema-generation-database-objects">Hibernate
     *      documentation schema-generation-database-objects</a>
     */
    public static final String HIBERNATE_3_PUBLIC_ID = "-//Hibernate/Hibernate Mapping DTD 3.0//EN";

    /**
     * Hibernate doctype reference
     *
     * @see <a href=
     *      "https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#schema-generation-database-objects">Hibernate
     *      documentation schema-generation-database-objects</a>
     */
    public static final String HIBERNATE_3_SYSTEM_ID = "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd";

    /**
     * Full XML doctype for Hibernate mappings
     *
     * @see <a href=
     *      "https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#schema-generation-database-objects">Hibernate
     *      documentation schema-generation-database-objects</a>
     */
    public static final String HIBERNATE_3_DOCTYPE_DEFINITION = "<!DOCTYPE hibernate-mapping PUBLIC \"" + HIBERNATE_3_PUBLIC_ID
            + "\" \"" + HIBERNATE_3_SYSTEM_ID + "\">";

    private static final Collection.Key PROPERTY = CommonUtil.createKey( "property" );
    private static final Collection.Key LINK_TABLE = CommonUtil.createKey( "linktable" );
    private static final Collection.Key CFC = CommonUtil.createKey( "cfc" );
    private static final Collection.Key GENERATOR = CommonUtil.createKey( "generator" );
    private static final Collection.Key PARAMS = CommonUtil.createKey( "params" );
    private static final Collection.Key SEQUENCE = CommonUtil.createKey( "sequence" );
    private static final Collection.Key UNIQUE_KEY_NAME = CommonUtil.createKey( "uniqueKeyName" );
    private static final Collection.Key GENERATED = CommonUtil.createKey( "generated" );
    private static final Collection.Key FIELDTYPE = CommonUtil.createKey( "fieldtype" );
    private static final Collection.Key KEY = CommonUtil.createKey( "key" );
    private static final Collection.Key TYPE = CommonUtil.createKey( "type" );

    /**
     *  @TODO: Move this into some CFConstants class, or somewhere that both HBMCreator and HibernateCaster can reference it.
     * @TODO: For 7.0, Migrate to Map.of() or Map.ofEntries in Java 9+
     */
    public static final class Relationships {
        public static final String ONE_TO_MANY = "one-to-many";
        public static final String MANY_TO_MANY = "many-to-many";
        public static final String MANY_TO_ONE = "many-to-one";
        public static final String ONE_TO_ONE = "one-to-one";

        /**
         * Identify whether the given string denotes one of the four main relationship types:
         * - one-to-many
         * - one-to-one
         * - many-to-many
         * - many-to-one
         * 
         * @param fieldType Field type string from a persistent property, like "one-to-many" or "id"
         * @return Boolean
         */
        public static boolean isRelationshipType( String fieldType ){
            return 
                ONE_TO_MANY.equalsIgnoreCase(fieldType) ||
                MANY_TO_MANY.equalsIgnoreCase(fieldType) ||
                MANY_TO_ONE.equalsIgnoreCase(fieldType) ||
                ONE_TO_ONE.equalsIgnoreCase(fieldType);
        }
    }

    /**
     * Generate an XML node tree defining a Hibernate mapping for the given Component
     *
     * @param pc
     *             Lucee PageContext object
     * @param dc
     *             Lucee DatasourceConnection object
     * @param cfc
     *             Lucee Component to create the mapping XML for
     * @param data
     *             SessionFactoryData instance to pull Hibernate metadata from
     *
     * @return XML root node
     *
     * @throws PageException
     */
    public static Element createXMLMapping( PageContext pc, DatasourceConnection dc, Component cfc, SessionFactoryData data )
            throws PageException {
        Document doc = XMLUtil.newDocument();

        Element hibernateMapping = doc.createElement( "hibernate-mapping" );
        doc.appendChild( hibernateMapping );

        // @TODO: Support for embeded objects
        Struct meta = cfc.getMetaData( pc );

        String extend = cfc.getExtends();
        boolean isClass = Util.isEmpty( extend );

        Property[] _props = getProperties( pc, cfc, dc, meta, isClass, true, data );

        Map<String, PropertyCollection> joins = new HashMap<>();
        PropertyCollection propColl = splitJoins( cfc, joins, _props, data );

        StringBuilder comment = new StringBuilder();
        comment.append( "\nsource:" ).append( cfc.getPageSource().getDisplayPath() );
        comment.append( "\ncompilation-time:" )
                .append( CommonUtil.createDateTime( HibernateUtil.getCompileTime( pc, cfc.getPageSource() ) ) );
        comment.append( "\ndatasource:" ).append( dc.getDatasource().getName() );
        comment.append( "\n" );

        hibernateMapping.appendChild( doc.createComment( comment.toString() ) );

        if ( !isClass && !cfc.isBasePeristent() ) {
            isClass = true;
        }

        Element join = null;
        boolean doTable = true;

        Element clazz;
        if ( isClass ) {
            clazz = doc.createElement( "class" );
            hibernateMapping.appendChild( clazz );
        }
        // extended CFC
        else {
            // MZ: Fetches one level deep
            _props   = getProperties( pc, cfc, dc, meta, isClass, false, data );
            // MZ: Reinitiate the property collection
            propColl = splitJoins( cfc, joins, _props, data );

            String ext = CommonUtil.last( extend, "." ).trim();
            Component base = data.getEntityByCFCName( ext, false );
            ext = HibernateCaster.getEntityName( base );

            String discriminatorValue = toString( cfc, null, meta, "discriminatorValue", data );
            if ( !Util.isEmpty( discriminatorValue, true ) ) {
                doTable = false;
                clazz   = doc.createElement( "subclass" );
                hibernateMapping.appendChild( clazz );
                clazz.setAttribute( "extends", ext );
                clazz.setAttribute( "discriminator-value", discriminatorValue );

                String joincolumn = toString( cfc, null, meta, "joincolumn", false, data );
                if ( !Util.isEmpty( joincolumn ) ) {
                    join = doc.createElement( "join" );
                    clazz.appendChild( join );
                    doTable = true;
                    Element key = doc.createElement( "key" );
                    join.appendChild( key );
                    key.setAttribute( "column", formatColumn( joincolumn, data ) );
                }

            } else {
                // MZ: Match on joinColumn for a joined subclass, otherwise use a union subclass
                String joinColumn = toString( cfc, null, meta, "joincolumn", false, data );
                if ( !Util.isEmpty( joinColumn, true ) ) {
                    clazz = doc.createElement( "joined-subclass" );
                    hibernateMapping.appendChild( clazz );
                    clazz.setAttribute( "extends", ext );
                    Element key = doc.createElement( "key" );
                    clazz.appendChild( key );
                    key.setAttribute( "column", formatColumn( joinColumn, data ) );
                } else {
                    // MZ: When no joinColumn exists, default to an explicit table per class
                    clazz = doc.createElement( "union-subclass" );
                    clazz.setAttribute( "extends", ext );
                    doTable = true;
                    hibernateMapping.appendChild( clazz );
                }

            }
        }

        addGeneralClassAttributes( cfc, meta, clazz, data );

        if ( join != null )
            clazz = join;
        if ( doTable )
            addGeneralTableAttributes( dc, cfc, meta, clazz, data );

        Struct columnsInfo = null;
        if ( data.getORMConfiguration().useDBForMapping() ) {
            columnsInfo = data.getTableInfo( dc, getTableName( meta, cfc, data ) );
        }

        if ( isClass )
            setCacheStrategy( cfc, null, doc, meta, clazz, data );

        // id
        if ( isClass )
            addId( cfc, clazz, propColl, columnsInfo, data );

        // discriminator
        if ( isClass )
            addDiscriminator( cfc, doc, clazz, meta, data );

        // version
        if ( isClass )
            addVersion( cfc, clazz, propColl, data );

        // property
        addProperty( cfc, clazz, propColl, columnsInfo, data );

        // relations
        addRelation( cfc, clazz, pc, propColl, dc, data );

        // collection
        addCollection( cfc, clazz, propColl, data );

        // join
        addJoin( cfc, clazz, pc, joins, columnsInfo, dc, data );

        return hibernateMapping;
    }

    private static Property[] getProperties( PageContext pc, Component cfc, DatasourceConnection dc, Struct meta, boolean isClass,
            boolean recursivePersistentMappedSuperclass, SessionFactoryData data ) throws PageException {
        Property[] _props;
        if ( recursivePersistentMappedSuperclass ) {
            _props = CommonUtil.getProperties( cfc, true, true, true, true );
        } else {
            _props = cfc.getProperties( true, false, false, false );
        }

        if ( isClass && _props.length == 0 && data.getORMConfiguration().useDBForMapping() ) {
            if ( meta == null )
                meta = cfc.getMetaData( pc );
            _props = HibernateUtil.createPropertiesFromTable( dc, getTableName( meta, cfc, data ) );
        }
        return _props;
    }

    private static void addId( Component cfc, Element clazz, PropertyCollection propColl,
            Struct columnsInfo, SessionFactoryData data ) throws PageException {
        Property[] _ids = getIds( cfc, propColl, data );

        if ( _ids.length == 1 )
            createXMLMappingId( cfc, clazz, _ids[ 0 ], columnsInfo, data );
        else if ( _ids.length > 1 )
            createXMLMappingCompositeId( cfc, clazz, _ids, columnsInfo, data );
        else {
            String message = String.format( "missing id property for entity [%s]", HibernateCaster.getEntityName( cfc ) );
            throw ExceptionUtil.createException( data, cfc, message, null );
        }
    }

    private static PropertyCollection splitJoins( Component cfc, Map<String, PropertyCollection> joins, Property[] props,
            SessionFactoryData data ) {
        Struct sct = CommonUtil.createStruct();
        ArrayList<Property> others = new ArrayList<>();
        List<Property> list;
        String table;
        Property prop;
        String fieldType;
        boolean isJoin;
        for ( int i = 0; i < props.length; i++ ) {
            prop  = props[ i ];
            table = getTable( cfc, prop, data );
            // joins
            if ( !Util.isEmpty( table, true ) ) {
                isJoin = true;
                // wrong field type
                try {
                    fieldType = toString( cfc, prop, sct, FIELDTYPE, false, data );

                    if ( "collection".equalsIgnoreCase( fieldType ) )
                        isJoin = false;
                    else if ( "primary".equals( fieldType ) )
                        isJoin = false;
                    else if ( "version".equals( fieldType ) )
                        isJoin = false;
                    else if ( "timestamp".equals( fieldType ) )
                        isJoin = false;
                } catch ( PageException e ) {
                }

                // missing column
                String columns = null;
                try {
                    if ( CommonUtil.isRelated( props[ i ] ) ) {
                        columns = toString( cfc, props[ i ], prop.getDynamicAttributes(), "fkcolumn", data );
                    } else {
                        columns = toString( cfc, props[ i ], prop.getDynamicAttributes(), "joincolumn", data );
                    }
                } catch ( PageException e ) {
                }
                if ( Util.isEmpty( columns ) )
                    isJoin = false;

                if ( isJoin ) {
                    table = table.trim();
                    list  = ( List<Property> ) sct.get( table, null );
                    if ( list == null ) {
                        list = new ArrayList<>();
                        sct.setEL( CommonUtil.createKey( table ), list );
                    }
                    list.add( prop );
                    continue;
                }
            }
            others.add( prop );
        }

        // fill to joins
        Iterator<Entry<Key, Object>> it = sct.entryIterator();
        Entry<Key, Object> e;
        while ( it.hasNext() ) {
            e    = it.next();
            list = ( List<Property> ) e.getValue();
            joins.put( e.getKey().getString(), new PropertyCollection( e.getKey().getString(), list ) );
        }

        return new PropertyCollection( null, others );
    }

    private static Property[] getIds( Component cfc, PropertyCollection pc, SessionFactoryData data ) {
        return getIds( cfc, pc.getProperties(), pc.getTableName(), false, data );
    }

    private static Property[] getIds( Component cfc, Property[] props, String tableName, boolean ignoreTableName,
            SessionFactoryData data ) {
        ArrayList<Property> ids = new ArrayList<>();
        for ( int y = 0; y < props.length; y++ ) {
            if ( !ignoreTableName && !hasTable( cfc, props[ y ], tableName, data ) )
                continue;

            String fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, null ), null );
            if ( "id".equalsIgnoreCase( fieldType ) || CommonUtil.listFindNoCaseIgnoreEmpty( fieldType, "id", ',' ) != -1 )
                ids.add( props[ y ] );
        }

        // no id field defined
        if ( ids.isEmpty() ) {
            String fieldType;
            for ( int y = 0; y < props.length; y++ ) {
                if ( !ignoreTableName && !hasTable( cfc, props[ y ], tableName, data ) )
                    continue;
                fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, null ), null );
                if ( Util.isEmpty( fieldType, true ) && props[ y ].getName().equalsIgnoreCase( "id" ) ) {
                    ids.add( props[ y ] );
                    props[ y ].getDynamicAttributes().setEL( FIELDTYPE, "id" );
                }
            }
        }

        // still no id field defined
        if ( ids.isEmpty() && props.length > 0 ) {
            String owner = props[ 0 ].getOwnerName();
            if ( !Util.isEmpty( owner ) )
                owner = CommonUtil.last( owner, "." ).trim();

            String fieldType;
            if ( !Util.isEmpty( owner ) ) {
                String id = owner + "id";
                for ( int y = 0; y < props.length; y++ ) {
                    if ( !ignoreTableName && !hasTable( cfc, props[ y ], tableName, data ) )
                        continue;
                    fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, null ), null );
                    if ( Util.isEmpty( fieldType, true ) && props[ y ].getName().equalsIgnoreCase( id ) ) {
                        ids.add( props[ y ] );
                        props[ y ].getDynamicAttributes().setEL( FIELDTYPE, "id" );
                    }
                }
            }
        }
        return ids.toArray( new Property[ ids.size() ] );
    }

    private static void addVersion( Component cfc, Element clazz, PropertyCollection propColl, SessionFactoryData data ) throws PageException {
        Property[] props = propColl.getProperties();
        for ( int y = 0; y < props.length; y++ ) {
            String fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, null ), null );
            if ( "version".equalsIgnoreCase( fieldType ) )
                createXMLMappingVersion( clazz, cfc, props[ y ], data );
            else if ( "timestamp".equalsIgnoreCase( fieldType ) )
                createXMLMappingTimestamp( clazz, cfc, props[ y ], data );
        }
    }

    private static void addCollection( Component cfc, Element clazz, PropertyCollection propColl, SessionFactoryData data ) throws PageException {
        Property[] props = propColl.getProperties();
        for ( int y = 0; y < props.length; y++ ) {
            String fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, "column" ), "column" );
            if ( "collection".equalsIgnoreCase( fieldType ) )
                createXMLMappingCollection( clazz, cfc, props[ y ], data );
        }
    }

    private static void addJoin( Component cfc, Element clazz, PageContext pc, Map<String, PropertyCollection> joins,
            Struct columnsInfo, DatasourceConnection dc, SessionFactoryData data ) throws PageException {

        Iterator<Entry<String, PropertyCollection>> it = joins.entrySet().iterator();
        Entry<String, PropertyCollection> entry;
        while ( it.hasNext() ) {
            entry = it.next();
            addJoin( cfc, pc, columnsInfo, clazz, entry.getValue(), dc, data );
        }

    }

    private static void addJoin( Component cfc, PageContext pc, Struct columnsInfo, Element clazz, PropertyCollection coll,
            DatasourceConnection dc, SessionFactoryData data ) throws PageException {
        Property[] properties = coll.getProperties();
        if ( properties.length == 0 )
            return;

        Document doc = XMLUtil.getDocument( clazz );

        Element join = doc.createElement( "join" );
        clazz.appendChild( join );

        join.setAttribute( "table", escape( HibernateUtil.convertTableName( data, coll.getTableName() ) ) );

        Property first = properties[ 0 ];
        String schema = null, catalog = null, mappedBy = null, columns = null;
        if ( CommonUtil.isRelated( first ) ) {
            catalog = toString( cfc, first, first.getDynamicAttributes(), "linkcatalog", data );
            schema  = toString( cfc, first, first.getDynamicAttributes(), "linkschema", data );
            columns = toString( cfc, first, first.getDynamicAttributes(), "fkcolumn", data );

        } else {
            catalog  = toString( cfc, first, first.getDynamicAttributes(), "catalog", data );
            schema   = toString( cfc, first, first.getDynamicAttributes(), "schema", data );
            mappedBy = toString( cfc, first, first.getDynamicAttributes(), "mappedby", data );
            columns  = toString( cfc, first, first.getDynamicAttributes(), "joincolumn", data );
        }

        if ( !Util.isEmpty( catalog ) )
            join.setAttribute( "catalog", catalog );
        if ( !Util.isEmpty( schema ) )
            join.setAttribute( "schema", schema );

        Element key = doc.createElement( "key" );
        join.appendChild( key );
        if ( !Util.isEmpty( mappedBy ) )
            key.setAttribute( "property-ref", mappedBy );
        setColumn( doc, key, columns, data );

        addProperty( cfc, join, coll, columnsInfo, data );
        int count = addRelation( cfc, join, pc, coll, dc, data );

        if ( count > 0 )
            join.setAttribute( "inverse", "true" );

    }

    private static int addRelation( Component cfc, Element clazz, PageContext pc, PropertyCollection propColl, DatasourceConnection dc, SessionFactoryData data ) throws PageException {
        Property[] props = propColl.getProperties();
        int count = 0;
        for ( int y = 0; y < props.length; y++ ) {
            String fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, "column" ), "column" );
            if ( CFConstants.Relationships.ONE_TO_ONE.equalsIgnoreCase( fieldType ) ) {
                createXMLMappingOneToOne( clazz, cfc, props[ y ], data );
                count++;
            } else if ( CFConstants.Relationships.MANY_TO_ONE.equalsIgnoreCase( fieldType ) ) {
                createXMLMappingManyToOne( clazz, cfc, props[ y ], propColl, data );
                count++;
            } else if ( CFConstants.Relationships.ONE_TO_MANY.equalsIgnoreCase( fieldType ) ) {
                createXMLMappingOneToMany( dc, cfc, propColl, clazz, props[ y ], data );
                count++;
            } else if ( CFConstants.Relationships.MANY_TO_MANY.equalsIgnoreCase( fieldType ) ) {
                createXMLMappingManyToMany( dc, cfc, propColl, clazz, pc, props[ y ], data );
                count++;
            }
        }
        return count;
    }

    private static void addProperty( Component cfc, Element clazz, PropertyCollection propColl,
            Struct columnsInfo, SessionFactoryData data ) throws PageException {
        Property[] props = propColl.getProperties();
        for ( int y = 0; y < props.length; y++ ) {
            String fieldType = CommonUtil.toString( props[ y ].getDynamicAttributes().get( FIELDTYPE, "column" ), "column" );
            if ( "column".equalsIgnoreCase( fieldType ) )
                createXMLMappingProperty( clazz, cfc, props[ y ], columnsInfo, data );
        }
    }

    private static void addDiscriminator( Component cfc, Document doc, Element clazz,Struct meta,
            SessionFactoryData data ) throws DOMException, PageException {

        String str = toString( cfc, null, meta, "discriminatorColumn", data );
        if ( !Util.isEmpty( str, true ) ) {
            Element disc = doc.createElement( "discriminator" );
            clazz.appendChild( disc );
            disc.setAttribute( "column", formatColumn( str, data ) );
        }

    }

    private static void addGeneralClassAttributes( Component cfc, Struct meta, Element clazz,
            SessionFactoryData data ) throws PageException {

        // entity-name
        String str = toString( cfc, null, meta, "entityname", data );
        if ( Util.isEmpty( str, true ) )
            str = HibernateCaster.getEntityName( cfc );
        clazz.setAttribute( "entity-name", str );

        // batch-size
        Integer i = toInteger( cfc, meta, "batchsize", data );
        if ( i != null && i.intValue() > 0 )
            clazz.setAttribute( "batch-size", CommonUtil.toString( i ) );

        // dynamic-insert
        Boolean b = toBoolean( cfc, meta, "dynamicinsert", data );
        if ( b != null && b.booleanValue() )
            clazz.setAttribute( "dynamic-insert", "true" );

        // dynamic-update
        b = toBoolean( cfc, meta, "dynamicupdate", data );
        if ( b != null && b.booleanValue() )
            clazz.setAttribute( "dynamic-update", "true" );

        // lazy (dtd defintion:<!ATTLIST class lazy (true|false) #IMPLIED>)
        b = toBoolean( cfc, meta, "lazy", data );
        if ( b == null )
            b = Boolean.TRUE;
        clazz.setAttribute( "lazy", CommonUtil.toString( b.booleanValue() ) );

        // select-before-update
        b = toBoolean( cfc, meta, "selectbeforeupdate", data );
        if ( b != null && b.booleanValue() )
            clazz.setAttribute( "select-before-update", "true" );

        // optimistic-lock
        str = toString( cfc, null, meta, CFConstants.OptimisticLock.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim();
            if ( CFConstants.OptimisticLock.isValidValue( str ) )
                clazz.setAttribute( "optimistic-lock", str );
            else {
                String message = String.format(
                        "invalid value [%s] for attribute [%s] of tag [component], valid values are [%s]",
                        str,
                        CFConstants.OptimisticLock.ATTRIBUTE_NAME,
                        CFConstants.OptimisticLock.getPossibleValues().toString()
                );
                throw ExceptionUtil.createException( data, cfc, message, null );
            }
        }

        // read-only
        b = toBoolean( cfc, meta, "readOnly", data );
        if ( b != null && b.booleanValue() )
            clazz.setAttribute( "mutable", "false" );

        // rowid
        str = toString( cfc, null, meta, "rowid", data );
        if ( !Util.isEmpty( str, true ) )
            clazz.setAttribute( "rowid", str );

        // where
        str = toString( cfc, null, meta, "where", data );
        if ( !Util.isEmpty( str, true ) )
            clazz.setAttribute( "where", str );

    }

    private static void addGeneralTableAttributes( DatasourceConnection dc, Component cfc, Struct meta,
            Element clazz, SessionFactoryData data ) throws PageException {
        // table
        clazz.setAttribute( "table", escape( getTableName( meta, cfc, data ) ) );

        // catalog
        String str = toString( cfc, null, meta, "catalog", data );
        if ( str == null ) // empty string is allowed as input
            str = ORMConfigurationUtil.getCatalog( data.getORMConfiguration(), dc.getDatasource().getName() );

        if ( !Util.isEmpty( str, true ) )
            clazz.setAttribute( "catalog", str );

        // schema
        str = toString( cfc, null, meta, "schema", data );
        if ( str == null )// empty string is allowed as input
            str = ORMConfigurationUtil.getSchema( data.getORMConfiguration(), dc.getDatasource().getName() );
        if ( !Util.isEmpty( str, true ) )
            clazz.setAttribute( "schema", str );

    }

    /**
     * Detect and escape reserved words. Uses backticks for escaping reserved values.
     * 
     * @param str
     * @return The value, potentially wrapped with SQL escape characters.
     */
    private static String escape( String str ) {
        if ( HibernateUtil.isKeyword( str ) )
            return "`" + str + "`";
        return str;
    }

    private static String getTableName( Struct meta, Component cfc, SessionFactoryData data )
            throws PageException {
        String tableName = toString( cfc, null, meta, "table", data );
        if ( Util.isEmpty( tableName, true ) )
            tableName = HibernateCaster.getEntityName( cfc );
        return HibernateUtil.convertTableName( data, tableName );
    }

    private static String getTable( Component cfc, Property prop, SessionFactoryData data ) {
        try {
            return HibernateUtil.convertTableName( data, toString( cfc, prop, prop.getDynamicAttributes(), "table", data ) );
        } catch ( PageException e ) {
            return null;
        }
    }

    private static boolean hasTable( Component cfc, Property prop, String tableName, SessionFactoryData data ) {
        String t = getTable( cfc, prop, data );
        boolean left = Util.isEmpty( t, true );
        boolean right = Util.isEmpty( tableName, true );
        if ( left && right )
            return true;
        if ( left || right )
            return false;
        return tableName.trim().equalsIgnoreCase( t.trim() );
    }

    private static void createXMLMappingCompositeId( Component cfc, Element clazz, Property[] props, Struct columnsInfo,
            SessionFactoryData data ) throws PageException {
        Struct meta;

        Document doc = XMLUtil.getDocument( clazz );
        Element cid = doc.createElement( "composite-id" );
        clazz.appendChild( cid );

        Property prop;
        String fieldType;
        // ids
        for ( int y = 0; y < props.length; y++ ) {
            prop      = props[ y ];

            // do not add "key-property" for many-to-one
            meta      = prop.getDynamicAttributes();
            fieldType = toString( cfc, prop, meta, "fieldType", data );
            if ( CommonUtil.listFindNoCaseIgnoreEmpty( fieldType, CFConstants.Relationships.MANY_TO_ONE, ',' ) != -1 )
                continue;

            Element key = doc.createElement( "key-property" );
            cid.appendChild( key );

            // name
            key.setAttribute( "name", prop.getName() );

            // column
            Element column = doc.createElement( "column" );
            key.appendChild( column );

            String str = toString( cfc, prop, meta, "column", data );
            if ( Util.isEmpty( str, true ) )
                str = prop.getName();
            column.setAttribute( "name", formatColumn( str, data ) );
            ColumnInfo info = getColumnInfo( columnsInfo, str, null );

            str = toString( cfc, prop, meta, "sqltype", data );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "sql-type", str );
            str = toString( cfc, prop, meta, "length", data );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "length", str );

            String generator = toString( cfc, prop, meta, "generator", data );
            String type = getType( info, cfc, prop, meta, getDefaultTypeForGenerator( generator, "string" ), data );
            if ( !Util.isEmpty( type ) )
                key.setAttribute( "type", type );
        }

        // many-to-one
        for ( int y = 0; y < props.length; y++ ) {
            prop      = props[ y ];
            meta      = prop.getDynamicAttributes();
            fieldType = toString( cfc, prop, meta, "fieldType", data );
            if ( CommonUtil.listFindNoCaseIgnoreEmpty( fieldType, CFConstants.Relationships.MANY_TO_ONE, ',' ) == -1 )
                continue;

            Element key = doc.createElement( "key-many-to-one" );
            cid.appendChild( key );

            // name
            key.setAttribute( "name", prop.getName() );

            // entity-name
            setForeignEntityName( cfc, prop, meta, key, false, data );

            // fkcolum
            String str = toString( cfc, prop, meta, "fkcolumn", data );
            setColumn( doc, key, str, data );

            // lazy
            setLazy( cfc, prop, meta, key, data );
        }
    }

    private static void createXMLMappingId( Component cfc, Element clazz, Property prop, Struct columnsInfo,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();
        String str;

        Document doc = XMLUtil.getDocument( clazz );
        Element id = doc.createElement( "id" );
        clazz.appendChild( id );

        // access
        str = toString( cfc, prop, meta, "access", data );
        if ( !Util.isEmpty( str, true ) )
            id.setAttribute( "access", str );

        // name
        id.setAttribute( "name", prop.getName() );

        // column
        Element column = doc.createElement( "column" );
        id.appendChild( column );

        str = toString( cfc, prop, meta, "column", data );
        if ( Util.isEmpty( str, true ) )
            str = prop.getName();
        column.setAttribute( "name", formatColumn( str, data ) );
        ColumnInfo info = getColumnInfo( columnsInfo, str, null );
        StringBuilder foreignCFC = new StringBuilder();
        String generator = createXMLMappingGenerator( id, cfc, prop, foreignCFC, data );

        str = toString( cfc, prop, meta, "length", data );
        if ( !Util.isEmpty( str, true ) )
            column.setAttribute( "length", str );

        // type
        String type = getType( info, cfc, prop, meta, getDefaultTypeForGenerator( generator, foreignCFC, data ), data );
        if ( !Util.isEmpty( type ) )
            id.setAttribute( "type", type );

        // unsaved-value
        str = toString( cfc, prop, meta, "unsavedValue", data );
        if ( str != null )
            id.setAttribute( "unsaved-value", str );

    }

    private static String getDefaultTypeForGenerator( String generator, StringBuilder foreignCFC, SessionFactoryData data ) {
        String value = getDefaultTypeForGenerator( generator, null );
        if ( value != null )
            return value;

        if ( "foreign".equalsIgnoreCase( generator ) ) {
            if ( !Util.isEmpty( foreignCFC.toString() ) ) {
                try {
                    Component cfc = data.getEntityByCFCName( foreignCFC.toString(), false );
                    if ( cfc != null ) {
                        Property[] ids = getIds( cfc, cfc.getProperties( true, false, false, false ), null, true, data );
                        if ( ids != null && ids.length > 0 ) {
                            Property id = ids[ 0 ];
                            id.getDynamicAttributes();
                            Struct meta = id.getDynamicAttributes();
                            if ( meta != null ) {
                                String type = CommonUtil.toString( meta.get( TYPE, null ) );

                                if ( !Util.isEmpty( type )
                                        && ( !type.equalsIgnoreCase( "any" ) && !type.equalsIgnoreCase( "object" ) ) ) {
                                    return type;
                                }

                                String g = CommonUtil.toString( meta.get( GENERATOR, null ) );
                                if ( !Util.isEmpty( g ) ) {
                                    return getDefaultTypeForGenerator( g, foreignCFC, data );
                                }

                            }
                        }
                    }
                } catch ( Throwable t ) {
                    if ( t instanceof ThreadDeath )
                        throw ( ThreadDeath ) t;
                }
            }
            return "string";
        }

        return "string";
    }

    public static String getDefaultTypeForGenerator( String generator, String defaultValue ) {
        if ( "increment".equalsIgnoreCase( generator ) )
            return "integer";
        if ( "identity".equalsIgnoreCase( generator ) )
            return "integer";
        if ( "native".equalsIgnoreCase( generator ) )
            return "integer";
        if ( "seqhilo".equalsIgnoreCase( generator ) )
            return "string";
        if ( "uuid".equalsIgnoreCase( generator ) )
            return "string";
        if ( "guid".equalsIgnoreCase( generator ) )
            return "string";
        if ( "select".equalsIgnoreCase( generator ) )
            return "string";
        return defaultValue;
    }

    private static String getType( ColumnInfo info, Component cfc, Property prop, Struct meta, String defaultValue,
            SessionFactoryData data ) throws PageException {
        // ormType
        String type = toString( cfc, prop, meta, "ormType", data );

        // dataType
        if ( Util.isEmpty( type, true ) ) {
            type = toString( cfc, prop, meta, "dataType", data );
        }

        // type
        if ( Util.isEmpty( type, true ) ) {
            type = prop.getType();
        }

        // type from db info
        if ( Util.isEmpty( type, true ) ) {
            if ( info != null ) {
                type = info.getTypeName();
            } else
                return defaultValue;
        }
        return HibernateCaster.toHibernateType( info, type, defaultValue );
    }

    private static ColumnInfo getColumnInfo( Struct columnsInfo, String columnName, ColumnInfo defaultValue ) {
        if ( columnsInfo != null ) {
            ColumnInfo info = ( ColumnInfo ) columnsInfo.get( CommonUtil.createKey( columnName ), null );
            if ( info == null )
                return defaultValue;
            return info;
        }
        return defaultValue;
    }

    private static String createXMLMappingGenerator( Element id, Component cfc, Property prop, StringBuilder foreignCFC,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();

        // generator
        String className = toString( cfc, prop, meta, "generator", data );
        if ( Util.isEmpty( className, true ) )
            return null;

        Document doc = XMLUtil.getDocument( id );
        Element generator = doc.createElement( "generator" );
        id.appendChild( generator );

        generator.setAttribute( "class", className );

        // params
        Object obj = meta.get( PARAMS, null );
        Struct sct = null;
        if ( obj == null )
            obj = CommonUtil.createStruct();
        else if ( obj instanceof String )
            obj = CommonUtil.convertToSimpleMap( ( String ) obj );

        if ( CommonUtil.isStruct( obj ) )
            sct = CommonUtil.toStruct( obj );
        else {
            throw ExceptionUtil.createException( data, cfc, "invalid value for attribute [params] of tag [property]", null );
        }
        className = className.trim().toLowerCase();

        // special classes
        if ( "foreign".equals( className ) ) {
            if ( !sct.containsKey( PROPERTY ) )
                sct.setEL( PROPERTY, toString( cfc, prop, meta, PROPERTY, true, data ) );

            if ( sct.containsKey( PROPERTY ) ) {
                String p = CommonUtil.toString( sct.get( PROPERTY ), null );
                if ( !Util.isEmpty( p ) )
                    foreignCFC.append( p );
            }

        } else if ( "select".equals( className ) ) {
            if ( !sct.containsKey( KEY ) )
                sct.setEL( KEY, toString( cfc, prop, meta, "selectKey", true, data ) );
        } else if ( "sequence".equals( className ) && !sct.containsKey( SEQUENCE ) ) {
            sct.setEL( SEQUENCE, toString( cfc, prop, meta, "sequence", true, data ) );
        }
        /**
         * @TODO: Add `sequence-indentity` support
         * ```
         *  else if ( "sequence-indentity".equals( className ) && !sct.containsKey( SEQUENCE_IDENTITY ) ) {
                sct.setEL( SEQUENCE_IDENTITY, toString( cfc, prop, meta, "sequence-indentity", true, data ) );
            }
         * ```
         */

        Iterator<Entry<Key, Object>> it = sct.entryIterator();
        Entry<Key, Object> e;
        Element param;
        while ( it.hasNext() ) {
            e     = it.next();
            param = doc.createElement( "param" );
            generator.appendChild( param );

            param.setAttribute( "name", e.getKey().getLowerString() );
            param.appendChild( doc.createTextNode( CommonUtil.toString( e.getValue() ) ) );

        }
        return className;
    }

    private static void createXMLMappingProperty( Element clazz, Component cfc, Property prop, Struct columnsInfo,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();

        // get table name
        String columnName = toString( cfc, prop, meta, "column", data );
        if ( Util.isEmpty( columnName, true ) )
            columnName = prop.getName();

        ColumnInfo info = getColumnInfo( columnsInfo, columnName, null );

        Document doc = XMLUtil.getDocument( clazz );
        final Element property = doc.createElement( "property" );
        clazz.appendChild( property );

        // name
        property.setAttribute( "name", prop.getName() );

        // type
        String str = getType( info, cfc, prop, meta, "string", data );
        property.setAttribute( "type", str );

        // formula or column
        str = toString( cfc, prop, meta, "formula", data );
        Boolean b;
        if ( !Util.isEmpty( str, true ) ) {
            property.setAttribute( "formula", "(" + str + ")" );
        } else {
            String length = null;

            Element column = doc.createElement( "column" );
            property.appendChild( column );
            column.setAttribute( "name", escape( HibernateUtil.convertColumnName( data, columnName ) ) );

            // check
            str = toString( cfc, prop, meta, "check", data );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "check", str );

            // default
            str = toString( cfc, prop, meta, "dbDefault", data );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "default", str );

            // index
            str = toString( cfc, prop, meta, "index", data );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "index", str );

            // length
            Integer i = toInteger( cfc, meta, "length", data );
            if ( i != null && i > 0 ) {
                length = CommonUtil.toString( i.intValue() );
                column.setAttribute( "length", length );
            }

            // not-null
            b = toBoolean( cfc, meta, "notnull", data );
            if ( b != null && b.booleanValue() )
                column.setAttribute( "not-null", "true" );

            // precision
            i = toInteger( cfc, meta, "precision", data );
            if ( i != null && i > -1 )
                column.setAttribute( "precision", CommonUtil.toString( i.intValue() ) );

            // scale
            i = toInteger( cfc, meta, "scale", data );
            if ( i != null && i > -1 )
                column.setAttribute( "scale", CommonUtil.toString( i.intValue() ) );

            // sql-type
            str = toString( cfc, prop, meta, "sqltype", data );
            if ( !Util.isEmpty( str, true ) ) {
                if ( ( str.equals( "varchar" ) || str.equals( "nvarchar" ) ) && length != null ) {
                    str += "(" + length + ")";
                }
                column.setAttribute( "sql-type", str );
            }

            // unique
            b = toBoolean( cfc, meta, "unique", data );
            if ( b != null && b.booleanValue() )
                column.setAttribute( "unique", "true" );

            // unique-key
            str = toString( cfc, prop, meta, "uniqueKey", data );
            if ( Util.isEmpty( str ) )
                str = CommonUtil.toString( meta.get( UNIQUE_KEY_NAME, null ), null );
            if ( !Util.isEmpty( str, true ) )
                column.setAttribute( "unique-key", str );

        }

        // generated
        str = toString( cfc, prop, meta, "generated", data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();

            if ( CFConstants.Generated.isValidValue(str) )
                property.setAttribute( "generated", str );
            else
                throw invalidValue( cfc, prop, CFConstants.Generated.ATTRIBUTE_NAME, str, CFConstants.Generated.getPossibleValues().toString(), data );
        }

        // update
        b = toBoolean( cfc, meta, "update", data );
        if ( b != null && !b.booleanValue() )
            property.setAttribute( "update", "false" );

        // insert
        b = toBoolean( cfc, meta, "insert", data );
        if ( b != null && !b.booleanValue() )
            property.setAttribute( "insert", "false" );

        // lazy (dtd defintion:<!ATTLIST property lazy (true|false) "false">)
        b = toBoolean( cfc, meta, "lazy", data );
        if ( b != null && b.booleanValue() )
            property.setAttribute( "lazy", "true" );

        // optimistic-lock
        b = toBoolean( cfc, meta, "optimisticlock", data );
        if ( b != null && !b.booleanValue() )
            property.setAttribute( "optimistic-lock", "false" );

    }

    /*
     * @TODO: dies kommt aber nicht hier sondern in verarbeitung in component <cfproperty persistent="true|false" >
     */
    private static void createXMLMappingOneToOne( Element clazz, Component cfc, Property prop,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();

        Boolean b;

        Document doc = XMLUtil.getDocument( clazz );
        Element x2o;

        // column
        String fkcolumn = toString( cfc, prop, meta, "fkcolumn", data );
        String linkTable = toString( cfc, prop, meta, "linkTable", data );

        if ( !Util.isEmpty( linkTable, true ) || !Util.isEmpty( fkcolumn, true ) ) {
            clazz = getJoin( clazz );

            x2o   = doc.createElement( CFConstants.Relationships.MANY_TO_ONE );
            x2o.setAttribute( "unique", "true" );

            if ( !Util.isEmpty( linkTable, true ) ) {
                setColumn( doc, x2o, linkTable, data );
            } else {
                setColumn( doc, x2o, fkcolumn, data );
            }

            // update
            b = toBoolean( cfc, meta, "update", data );
            if ( b != null )
                x2o.setAttribute( "update", CommonUtil.toString( b.booleanValue() ) );

            // insert
            b = toBoolean( cfc, meta, "insert", data );
            if ( b != null )
                x2o.setAttribute( "insert", CommonUtil.toString( b.booleanValue() ) );

            // not-null
            b = toBoolean( cfc, meta, "notNull", data );
            if ( b != null )
                x2o.setAttribute( "not-null", CommonUtil.toString( b.booleanValue() ) );

            // optimistic-lock
            b = toBoolean( cfc, meta, "optimisticLock", data );
            if ( b != null )
                x2o.setAttribute( "optimistic-lock", CommonUtil.toString( b.booleanValue() ) );

            // not-found
            b = toBoolean( cfc, meta, "missingRowIgnored", data );
            if ( b != null && b.booleanValue() )
                x2o.setAttribute( "not-found", "ignore" );

        } else {
            x2o = doc.createElement( CFConstants.Relationships.ONE_TO_ONE );
        }
        clazz.appendChild( x2o );

        // access
        String str = toString( cfc, prop, meta, "access", data );
        if ( !Util.isEmpty( str, true ) )
            x2o.setAttribute( "access", str );

        // constrained
        b = toBoolean( cfc, meta, "constrained", data );
        if ( b != null && b.booleanValue() )
            x2o.setAttribute( "constrained", "true" );

        // formula
        str = toString( cfc, prop, meta, "formula", data );
        if ( !Util.isEmpty( str, true ) )
            x2o.setAttribute( "formula", str );

        // embed-xml
        str = toString( cfc, prop, meta, "embedXml", data );
        if ( !Util.isEmpty( str, true ) )
            x2o.setAttribute( "embed-xml", str );

        // property-ref
        str = toString( cfc, prop, meta, "mappedBy", data );
        if ( !Util.isEmpty( str, true ) )
            x2o.setAttribute( "property-ref", str );

        // foreign-key
        str = toString( cfc, prop, meta, "foreignKeyName", data );
        if ( Util.isEmpty( str, true ) )
            str = toString( cfc, prop, meta, "foreignKey", data );
        if ( !Util.isEmpty( str, true ) )
            x2o.setAttribute( "foreign-key", str );

        setForeignEntityName( cfc, prop, meta, x2o, true, data );

        createXMLMappingXToX( x2o, cfc, prop, meta, data );
    }

    private static Component loadForeignCFC( Component cfc, Property prop, Struct meta, SessionFactoryData data )
            throws PageException {
        // entity
        String str = toString( cfc, prop, meta, "entityName", data );
        Component fcfc = null;

        if ( !Util.isEmpty( str, true ) ) {
            fcfc = data.getEntityByEntityName( str, false );
            if ( fcfc != null )
                return fcfc;
        }

        str = toString( cfc, prop, meta, "cfc", false, data );
        if ( !Util.isEmpty( str, true ) ) {
            return data.getEntityByCFCName( str, false );
        }
        return null;
    }

    private static void createXMLMappingCollection( Element clazz, Component cfc, Property prop,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();
        Document doc = XMLUtil.getDocument( clazz );
        Element el = null;

        // collection type
        String str = prop.getType();
        if ( Util.isEmpty( str, true ) || "any".equalsIgnoreCase( str ) || "object".equalsIgnoreCase( str ) )
            str = "array";
        else
            str = str.trim().toLowerCase();

        // bag
        if ( "array".equals( str ) || "bag".equals( str ) ) {
            el = doc.createElement( "bag" );
        }
        // map
        else if ( "struct".equals( str ) || "map".equals( str ) ) {
            el  = doc.createElement( "map" );

            // map-key
            str = toString( cfc, prop, meta, "structKeyColumn", true, data );
            if ( !Util.isEmpty( str, true ) ) {
                Element mapKey = doc.createElement( "map-key" );
                el.appendChild( mapKey );
                mapKey.setAttribute( "column", str );

                // type
                str = toString( cfc, prop, meta, "structKeyType", data );
                if ( !Util.isEmpty( str, true ) )
                    mapKey.setAttribute( "type", str );
                else
                    mapKey.setAttribute( "type", "string" );
            }
        } else
            throw invalidValue( cfc, prop, "collectiontype", str, "array,struct", data );

        setBeforeJoin( clazz, el );

        // name
        el.setAttribute( "name", prop.getName() );

        // table
        str = toString( cfc, prop, meta, "table", true, data );
        el.setAttribute( "table", escape( HibernateUtil.convertTableName( data, str ) ) );

        // catalog
        str = toString( cfc, prop, meta, "catalog", data );
        if ( !Util.isEmpty( str, true ) )
            el.setAttribute( "catalog", str );

        // schema
        str = toString( cfc, prop, meta, "schema", data );
        if ( !Util.isEmpty( str, true ) )
            el.setAttribute( "schema", str );

        // mutable
        Boolean b = toBoolean( cfc, meta, "readonly", data );
        if ( b != null && b.booleanValue() )
            el.setAttribute( "mutable", "false" );

        // order-by
        str = toString( cfc, prop, meta, "orderby", data );
        if ( !Util.isEmpty( str, true ) )
            el.setAttribute( "order-by", str );

        // element-column
        str = toString( cfc, prop, meta, "elementcolumn", data );
        if ( !Util.isEmpty( str, true ) ) {
            Element element = doc.createElement( "element" );
            el.appendChild( element );

            // column
            element.setAttribute( "column", formatColumn( str, data ) );

            // type
            str = toString( cfc, prop, meta, "elementtype", data );
            if ( !Util.isEmpty( str, true ) )
                element.setAttribute( "type", str );
        }

        // batch-size
        Integer i = toInteger( cfc, meta, "batchsize", data );
        if ( i != null && i.intValue() > 1 )
            el.setAttribute( "batch-size", CommonUtil.toString( i.intValue() ) );

        // column
        str = toString( cfc, prop, meta, "fkcolumn", data );
        if ( Util.isEmpty( str, true ) )
            str = toString( cfc, prop, meta, "column", data );
        if ( !Util.isEmpty( str, true ) ) {
            Element key = doc.createElement( "key" );
            CommonUtil.setFirst( el, key );

            // column
            key.setAttribute( "column", formatColumn( str, data ) );

            // property-ref
            str = toString( cfc, prop, meta, "mappedBy", data );
            if ( !Util.isEmpty( str, true ) )
                key.setAttribute( "property-ref", str );
        }

        // cache
        setCacheStrategy( cfc, prop, doc, meta, el, data );

        // optimistic-lock
        b = toBoolean( cfc, meta, "optimisticlock", data );
        if ( b != null && !b.booleanValue() )
            el.setAttribute( "optimistic-lock", "false" );

    }

    private static void setBeforeJoin( Element clazz, Element el ) {
        Element join;
        if ( clazz.getNodeName().equals( "join" ) ) {
            join  = clazz;
            clazz = getClazz( clazz );
        } else {
            join = getJoin( clazz );
        }

        if ( join == clazz )
            clazz.appendChild( el );
        else
            clazz.insertBefore( el, join );

    }

    private static Element getClazz( Element join ) {
        if ( join.getNodeName().equals( "join" ) ) {
            return ( Element ) join.getParentNode();
        }
        return join;
    }

    private static Element getJoin( Element clazz ) {
        if ( clazz.getNodeName().equals( "subclass" ) ) {
            NodeList joins = clazz.getElementsByTagName( "join" );
            if ( joins != null && joins.getLength() > 0 )
                return ( Element ) joins.item( 0 );
        }
        return clazz;
    }

    private static void createXMLMappingManyToMany( DatasourceConnection dc, Component cfc, PropertyCollection propColl,
            Element clazz, PageContext pc, Property prop, SessionFactoryData data ) throws PageException {
        Element el = createXMLMappingXToMany( propColl, clazz, cfc, prop, data );
        Struct meta = prop.getDynamicAttributes();
        Document doc = XMLUtil.getDocument( clazz );
        Element m2m = doc.createElement( CFConstants.Relationships.MANY_TO_MANY );
        el.appendChild( m2m );

        // link
        setLink( dc, cfc, prop, el, meta, true, data );

        setForeignEntityName( cfc, prop, meta, m2m, true, data );

        // order-by
        String str = toString( cfc, prop, meta, "orderby", data );
        if ( !Util.isEmpty( str, true ) )
            m2m.setAttribute( "order-by", str );

        // column
        str = toString( cfc, prop, meta, "inversejoincolumn", data );

        // build fkcolumn name
        if ( Util.isEmpty( str, true ) ) {
            Component other = loadForeignCFC( cfc, prop, meta, data );
            if ( other != null ) {
                boolean isClass = Util.isEmpty( other.getExtends() );
                // MZ: Recursive search for persistent mappedSuperclass properties
                Property[] _props = getProperties( pc, other, dc, meta, isClass, true, data );
                PropertyCollection _propColl = splitJoins( cfc, new HashMap<>(), _props, data );
                _props = _propColl.getProperties();

                Struct m;
                Property _prop = null;
                for ( int i = 0; i < _props.length; i++ ) {
                    m = _props[ i ].getDynamicAttributes();
                    // fieldtype
                    String fieldtype = CommonUtil.toString( m.get( FIELDTYPE, null ), null );
                    if ( CFConstants.Relationships.MANY_TO_MANY.equalsIgnoreCase( fieldtype ) ) {
                        // linktable
                        String currLinkTable = CommonUtil.toString( meta.get( LINK_TABLE, null ), null );
                        String othLinkTable = CommonUtil.toString( m.get( LINK_TABLE, null ), null );
                        if ( currLinkTable.equals( othLinkTable ) ) {
                            // cfc name
                            String cfcName = CommonUtil.toString( m.get( CFC, null ), null );
                            if ( cfc.equalTo( cfcName ) ) {
                                _prop = _props[ i ];
                            }
                        }
                    }
                }
                str = createM2MFKColumnName( other, _prop, _propColl, data );
            }
        }
        setColumn( doc, m2m, str, data );

        // not-found
        Boolean b = toBoolean( cfc, meta, "missingrowignored", data );
        if ( b != null && b.booleanValue() )
            m2m.setAttribute( "not-found", "ignore" );

        // property-ref
        str = toString( cfc, prop, meta, "mappedby", data );
        if ( !Util.isEmpty( str, true ) )
            m2m.setAttribute( "property-ref", str );

        // foreign-key
        str = toString( cfc, prop, meta, "foreignKeyName", data );
        if ( Util.isEmpty( str, true ) )
            str = toString( cfc, prop, meta, "foreignKey", data );
        if ( !Util.isEmpty( str, true ) )
            m2m.setAttribute( "foreign-key", str );
    }

    private static boolean setLink( DatasourceConnection dc, Component cfc, Property prop, Element el, Struct meta,
            boolean linkTableRequired, SessionFactoryData data ) throws PageException {
        String str = toString( cfc, prop, meta, "linktable", linkTableRequired, data );

        if ( !Util.isEmpty( str, true ) ) {

            el.setAttribute( "table", escape( HibernateUtil.convertTableName( data, str ) ) );

            // schema
            str = toString( cfc, prop, meta, "linkschema", data );
            if ( Util.isEmpty( str, true ) )
                str = ORMConfigurationUtil.getSchema( data.getORMConfiguration(), dc.getDatasource().getName() );
            if ( !Util.isEmpty( str, true ) )
                el.setAttribute( "schema", str );

            // catalog
            str = toString( cfc, prop, meta, "linkcatalog", data );
            if ( Util.isEmpty( str, true ) )
                str = ORMConfigurationUtil.getCatalog( data.getORMConfiguration(), dc.getDatasource().getName() );
            if ( !Util.isEmpty( str, true ) )
                el.setAttribute( "catalog", str );
            return true;
        }
        return false;
    }

    private static void createXMLMappingOneToMany( DatasourceConnection dc, Component cfc, PropertyCollection propColl,
            Element clazz, Property prop, SessionFactoryData data ) throws PageException {
        Element el = createXMLMappingXToMany( propColl, clazz, cfc, prop, data );
        Struct meta = prop.getDynamicAttributes();
        Document doc = XMLUtil.getDocument( clazz );
        Element x2m;

        // order-by
        String str = toString( cfc, prop, meta, "orderby", data );
        if ( !Util.isEmpty( str, true ) )
            el.setAttribute( "order-by", str );

        // link
        if ( setLink( dc, cfc, prop, el, meta, false, data ) ) {
            x2m = doc.createElement( CFConstants.Relationships.MANY_TO_MANY );
            x2m.setAttribute( "unique", "true" );

            str = toString( cfc, prop, meta, "inversejoincolumn", data );
            setColumn( doc, x2m, str, data );
        } else {
            x2m = doc.createElement( CFConstants.Relationships.ONE_TO_MANY );
        }
        el.appendChild( x2m );

        // entity-name

        setForeignEntityName( cfc, prop, meta, x2m, true, data );

    }

    private static Element createXMLMappingXToMany( PropertyCollection propColl, Element clazz, Component cfc,
            Property prop, SessionFactoryData data ) throws PageException {
        final Struct meta = prop.getDynamicAttributes();
        Document doc = XMLUtil.getDocument( clazz );
        Element el = null;

        // collection type
        String str = prop.getType();
        if ( Util.isEmpty( str, true ) || "any".equalsIgnoreCase( str ) || "object".equalsIgnoreCase( str ) )
            str = "array";
        else
            str = str.trim().toLowerCase();

        Element mapKey = null;
        // bag
        if ( CFConstants.CollectionType.isBagType(str) ) {
            el = doc.createElement( "bag" );

        }
        // map
        else if ( CFConstants.CollectionType.isMapType(str) ) {
            el     = doc.createElement( "map" );

            // map-key
            mapKey = doc.createElement( "map-key" );

            // column
            str    = toString( cfc, prop, meta, "structKeyColumn", true, data );
            mapKey.setAttribute( "column", formatColumn( str, data ) );

            // type
            str = toString( cfc, prop, meta, "structKeyType", data );
            if ( !Util.isEmpty( str, true ) )
                mapKey.setAttribute( "type", str );
            else
                mapKey.setAttribute( "type", "string" );// @TODO: get type dynamicly
        } else
            throw invalidValue( cfc, prop, CFConstants.CollectionType.ATTRIBUTE_NAME, str, CFConstants.CollectionType.getPossibleValues().toString(), data );

        setBeforeJoin( clazz, el );

        // batch-size
        Integer i = toInteger( cfc, meta, "batchsize", data );
        if ( i != null && i.intValue() > 1 ) {
            el.setAttribute( "batch-size", CommonUtil.toString( i.intValue() ) );
        }

        // cacheUse
        setCacheStrategy( cfc, prop, doc, meta, el, data );

        // column
        str = createFKColumnName( cfc, prop, propColl, data );

        if ( !Util.isEmpty( str, true ) ) {
            Element key = doc.createElement( "key" );
            el.appendChild( key );

            // column
            setColumn( doc, key, str, data );

            // property-ref
            str = toString( cfc, prop, meta, "mappedBy", data );
            if ( !Util.isEmpty( str, true ) )
                key.setAttribute( "property-ref", str );
        }

        // inverse
        Boolean b = toBoolean( cfc, meta, "inverse", data );
        if ( b != null && b.booleanValue() )
            el.setAttribute( "inverse", "true" );

        // mutable
        b = toBoolean( cfc, meta, "readonly", data );
        if ( b != null && b.booleanValue() )
            el.setAttribute( "mutable", "false" );

        // optimistic-lock
        b = toBoolean( cfc, meta, "optimisticlock", data );
        if ( b != null && !b.booleanValue() )
            el.setAttribute( "optimistic-lock", "false" );

        // where
        str = toString( cfc, prop, meta, "where", data );
        if ( !Util.isEmpty( str, true ) )
            el.setAttribute( "where", str );

        // add map key
        if ( mapKey != null )
            el.appendChild( mapKey );

        createXMLMappingXToX( el, cfc, prop, meta, data );

        return el;
    }

    private static String createFKColumnName( Component cfc, Property prop, PropertyCollection propColl, SessionFactoryData data )
            throws PageException {

        // fk column from local defintion
        String str = prop == null ? null : toString( cfc, prop, prop.getDynamicAttributes(), "fkcolumn", data );
        if ( !Util.isEmpty( str ) )
            return str;

        // no local defintion, get from Foreign enity
        Struct meta = prop.getDynamicAttributes();
        String type = toString( cfc, prop, meta, "fieldtype", false, data );
        String otherType;
        if ( CFConstants.Relationships.MANY_TO_ONE.equalsIgnoreCase( type ) )
            otherType = CFConstants.Relationships.ONE_TO_MANY;
        else if ( CFConstants.Relationships.ONE_TO_MANY.equalsIgnoreCase( type ) )
            otherType = CFConstants.Relationships.MANY_TO_ONE;
        else
            return createM2MFKColumnName( cfc, prop, propColl, data );

        String feName = toString( cfc, prop, meta, "cfc", true, data );
        Component feCFC = data.getEntityByCFCName( feName, false );
        Property[] feProps = feCFC.getProperties( true, false, false, false );

        Property p;
        Component _cfc;
        for ( int i = 0; i < feProps.length; i++ ) {
            p   = feProps[ i ];

            // compare fieldType
            str = toString( feCFC, p, p.getDynamicAttributes(), "fieldtype", false, data );
            if ( !otherType.equalsIgnoreCase( str ) )
                continue;

            // compare cfc
            str = toString( feCFC, p, p.getDynamicAttributes(), "cfc", false, data );
            if ( Util.isEmpty( str ) )
                continue;
            _cfc = data.getEntityByCFCName( str, false );
            if ( _cfc == null || !_cfc.equals( cfc ) )
                continue;

            // get fkcolumn
            str = toString( _cfc, p, p.getDynamicAttributes(), "fkcolumn", data );
            if ( !Util.isEmpty( str ) )
                return str;

        }
        String message = String.format(
                "Persistent property [%s] on component [%s] is missing `fkcolumn` definition for relationship identification",
                prop.getName(), cfc.getName() );
        throw ExceptionUtil.createException( data, null, message, null );
    }

    private static String createM2MFKColumnName( Component cfc, Property prop, PropertyCollection propColl,
            SessionFactoryData data ) throws PageException {

        String str = prop == null ? null : toString( cfc, prop, prop.getDynamicAttributes(), "fkcolumn", data );
        if ( Util.isEmpty( str ) ) {
            Property[] ids = getIds( cfc, propColl, data );
            if ( ids.length == 1 ) {
                str = toString( cfc, ids[ 0 ], ids[ 0 ].getDynamicAttributes(), "column", data );
                if ( Util.isEmpty( str, true ) )
                    str = ids[ 0 ].getName();
            } else if ( prop != null )
                str = toString( cfc, prop, prop.getDynamicAttributes(), "fkcolumn", true, data );
            else {
                String message = String.format(
                        "Persistent property [%s] on component [%s] is missing `fkcolumn` definition for relationship identification",
                        prop.getName(), cfc.getName() );
                throw ExceptionUtil.createException( data, null, message, null );
            }

            str = HibernateCaster.getEntityName( cfc ) + "_" + str;
        }
        return str;
    }

    private static void setForeignEntityName( Component cfc, Property prop, Struct meta, Element el, boolean cfcRequired,
            SessionFactoryData data ) throws PageException {
        // entity
        String str = cfcRequired ? null : toString( cfc, prop, meta, "entityName", data );
        if ( !Util.isEmpty( str, true ) ) {
            el.setAttribute( "entity-name", str );
        } else {
            str = toString( cfc, prop, meta, "cfc", cfcRequired, data );
            if ( !Util.isEmpty( str, true ) ) {
                Component _cfc = data.getEntityByCFCName( str, false );
                str = HibernateCaster.getEntityName( _cfc );
                el.setAttribute( "entity-name", str );
            }
        }
    }

    private static void setCacheStrategy( Component cfc, Property prop, Document doc, Struct meta, Element el,
            SessionFactoryData data ) throws PageException {
        String strategy = toString( cfc, prop, meta, "cacheuse", data );

        if ( !Util.isEmpty( strategy, true ) ) {
            strategy = strategy.trim().toLowerCase();
            if ( CFConstants.CacheUse.isValidValue(strategy) ) {
                Element cache = doc.createElement( "cache" );
                CommonUtil.setFirst( el, cache );
                el.appendChild( cache );
                cache.setAttribute( CFConstants.CacheUse.HBM_KEY, strategy );
                String name = toString( cfc, prop, meta, "cacheName", data );
                if ( !Util.isEmpty( name, true ) ) {
                    cache.setAttribute( "region", name );
                }
            } else {
                String message = String.format(
                        "invalid value [%s] for attribute [%s], valid values are [%s]",
                        strategy,
                        CFConstants.CacheUse.ATTRIBUTE_NAME,
                        CFConstants.CacheUse.getPossibleValues().toString()
                    );
                throw ExceptionUtil.createException( data, cfc, message, null );
            }
        }

    }

    private static void setColumn( Document doc, Element el, String columnValue, SessionFactoryData data ) throws PageException {
        if ( Util.isEmpty( columnValue, true ) )
            return;

        String[] arr = CommonUtil.toStringArray( columnValue, "," );
        if ( arr.length == 1 ) {
            el.setAttribute( "column", formatColumn( arr[ 0 ], data ) );
        } else {
            Element column;
            for ( int i = 0; i < arr.length; i++ ) {
                column = doc.createElement( "column" );
                el.appendChild( column );
                column.setAttribute( "name", formatColumn( arr[ i ], data ) );
            }
        }
    }

    private static void createXMLMappingManyToOne( Element clazz, Component cfc, Property prop,
            PropertyCollection propColl, SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();
        Boolean b;

        Document doc = XMLUtil.getDocument( clazz );
        clazz = getJoin( clazz );

        Element m2o = doc.createElement( CFConstants.Relationships.MANY_TO_ONE );
        clazz.appendChild( m2o );

        // columns
        String linktable = toString( cfc, prop, meta, "linktable", data );
        String _columns;
        if ( !Util.isEmpty( linktable, true ) )
            _columns = toString( cfc, prop, meta, "inversejoincolumn", data );
        else
            _columns = createFKColumnName( cfc, prop, propColl, data );
        setColumn( doc, m2o, _columns, data );

        // cfc
        setForeignEntityName( cfc, prop, meta, m2o, true, data );

        // column

        // insert
        b = toBoolean( cfc, meta, "insert", data );
        if ( b != null && !b.booleanValue() )
            m2o.setAttribute( "insert", "false" );

        // update
        b = toBoolean( cfc, meta, "update", data );
        if ( b != null && !b.booleanValue() )
            m2o.setAttribute( "update", "false" );

        // property-ref
        String str = toString( cfc, prop, meta, "mappedBy", data );
        if ( !Util.isEmpty( str, true ) )
            m2o.setAttribute( "property-ref", str );

        // update
        b = toBoolean( cfc, meta, "unique", data );
        if ( b != null && b.booleanValue() )
            m2o.setAttribute( "unique", "true" );

        // not-null
        b = toBoolean( cfc, meta, "notnull", data );
        if ( b != null && b.booleanValue() )
            m2o.setAttribute( "not-null", "true" );

        // optimistic-lock
        b = toBoolean( cfc, meta, "optimisticLock", data );
        if ( b != null && !b.booleanValue() )
            m2o.setAttribute( "optimistic-lock", "false" );

        // not-found
        b = toBoolean( cfc, meta, "missingRowIgnored", data );
        if ( b != null && b.booleanValue() )
            m2o.setAttribute( "not-found", "ignore" );

        // index
        str = toString( cfc, prop, meta, "index", data );
        if ( !Util.isEmpty( str, true ) )
            m2o.setAttribute( "index", str );

        // unique-key
        str = toString( cfc, prop, meta, "uniqueKeyName", data );
        if ( Util.isEmpty( str, true ) )
            str = toString( cfc, prop, meta, "uniqueKey", data );
        if ( !Util.isEmpty( str, true ) )
            m2o.setAttribute( "unique-key", str );

        // foreign-key
        str = toString( cfc, prop, meta, "foreignKeyName", data );
        if ( Util.isEmpty( str, true ) )
            str = toString( cfc, prop, meta, "foreignKey", data );
        if ( !Util.isEmpty( str, true ) )
            m2o.setAttribute( "foreign-key", str );

        // access
        str = toString( cfc, prop, meta, "access", data );
        if ( !Util.isEmpty( str, true ) )
            m2o.setAttribute( "access", str );

        createXMLMappingXToX( m2o, cfc, prop, meta, data );

    }

    private static String formatColumn( String name, SessionFactoryData data ) throws PageException {
        name = name.trim();
        return escape( HibernateUtil.convertColumnName( data, name ) );
    }

    /*
     *
     *
     * <cfproperty cfc="Referenced_CFC_Name" linktable="Link table name" linkcatalog="Catalog for the link table"
     * linkschema="Schema for the link table" fkcolumn="Foreign Key column name"
     * inversejoincolumn="Column name or comma-separated list of primary key columns"
     *
     *
     * >
     *
     */
    private static void createXMLMappingXToX( Element x2x, Component cfc, Property prop, Struct meta,
            SessionFactoryData data ) throws PageException {
        x2x.setAttribute( "name", prop.getName() );

        // cascade
        String str = toString( cfc, prop, meta, "cascade", data );
        if ( !Util.isEmpty( str, true ) )
            x2x.setAttribute( "cascade", str );

        // fetch
        str = toString( cfc, prop, meta, CFConstants.Fetch.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            if ( CFConstants.Fetch.isValidValue(str) )
                x2x.setAttribute( CFConstants.Fetch.HBM_KEY, str );
            else
                throw invalidValue( cfc, prop, CFConstants.Fetch.ATTRIBUTE_NAME, str, CFConstants.Fetch.getPossibleValues().toString(), data );
        }

        // lazy
        setLazy( cfc, prop, meta, x2x, data );

    }

    private static void setLazy( Component cfc, Property prop, Struct meta, Element x2x, SessionFactoryData data )
            throws PageException {
        String str = toString( cfc, prop, meta, CFConstants.Lazy.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            String name = x2x.getNodeName();

            if ( CFConstants.Relationships.isRelationshipType( name ) ) {
                if ( CFConstants.Lazy.isValidForRelationship(str)){
                    if ( CFConstants.Lazy.TRUE.equals(str) ){ str = CFConstants.Lazy.PROXY; }
                    x2x.setAttribute( CFConstants.Lazy.HBM_KEY, str );
                } else
                    throw invalidValue( cfc, prop, CFConstants.Lazy.ATTRIBUTE_NAME, str, CFConstants.Lazy.getPossibleForRelationship().toString(), data );
            }

            else {
                if ( CFConstants.Lazy.isValidForSimple(str) )
                    x2x.setAttribute( CFConstants.Lazy.HBM_KEY, str );
                else
                    throw invalidValue( cfc, prop, CFConstants.Lazy.ATTRIBUTE_NAME, str, CFConstants.Lazy.getPossibleForSimple().toString(), data );
            }
        }
    }

    private static void createXMLMappingTimestamp( Element clazz, Component cfc, Property prop,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();
        String str;
        Boolean b;

        Document doc = XMLUtil.getDocument( clazz );
        Element timestamp = doc.createElement( "timestamp" );
        clazz.appendChild( timestamp );

        timestamp.setAttribute( "name", prop.getName() );

        // access
        str = toString( cfc, prop, meta, "access", data );
        if ( !Util.isEmpty( str, true ) )
            timestamp.setAttribute( "access", str );

        // column
        str = toString( cfc, prop, meta, "column", data );
        if ( Util.isEmpty( str, true ) )
            str = prop.getName();
        timestamp.setAttribute( "column", formatColumn( str, data ) );

        // generated
        b = toBoolean( cfc, meta, "generated", data );
        if ( b != null )
            timestamp.setAttribute( "generated", b.booleanValue() ? "always" : "never" );

        // source
        str = toString( cfc, prop, meta, CFConstants.Source.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            if ( CFConstants.Source.isValidValue(str) )
                timestamp.setAttribute( CFConstants.Source.HBM_KEY, str );
            else
                throw invalidValue( cfc, prop, CFConstants.Source.ATTRIBUTE_NAME, str, CFConstants.Source.getPossibleValues().toString(), data );
        }

        // unsavedValue
        str = toString( cfc, prop, meta, CFConstants.UnsavedValue.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            if ( CFConstants.UnsavedValue.isValidValue(str) )
                timestamp.setAttribute( CFConstants.UnsavedValue.HBM_KEY, str );
            else
                throw invalidValue( cfc, prop, CFConstants.UnsavedValue.ATTRIBUTE_NAME, str, CFConstants.UnsavedValue.getPossibleValues().toString(), data );
        }
    }

    private static PageException invalidValue( Component cfc, Property prop, String attrName, String invalid, String valid,
            SessionFactoryData data ) {
        String owner = prop.getOwnerName();
        String message = String.format(
                "invalid value [%s] for attribute [%s] of property [%s] of Component [%s], valid values are [%s]", invalid,
                attrName, prop.getName(), CommonUtil.last( owner, "." ), valid );
        if ( Util.isEmpty( owner ) ) {
            message = String.format( "invalid value [%s] for attribute [%s] of property [%s], valid values are [%s]", invalid,
                    attrName, prop.getName(), valid );
            return ExceptionUtil.createException( data, cfc, message, null );
        }
        return ExceptionUtil.createException( data, cfc, message, null );
    }

    private static void createXMLMappingVersion( Element clazz, Component cfc, Property prop,
            SessionFactoryData data ) throws PageException {
        Struct meta = prop.getDynamicAttributes();

        Document doc = XMLUtil.getDocument( clazz );
        Element version = doc.createElement( "version" );
        clazz.appendChild( version );

        version.setAttribute( "name", prop.getName() );

        // column
        String str = toString( cfc, prop, meta, "column", data );
        if ( Util.isEmpty( str, true ) )
            str = prop.getName();
        version.setAttribute( "column", formatColumn( str, data ) );

        // access
        str = toString( cfc, prop, meta, "access", data );
        if ( !Util.isEmpty( str, true ) )
            version.setAttribute( "access", str );

        // generated
        Object o = meta.get( GENERATED, null );
        if ( o != null ) {
            Boolean b = CommonUtil.toBoolean( o, null );
            if ( b != null ) {
                str = b.booleanValue() ? "always" : "never";
            } else {
                str = CommonUtil.toString( o, null );
                if ( "always".equalsIgnoreCase( str ) )
                    str = "always";
                else if ( "never".equalsIgnoreCase( str ) )
                    str = "never";
                else
                    throw invalidValue( cfc, prop, "generated", o.toString(), "true,false,always,never", data );
            }
            version.setAttribute( CFConstants.Generated.HBM_KEY, str );
        }

        // insert
        Boolean b = toBoolean( cfc, meta, "insert", data );
        if ( b != null && !b.booleanValue() )
            version.setAttribute( "insert", "false" );

        // type
        String typeName = CFConstants.VersionDataType.HBM_KEY;
        str = toString( cfc, prop, meta, typeName, data );
        if ( Util.isEmpty( str, true ) ) {
            typeName = "ormType";
            str      = toString( cfc, prop, meta, typeName, data );
        }
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            if ( CFConstants.VersionDataType.isValidValue(str) )
                version.setAttribute( CFConstants.VersionDataType.HBM_KEY, "integer" );
            else
                throw invalidValue( cfc, prop, typeName, str, CFConstants.VersionDataType.getPossibleValues().toString(), data );
        } else
            version.setAttribute( CFConstants.VersionDataType.HBM_KEY, CFConstants.VersionDataType.INTEGER );

        // unsavedValue
        str = toString( cfc, prop, meta, CFConstants.VersionUnsavedValue.ATTRIBUTE_NAME, data );
        if ( !Util.isEmpty( str, true ) ) {
            str = str.trim().toLowerCase();
            if ( CFConstants.VersionUnsavedValue.isValidValue(str) )
                version.setAttribute( CFConstants.VersionUnsavedValue.HBM_KEY, str );
            else
                throw invalidValue( cfc, prop, CFConstants.VersionUnsavedValue.ATTRIBUTE_NAME, str, CFConstants.VersionUnsavedValue.getPossibleValues().toString(), data );
        }
    }

    private static String toString( Component cfc, Property prop, Struct sct, String key, SessionFactoryData data )
            throws PageException {
        return toString( cfc, prop, sct, key, false, data );
    }

    private static String toString( Component cfc, Property prop, Struct sct, String key, boolean throwErrorWhenNotExist,
            SessionFactoryData data ) throws PageException {
        return toString( cfc, prop, sct, CommonUtil.createKey( key ), throwErrorWhenNotExist, data );
    }

    private static String toString( Component cfc, Property prop, Struct sct, Collection.Key key, boolean throwErrorWhenNotExist,
            SessionFactoryData data ) throws PageException {
        Object value = sct.get( key, null );
        if ( value == null ) {
            if ( throwErrorWhenNotExist ) {
                if ( prop == null ) {
                    String message = String.format( "attribute [%s] is required", key );
                    throw ExceptionUtil.createException( data, cfc, message, null );
                } else {
                    String message = String.format( "attribute [%s] of property [%s] of Component [%s] is required", key,
                            prop.getName(), _getCFCName( prop ) );
                    throw ExceptionUtil.createException( data, cfc, message, null );
                }
            }
            return null;
        }

        String str = CommonUtil.toString( value, null );
        if ( str == null ) {
            if ( prop == null ) {
                String message = String.format( "invalid type [%s] for attribute [%s], value must be a string",
                        CommonUtil.toTypeName( value ), key );
                throw ExceptionUtil.createException( data, cfc, message, null );
            } else {
                String message = String.format(
                        "invalid type [%s] for attribute [%s] of property [%s] of Component [%s], value must be a string",
                        CommonUtil.toTypeName( value ), key, prop.getName(), _getCFCName( prop ) );
                throw ExceptionUtil.createException( data, cfc, message, null );
            }
        }
        return str;
    }

    private static String _getCFCName( Property prop ) {
        String owner = prop.getOwnerName();
        return CommonUtil.last( owner, "." );
    }

    private static Boolean toBoolean( Component cfc, Struct sct, String key, SessionFactoryData data ) throws PageException {
        Object value = sct.get( CommonUtil.createKey( key ), null );
        if ( value == null )
            return null;

        Boolean b = CommonUtil.toBoolean( value, null );
        if ( b == null ) {
            String message = String.format( "invalid type [%s] for attribute [%s], value must be a boolean",
                    CommonUtil.toTypeName( value ), key );
            throw ExceptionUtil.createException( data, cfc, message, null );
        }
        return b;
    }

    private static Integer toInteger( Component cfc, Struct sct, String key, SessionFactoryData data ) throws PageException {
        Object value = sct.get( CommonUtil.createKey( key ), null );
        if ( value == null )
            return null;

        Integer i = CommonUtil.toInteger( value, null );
        if ( i == null ) {
            String message = String.format( "invalid type [%s] for attribute [%s], value must be a numeric value",
                    CommonUtil.toTypeName( value ), key );
            throw ExceptionUtil.createException( data, cfc, message, null );
        }
        return i;
    }

    /**
     * Convert the document to a file-ready XML string.
     * <p>
     * Will prepend the XML head tags.
     *
     * @param document
     *                 The W3C document root element for converting to an XML string
     *
     * @return a fully-formed HBM XML document ready to save to a file.
     *
     * @throws PageException
     */
    public static String toMappingString( Element document ) throws PageException {
        return getXMLOpen() + XMLUtil.toString( document );
    }

    /**
     * Save the XML dom to a hibernate mapping file (myEntity.hbm.xml)
     *
     * @param cfc
     *            Lucee Component (entity) that we're saving the mapping for
     * @param xml
     *            Fully-formed hibernate mapping XML
     */
    public static void saveMapping( Component cfc, String xml ) {
        Resource res = getMappingResource( cfc );
        if ( res != null ) {
            try {
                CommonUtil.write( res, xml, CommonUtil.getUTF8Charset(), false );
            } catch ( Exception e ) {
                // TODO: For 7.0, throw "Unable to save XML mapping to disk"
            }
        }
    }

    /**
     * Get the hibernate mapping XML as a string.
     *
     * @param cfc
     *            Lucee Component (entity) for which to load the HBM mapping xml file
     *
     * @return A giant XML string
     */
    public static String loadMapping( Component cfc ) throws PageException, IOException {

        Resource resource = getMappingResource( cfc );
        if ( resource == null ) {
            String message = String.format( "Hibernate mapping not found for entity [%s]", cfc.getName() );
            throw ExceptionUtil.createException( message );
        }

        return CommonUtil.toString( resource, CommonUtil.getUTF8Charset() );
    }

    /**
     * Get the last modified time for this component's mapping. Will return 0 if no mapping found.
     *
     * @param cfc
     *            The Lucee component (persistent entity) we're pulling the modification date for
     *
     * @return A <code>long</code> value representing the time the file was last modified, measured in milliseconds
     *         since the epoch (00:00:00 GMT, January 1, 1970), or <code>0L</code> if the file does not exist or if an
     *         I/O error occurs
     *
     * @see {lucee.commons.io.res.Resource#lastModified}
     */
    public static long getMappingLastModified( Component cfc ) {
        Resource res = getMappingResource( cfc );
        if ( res == null )
            return 0;
        return res.lastModified();
    }

    /**
     * Get the HBM mapping file ( i.e. `models/myEntity.hbm.xml`) for this persistent Component/entity.
     *
     * @param cfc
     *            Lucee Component
     *
     * @return Lucee Resource, i.e. a Lucee-fied File object
     */
    public static Resource getMappingResource( Component cfc ) {
        Resource res = cfc.getPageSource().getResource();
        if ( res == null )
            return null;
        return res.getParentResource().getRealResource( res.getName() + ".hbm.xml" );
    }

    /**
     * Get the opening of a Hibernate mapping XML file, including xml tag and DOCTYPE declaration
     */
    public static String getXMLOpen() {
        StringBuilder xml = new StringBuilder();
        xml.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
        xml.append( HIBERNATE_3_DOCTYPE_DEFINITION + "\n" );
        return xml.toString();
    }

    /**
     * Strip the open/close tags (i.e. `<xml>`, `<!DOCTYPE>`, `<hibernate-mapping>`) from an hbm.xml file.
     * <p>
     * Useful for assembling multiple entities into a single `<hibernate-mapping>` element for sending to Hibernate.
     *
     * @param xml
     *            XML string from which to strip open and close tags
     *
     * @return an XML string with the DOCTYPE, `<xml>` and `<hibernate-mapping>` elements removed
     */
    public static String stripXMLOpenClose( String xml ) {
        return xml.replaceAll( "<\\?xml[^>]+>", "" ).replaceAll( "<!DOCTYPE[^>]+>", "" ).replaceAll( "</?hibernate-mapping>",
                "" );
    }
}

class PropertyCollection {

    private Property[] properties;
    private String tableName;

    public PropertyCollection( String tableName, Property[] properties ) {
        this.tableName  = tableName;
        this.properties = properties;
    }

    public PropertyCollection( String tableName, List<Property> properties ) {
        this.tableName  = tableName;
        this.properties = properties.toArray( new Property[ properties.size() ] );
    }

    public Property[] getProperties() {
        return properties;
    }

    public String getTableName() {
        return tableName;
    }

}
