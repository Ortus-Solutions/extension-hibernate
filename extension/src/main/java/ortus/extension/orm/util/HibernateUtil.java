package ortus.extension.orm.util;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import ortus.extension.orm.ColumnInfo;
import ortus.extension.orm.HibernateCaster;
import ortus.extension.orm.HibernateORMEngine;
import ortus.extension.orm.HibernateORMSession;
import ortus.extension.orm.SessionFactoryData;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.Property;
import lucee.runtime.config.Config;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

public class HibernateUtil {

    public static final short FIELDTYPE_ID = 0;
    public static final short FIELDTYPE_COLUMN = 1;
    public static final short FIELDTYPE_TIMESTAMP = 2;
    public static final short FIELDTYPE_RELATION = 4;
    public static final short FIELDTYPE_VERSION = 8;
    public static final short FIELDTYPE_COLLECTION = 16;

    private static final String[] KEYWORDS = new String[] { "absolute", "access", "accessible", "action", "add", "after", "alias",
            "all", "allocate", "allow", "alter", "analyze", "and", "any", "application", "are", "array", "as", "asc",
            "asensitive", "assertion", "associate", "asutime", "asymmetric", "at", "atomic", "audit", "authorization", "aux",
            "auxiliary", "avg", "backup", "before", "begin", "between", "bigint", "binary", "bit", "bit_length", "blob",
            "boolean", "both", "breadth", "break", "browse", "bufferpool", "bulk", "by", "cache", "call", "called", "capture",
            "cardinality", "cascade", "cascaded", "case", "cast", "catalog", "ccsid", "change", "char", "char_length",
            "character", "character_length", "check", "checkpoint", "clob", "close", "cluster", "clustered", "coalesce",
            "collate", "collation", "collection", "collid", "column", "comment", "commit", "compress", "compute", "concat",
            "condition", "connect", "connection", "constraint", "constraints", "constructor", "contains", "containstable",
            "continue", "convert", "corresponding", "count", "count_big", "create", "cross", "cube", "current", "current_date",
            "current_default_transform_group", "current_lc_ctype", "current_path", "current_role", "current_server",
            "current_time", "current_timestamp", "current_timezone", "current_transform_group_for_type", "current_user", "cursor",
            "cycle", "data", "database", "databases", "date", "day", "day_hour", "day_microsecond", "day_minute", "day_second",
            "days", "db2general", "db2genrl", "db2sql", "dbcc", "dbinfo", "deallocate", "dec", "decimal", "declare", "default",
            "defaults", "deferrable", "deferred", "delayed", "delete", "deny", "depth", "deref", "desc", "describe", "descriptor",
            "deterministic", "diagnostics", "disallow", "disconnect", "disk", "distinct", "distinctrow", "distributed", "div",
            "do", "domain", "double", "drop", "dsnhattr", "dssize", "dual", "dummy", "dump", "dynamic", "each", "editproc",
            "else", "elseif", "enclosed", "encoding", "end", "end-exec", "end-exec1", "endexec", "equals", "erase", "errlvl",
            "escape", "escaped", "except", "exception", "excluding", "exclusive", "exec", "execute", "exists", "exit", "explain",
            "external", "extract", "false", "fenced", "fetch", "fieldproc", "file", "fillfactor", "filter", "final", "first",
            "float", "float4", "float8", "for", "force", "foreign", "found", "free", "freetext", "freetexttable", "from", "full",
            "fulltext", "function", "general", "generated", "get", "get_current_connection", "global", "go", "goto", "grant",
            "graphic", "group", "grouping", "handler", "having", "high_priority", "hold", "holdlock", "hour", "hour_microsecond",
            "hour_minute", "hour_second", "hours", "identified", "identity", "identity_insert", "identitycol", "if", "ignore",
            "immediate", "in", "including", "increment", "index", "indicator", "infile", "inherit", "initial", "initially",
            "inner", "inout", "input", "insensitive", "insert", "int", "int1", "int2", "int3", "int4", "int8", "integer",
            "integrity", "intersect", "interval", "into", "is", "isobid", "isolation", "iterate", "jar", "java", "join", "key",
            "keys", "kill", "language", "large", "last", "lateral", "leading", "leave", "left", "level", "like", "limit",
            "linear", "lineno", "lines", "linktype", "load", "local", "locale", "localtime", "localtimestamp", "locator",
            "locators", "lock", "lockmax", "locksize", "long", "longblob", "longint", "longtext", "loop", "low_priority", "lower",
            "ltrim", "map", "master_ssl_verify_server_cert", "match", "max", "maxextents", "maxvalue", "mediumblob", "mediumint",
            "mediumtext", "method", "microsecond", "microseconds", "middleint", "min", "minus", "minute", "minute_microsecond",
            "minute_second", "minutes", "minvalue", "mlslabel", "mod", "mode", "modifies", "modify", "module", "month", "months",
            "names", "national", "natural", "nchar", "nclob", "new", "new_table", "next", "no", "no_write_to_binlog", "noaudit",
            "nocache", "nocheck", "nocompress", "nocycle", "nodename", "nodenumber", "nomaxvalue", "nominvalue", "nonclustered",
            "none", "noorder", "not", "nowait", "null", "nullif", "nulls", "number", "numeric", "numparts", "nvarchar", "obid",
            "object", "octet_length", "of", "off", "offline", "offsets", "old", "old_table", "on", "online", "only", "open",
            "opendatasource", "openquery", "openrowset", "openxml", "optimization", "optimize", "option", "optionally", "or",
            "order", "ordinality", "out", "outer", "outfile", "output", "over", "overlaps", "overriding", "package", "pad",
            "parameter", "part", "partial", "partition", "path", "pctfree", "percent", "piecesize", "plan", "position",
            "precision", "prepare", "preserve", "primary", "print", "prior", "priqty", "privileges", "proc", "procedure",
            "program", "psid", "public", "purge", "queryno", "raiserror", "range", "raw", "read", "read_write", "reads",
            "readtext", "real", "reconfigure", "recovery", "recursive", "ref", "references", "referencing", "regexp", "relative",
            "release", "rename", "repeat", "replace", "replication", "require", "resignal", "resource", "restart", "restore",
            "restrict", "result", "result_set_locator", "return", "returns", "revoke", "right", "rlike", "role", "rollback",
            "rollup", "routine", "row", "rowcount", "rowguidcol", "rowid", "rownum", "rows", "rrn", "rtrim", "rule", "run",
            "runtimestatistics", "save", "savepoint", "schema", "schemas", "scope", "scratchpad", "scroll", "search", "second",
            "second_microsecond", "seconds", "secqty", "section", "security", "select", "sensitive", "separator", "session",
            "session_user", "set", "sets", "setuser", "share", "show", "shutdown", "signal", "similar", "simple", "size",
            "smallint", "some", "source", "space", "spatial", "specific", "specifictype", "sql", "sql_big_result",
            "sql_calc_found_rows", "sql_small_result", "sqlcode", "sqlerror", "sqlexception", "sqlid", "sqlstate", "sqlwarning",
            "ssl", "standard", "start", "starting", "state", "static", "statistics", "stay", "stogroup", "stores",
            "straight_join", "style", "subpages", "substr", "substring", "successful", "sum", "symmetric", "synonym", "sysdate",
            "sysfun", "sysibm", "sysproc", "system", "system_user", "table", "tablespace", "temporary", "terminated", "textsize",
            "then", "time", "timestamp", "timezone_hour", "timezone_minute", "tinyblob", "tinyint", "tinytext", "to", "top",
            "trailing", "tran", "transaction", "translate", "translation", "treat", "trigger", "trim", "true", "truncate",
            "tsequal", "type", "uid", "under", "undo", "union", "unique", "unknown", "unlock", "unnest", "unsigned", "until",
            "update", "updatetext", "upper", "usage", "use", "user", "using", "utc_date", "utc_time", "utc_timestamp", "validate",
            "validproc", "value", "values", "varbinary", "varchar", "varchar2", "varcharacter", "variable", "variant", "varying",
            "vcat", "view", "volumes", "waitfor", "when", "whenever", "where", "while", "window", "with", "within", "without",
            "wlm", "work", "write", "writetext", "xor", "year", "year_month", "zerofill", "zone" };
    private static final Set<String> keywords = new HashSet<>();
    static {
        for ( int i = 0; i < KEYWORDS.length; i++ ) {
            keywords.add( KEYWORDS[ i ] );
        }
    }

    private HibernateUtil() {
        throw new IllegalStateException( "Utility class; please don't instantiate!" );
    }

    public static boolean isKeyword( String word ) {
        if ( word == null )
            return false;
        return keywords.contains( word.trim().toLowerCase() );
    }

    public static Type getPropertyType( ClassMetadata metaData, String name ) throws HibernateException {
        try {
            return metaData.getPropertyType( name );
        } catch ( HibernateException he ) {
            if ( name.equalsIgnoreCase( metaData.getIdentifierPropertyName() ) )
                return metaData.getIdentifierType();

            String[] names = metaData.getPropertyNames();
            for ( int i = 0; i < names.length; i++ ) {
                if ( names[ i ].equalsIgnoreCase( name ) )
                    return metaData.getPropertyType( names[ i ] );
            }
            throw he;
        }
    }

    public static Type getPropertyType( ClassMetadata metaData, String name, Type defaultValue ) {
        try {
            return metaData.getPropertyType( name );
        } catch ( HibernateException he ) {
            if ( name.equalsIgnoreCase( metaData.getIdentifierPropertyName() ) )
                return metaData.getIdentifierType();

            String[] names = metaData.getPropertyNames();
            for ( int i = 0; i < names.length; i++ ) {
                if ( names[ i ].equalsIgnoreCase( name ) )
                    return metaData.getPropertyType( names[ i ] );
            }
            return defaultValue;
        }
    }

    /**
     * Validate a column name exists in the given hibernate metadata object.
     * 
     * @param metaData Class-specific metadata. This must change to use Hibernate's `MappingMetamodel` object
     * @param name Column name to look for
     * @return Currently void, but in the future this method will return a boolean and move exception generation to {@link ExceptionUtil}.
     * @throws PageException
     */
    public static String validateColumnName( ClassMetadata metaData, String name ) throws PageException {
        String res = validateColumnName( metaData, name, null );
        if ( res != null )
            return res;
        String message = String.format( "invalid name, there is no property with name [%s] in the entity [%s]", name,
                metaData.getEntityName() );
        String detail = String.format( "valid properties names are [%s]",
                CommonUtil.toList( metaData.getPropertyNames(), ", " ) );
        throw ExceptionUtil.createException( message, detail );

    }

    /**
     * Validate a column name exists in the given hibernate metadata object. If not found, will return default value
     * 
     * @param metaData Class-specific metadata. This must change to use Hibernate's `MappingMetamodel` object
     * @param name Column name to look for
     * @defaultValue Default to return if column not found.
     * @return The default value if column name not found.
     * @throws PageException
     */
    public static String validateColumnName( ClassMetadata metaData, String name, String defaultValue ) {
        Type type = metaData.getIdentifierType();
        // composite id
        if ( type.isComponentType() ) {
            Optional<String> match = findMatchingFieldname( ( ( ComponentType ) type ).getPropertyNames(), name );
            if ( match.isPresent() )
                return match.get();
        }
        // regular id
        String id = metaData.getIdentifierPropertyName();
        if ( id != null && name.equalsIgnoreCase( id ) )
            return metaData.getIdentifierPropertyName();

        Optional<String> match = findMatchingFieldname( metaData.getPropertyNames(), name );
        return match.orElseGet(()-> defaultValue );
    }

    private static Optional<String> findMatchingFieldname( String[] names, String name ) {
        return Arrays.stream( names ).filter( name::equalsIgnoreCase ).findFirst();
    }

    //

    public static Property[] createPropertiesFromTable( DatasourceConnection dc, String tableName ) {
        Struct properties = CommonUtil.createStruct();
        try {
            DatabaseMetaData md = dc.getConnection().getMetaData();
            String dbName = CFMLEngineFactory.getInstance().getDBUtil().getDatabaseName( dc );
            Collection.Key name;

            // get all columns
            ResultSet res = md.getColumns( dbName, null, tableName, null );
            while ( res.next() ) {
                name = CommonUtil.createKey( res.getString( "COLUMN_NAME" ) );
                properties.setEL( name, CommonUtil.createProperty( name.getString(), res.getString( "TYPE_NAME" ) ) );
            }

            // ids
            res = md.getPrimaryKeys( null, null, tableName );
            Property p;
            while ( res.next() ) {
                name = CommonUtil.createKey( res.getString( "COLUMN_NAME" ) );
                p    = ( Property ) properties.get( name, null );
                if ( p != null )
                    p.getDynamicAttributes().setEL( CommonUtil.FIELDTYPE, "id" );
            }

            // @TODO: foreign-key relation

        } catch ( Throwable t ) {
            if ( t instanceof ThreadDeath )
                throw ( ThreadDeath ) t;
            return new Property[ 0 ];
        }

        Iterator<Object> it = properties.valueIterator();
        Property[] rtn = new Property[ properties.size() ];
        for ( int i = 0; i < rtn.length; i++ ) {
            rtn[ i ] = ( Property ) it.next();
        }

        return rtn;
    }

    public static Property[] getProperties( Component component, int fieldType, Property[] defaultValue ) {
        Property[] props = component.getProperties( true, false, false, false );
        List<Property> rtn = new ArrayList<>();

        if ( props != null ) {
            for ( int i = 0; i < props.length; i++ ) {
                if ( fieldType == getFieldType( props[ i ], FIELDTYPE_COLUMN ) )
                    rtn.add( props[ i ] );
            }
        }
        return rtn.toArray( new Property[ rtn.size() ] );
    }

    private static int getFieldType( Property property, int defaultValue ) {
        return getFieldType( CommonUtil.toString( property.getDynamicAttributes().get( CommonUtil.FIELDTYPE, null ), null ),
                defaultValue );

    }

    private static int getFieldType( String fieldType, int defaultValue ) {
        if ( Util.isEmpty( fieldType, true ) )
            return defaultValue;
        fieldType = fieldType.trim().toLowerCase();

        if ( "id".equals( fieldType ) )
            return FIELDTYPE_ID;
        if ( "column".equals( fieldType ) )
            return FIELDTYPE_COLUMN;
        if ( "timestamp".equals( fieldType ) )
            return FIELDTYPE_TIMESTAMP;
        if ( "relation".equals( fieldType ) )
            return FIELDTYPE_RELATION;
        if ( "version".equals( fieldType ) )
            return FIELDTYPE_VERSION;
        if ( "collection".equals( fieldType ) )
            return FIELDTYPE_COLLECTION;
        return defaultValue;
    }

    public static String convertTableName( SessionFactoryData data, String tableName ) throws PageException {
        if ( tableName == null )
            return null;
        return data.getNamingStrategy().convertTableName( tableName );
    }

    public static String convertColumnName( SessionFactoryData data, String columnName ) throws PageException {
        if ( columnName == null )
            return null;
        return data.getNamingStrategy().convertColumnName( columnName );
    }

    public static boolean isEntity( ORMConfiguration ormConf, Component cfc, String cfcName, String name ) {
        if ( !Util.isEmpty( cfcName ) ) {
            if ( cfc.equalTo( cfcName ) )
                return true;

            if ( cfcName.indexOf( '.' ) != -1 ) {
                String prefix = cfcName.replace( '.', '/' ) + ".";
                Resource[] locations = ormConf.getCfcLocations();
                Resource res;
                for ( int i = 0; i < locations.length; i++ ) {
                    res = locations[ i ].getRealResource( prefix + "cfc" );
                    if ( res.equals( cfc.getPageSource().getResource() ) )
                        return true;
                }
                return false;
            }
        }

        if ( cfc.equalTo( name ) )
            return true;
        return name.equalsIgnoreCase( HibernateCaster.getEntityName( cfc ) );
    }

    /**
     * Sanitize an entity name for use as a collection key
     *
     * @param entityName
     *
     * @return The lowercased entity name with whitespace trimmed.
     */
    public static String sanitizeEntityName( String entityName ) {
        return entityName.toLowerCase().trim();
    }

    public static Struct checkTable( DatasourceConnection dc, String tableName ) throws PageException {

        try {
            String dbName = CFMLEngineFactory.getInstance().getDBUtil().getDatabaseName( dc );
            DatabaseMetaData md = dc.getConnection().getMetaData();
            Struct rows = checkTableFill( md, dbName, tableName );
            if ( rows.isEmpty() ) {
                String tableName2 = checkTableValidate( md, dbName, tableName );
                if ( tableName2 != null )
                    rows = checkTableFill( md, dbName, tableName2 );
            }

            if ( rows.isEmpty() ) {
                return null;
            }
            return rows;
        } catch ( SQLException e ) {
            throw ExceptionUtil.toPageException( e );
        }
    }

    private static Struct checkTableFill( DatabaseMetaData md, String dbName, String tableName )
            throws SQLException, PageException {
        Struct rows = CFMLEngineFactory.getInstance().getCreationUtil().createCastableStruct( tableName, Struct.TYPE_LINKED );

        try ( ResultSet columns = md.getColumns( dbName, null, tableName, null ); ) {
            String name;
            Object nullable;
            while ( columns.next() ) {
                name     = columns.getString( "COLUMN_NAME" );

                nullable = columns.getObject( "IS_NULLABLE" );
                rows.setEL( CommonUtil.createKey( name ),
                        new ColumnInfo( name, columns.getInt( "DATA_TYPE" ), columns.getString( "TYPE_NAME" ),
                                columns.getInt( "COLUMN_SIZE" ), CommonUtil.toBooleanValue( nullable ) ) );
            }
        }

        return rows;
    }

    private static String checkTableValidate( DatabaseMetaData md, String dbName, String tableName ) {

        try ( ResultSet tables = md.getTables( dbName, null, null, null ); ) {
            String name;
            while ( tables.next() ) {
                name = tables.getString( "TABLE_NAME" );
                if ( name.equalsIgnoreCase( tableName )
                        && tables.getString( "TABLE_TYPE" ).toUpperCase().indexOf( "SYSTEM" ) == -1 )
                    return name;
            }
        } catch ( Throwable t ) {
            if ( t instanceof ThreadDeath )
                throw ( ThreadDeath ) t;
        }
        return null;

    }

    public static HibernateORMEngine getORMEngine( PageContext pc ) throws PageException {
        if ( pc == null )
            pc = CommonUtil.pc();
        Config config = pc.getConfig();
        return ( HibernateORMEngine ) config.getORMEngine( pc );// TODO add this method to the public interface
    }

    public static HibernateORMSession getORMSession( PageContext pc, boolean create ) throws PageException {
        return ( HibernateORMSession ) pc.getORMSession( create );// TODO add this method to the public interface
    }

    public static Property[] getIDProperties( Component c, boolean onlyPeristent, boolean includeBaseProperties ) {
        Property[] props = CommonUtil.getProperties( c, onlyPeristent, includeBaseProperties, false, false );
        List<Property> tmp = new ArrayList<>();
        for ( int i = 0; i < props.length; i++ ) {
            if ( "id".equalsIgnoreCase(
                    CommonUtil.toString( props[ i ].getDynamicAttributes().get( CommonUtil.FIELDTYPE, null ), "" ) ) )
                tmp.add( props[ i ] );
        }
        return tmp.toArray( new Property[ tmp.size() ] );
    }

    public static long getCompileTime( PageContext pc, PageSource ps ) throws PageException {
        return CFMLEngineFactory.getInstance().getTemplateUtil().getCompileTime( pc, ps );
    }

    public static String[] merge( String[] arr1, String[] arr2 ) {
        String[] ret = new String[ arr1.length + arr2.length ];
        for ( int i = 0; i < arr1.length; i++ ) {
            ret[ i ] = arr1[ i ];
        }
        for ( int i = 0; i < arr2.length; i++ ) {
            ret[ arr1.length + i ] = arr2[ i ];
        }
        return ret;
    }

    /**
     * Check filename string against known application.cfc name.
     *
     * @param name
     *             Filename to check
     *
     * @return True if string matches 'application.cfc'.
     */
    public static boolean isApplicationName( String name ) {
        return name.toLowerCase().equalsIgnoreCase( "application.cfc" );
    }
}
