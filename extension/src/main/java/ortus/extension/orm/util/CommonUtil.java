package ortus.extension.orm.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.util.Map;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefBoolean;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.config.Config;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLItem;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.ext.function.Function;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;
import lucee.runtime.util.DBUtil;
import lucee.runtime.util.Decision;
import lucee.runtime.util.ORMUtil;
import lucee.runtime.util.Operation;
import lucee.runtime.op.Castable;
import lucee.runtime.type.ObjectWrap;

public class CommonUtil {

    public static final Key ENTITY_NAME = CommonUtil.createKey( "entityname" );
    public static final Key FIELDTYPE = CommonUtil.createKey( "fieldtype" );
    public static final Key INIT = CommonUtil.createKey( "init" );
    private static final short INSPECT_UNDEFINED = ( short ) 4; /* ConfigImpl.INSPECT_UNDEFINED */
    private static Charset charset;

    private static Charset utf8Charset;
    private static Charset utf16beCharset;
    private static Charset uitf16leCharset;

    private static Cast caster;
    private static Decision decision;
    private static Creation creator;
    private static Operation op;
    private static lucee.runtime.util.ListUtil list;
    private static DBUtil db;
    private static ORMUtil orm;

    private static Method mGetDatasourceConnection;
    private static Method mReleaseDatasourceConnection;

    private CommonUtil() {
        throw new IllegalStateException( "Utility class; please don't instantiate!" );
    }

    public static Charset getCharset() {
        if ( charset == null ) {
            String strCharset = System.getProperty( "file.encoding" );
            if ( strCharset == null || strCharset.equalsIgnoreCase( "MacRoman" ) )
                strCharset = "cp1252";

            if ( strCharset.equalsIgnoreCase( "utf-8" ) )
                charset = getUTF8Charset();
            else
                charset = toCharset( strCharset );
        }
        return charset;
    }

    public static Charset getUTF8Charset() {
        if ( utf8Charset == null )
            utf8Charset = toCharset( "UTF-8" );
        return utf8Charset;
    }

    private static Charset getUTF16LECharset() {
        if ( uitf16leCharset == null )
            uitf16leCharset = toCharset( "UTF-16LE" );
        return uitf16leCharset;
    }

    private static Charset getUTF16BECharset() {
        if ( utf16beCharset == null )
            utf16beCharset = toCharset( "UTF-16BE" );
        return utf16beCharset;
    }

    private static Charset toCharset( String charset ) {
        try {
            return CFMLEngineFactory.getInstance().getCastUtil().toCharset( charset );
        } catch ( PageException pe ) {
            throw CFMLEngineFactory.getInstance().getExceptionUtil().createPageRuntimeException( pe );
        }
    }

    @SuppressWarnings("rawtypes")
    public static Object castTo( PageContext pc, Class trgClass, Object obj ) throws PageException {
        return caster().castTo( pc, trgClass, obj );
    }

    public static Array toArray( Object obj ) throws PageException {
        return caster().toArray( obj );
    }

    public static Array toArray( Object obj, Array defaultValue ) {
        return caster().toArray( obj, defaultValue );
    }

    public static Boolean toBoolean( String str ) throws PageException {
        return caster().toBoolean( str );
    }

    public static Boolean toBoolean( String str, Boolean defaultValue ) {
        return caster().toBoolean( str, defaultValue );
    }

    public static Boolean toBoolean( Object obj ) throws PageException {
        return caster().toBoolean( obj );
    }

    public static Boolean toBoolean( Object obj, Boolean defaultValue ) {
        return caster().toBoolean( obj, defaultValue );
    }

    public static Boolean toBooleanValue( String str ) throws PageException {
        return caster().toBooleanValue( str );
    }

    public static Boolean toBooleanValue( String str, Boolean defaultValue ) {
        return caster().toBooleanValue( str, defaultValue );
    }

    public static boolean toBooleanValue( Object obj ) throws PageException {
        return caster().toBooleanValue( obj );
    }

    public static boolean toBooleanValue( Object obj, boolean defaultValue ) {
        return caster().toBooleanValue( obj, defaultValue );
    }

    public static Component toComponent( Object obj ) throws PageException {
        return caster().toComponent( obj );
    }

    public static Component toComponent( Object obj, Component defaultValue ) {
        return caster().toComponent( obj, defaultValue );
    }

    public static Object toList( String[] arr, String delimiter ) {
        return list().toList( arr, delimiter );
    }

    public static String toString( Object obj, String defaultValue ) {
        return caster().toString( obj, defaultValue );
    }

    public static String toString( Object obj ) throws PageException {
        return caster().toString( obj );
    }

    public static String toString( boolean b ) {
        return caster().toString( b );
    }

    public static String toString( double d ) {
        return caster().toString( d );
    }

    public static String toString( int i ) {
        return caster().toString( i );
    }

    public static String toString( long l ) {
        return caster().toString( l );
    }

    /**
     * reads String data from File
     *
     * @param file
     * @param charset
     *
     * @return readed string
     *
     * @throws IOException
     */
    public static String toString( Resource file, Charset charset ) throws IOException {
        try ( Reader r = getReader( file, charset ); ) {
            return toString( r );
        }
    }

    public static String toString( Reader reader ) throws IOException {
        StringWriter sw = new StringWriter( 512 );
        copy( toBufferedReader( reader ), sw );
        sw.close();
        return sw.toString();
    }

    public static BufferedReader toBufferedReader( Reader r ) {
        if ( r instanceof BufferedReader )
            return ( BufferedReader ) r;
        return new BufferedReader( r );
    }

    private static final void copy( Reader r, Writer w ) throws IOException {
        copy( r, w, 0xffff );
    }

    private static final void copy( Reader r, Writer w, int blockSize ) throws IOException {
        char[] buffer = new char[ blockSize ];
        int len;

        while ( ( len = r.read( buffer ) ) != -1 )
            w.write( buffer, 0, len );
    }

    public static Reader getReader( Resource res, Charset charset ) throws IOException {
        InputStream is = null;
        try {
            is = res.getInputStream();
            boolean markSupported = is.markSupported();
            if ( markSupported )
                is.mark( 4 );
            int first = is.read();
            int second = is.read();
            // FE FF UTF-16, big-endian
            if ( first == 0xFE && second == 0xFF ) {
                return getReaderForInputStream( is, getUTF16BECharset() );
            }
            // FF FE UTF-16, little-endian
            if ( first == 0xFF && second == 0xFE ) {
                return getReaderForInputStream( is, getUTF16LECharset() );
            }

            int third = is.read();
            // EF BB BF UTF-8
            if ( first == 0xEF && second == 0xBB && third == 0xBF ) {
                return getReaderForInputStream( is, getUTF8Charset() );
            }

            if ( markSupported ) {
                is.reset();
                return getReaderForInputStream( is, charset );
            }
        } catch ( IOException ioe ) {
            closeEL( is );
            throw ioe;
        }

        // when mark not supported return new reader
        closeEL( is );
        is = null;
        try {
            is = res.getInputStream();
        } catch ( IOException ioe ) {
            closeEL( is );
            throw ioe;
        }
        return getReaderForInputStream( is, charset );
    }

    private static Reader getReaderForInputStream( InputStream is, Charset cs ) {
        // TODO: This is a very bad pattern - setting and using class state on a util class from a static method.
        if ( cs == null ) {
            cs = getCharset();
        }
        return new BufferedReader( new InputStreamReader( is, cs ) );
    }

    public static String[] toStringArray( String list, String delimiter ) {
        return list().toStringArray( list().toArray( list, delimiter ), "" );
    }

    public static Float toFloat( Object obj ) throws PageException {
        return caster().toFloat( obj );
    }

    public static Float toFloat( Object obj, Float defaultValue ) {
        return caster().toFloat( obj, defaultValue );
    }

    public static float toFloatValue( Object obj ) throws PageException {
        return caster().toFloatValue( obj );
    }

    public static float toFloatValue( Object obj, float defaultValue ) {
        return caster().toFloatValue( obj, defaultValue );
    }

    public static Double toDouble( Object obj ) throws PageException {
        return caster().toDouble( obj );
    }

    public static Double toDouble( Object obj, Double defaultValue ) {
        return caster().toDouble( obj, defaultValue );
    }

    public static double toDoubleValue( Object obj ) throws PageException {
        return caster().toDoubleValue( obj );
    }

    public static double toDoubleValue( Object obj, double defaultValue ) {
        return caster().toDoubleValue( obj, defaultValue );
    }

    public static BigDecimal toBigDecimal( Object obj ) throws PageException {
        return caster().toBigDecimal( obj );
    }

    public static BigDecimal toBigDecimal( Object obj, BigDecimal defaultValue ) {
        return caster().toBigDecimal( obj, defaultValue );
    }

    public static Short toShort( Object obj ) throws PageException {
        return caster().toShort( obj );
    }

    public static Short toShort( Object obj, Short defaultValue ) {
        return caster().toShort( obj, defaultValue );
    }

    public static double toShortValue( Object obj ) throws PageException {
        return caster().toShortValue( obj );
    }

    public static double toShortValue( Object obj, short defaultValue ) {
        return caster().toShortValue( obj, defaultValue );
    }

    public static Integer toInteger( Object obj ) throws PageException {
        return caster().toInteger( obj );
    }

    public static Integer toInteger( Object obj, Integer defaultValue ) {
        return caster().toInteger( obj, defaultValue );
    }

    public static Long toLong( Object obj ) throws PageException {
        return caster().toLong( obj );
    }

    public static Long toLong( Object obj, Long defaultValue ) {
        return caster().toLong( obj, defaultValue );
    }

    public static long toLongValue( Object obj ) throws PageException {
        return caster().toLongValue( obj );
    }

    public static long toLongValue( Object obj, long defaultValue ) {
        return caster().toLongValue( obj, defaultValue );
    }

    public static byte[] toBinary( Object obj ) throws PageException {
        return caster().toBinary( obj );
    }

    public static byte[] toBinary( Object obj, byte[] defaultValue ) {
        return caster().toBinary( obj, defaultValue );
    }

    public static int toIntValue( Object obj ) throws PageException {
        return caster().toIntValue( obj );
    }

    public static int toIntValue( Object obj, int defaultValue ) {
        return caster().toIntValue( obj, defaultValue );
    }

    public static Array toArray( Argument arg ) {
        Array trg = createArray();
        int[] keys = arg.intKeys();
        for ( int i = 0; i < keys.length; i++ ) {
            trg.setEL( keys[ i ], arg.get( keys[ i ], null ) );
        }
        return trg;
    }

    public static Serializable toSerializable( Object obj ) throws PageException {
        return caster().toSerializable( obj );
    }

    public static Serializable toSerializable( Object obj, Serializable defaultValue ) {
        return caster().toSerializable( obj, defaultValue );
    }

    public static Struct toStruct( Object obj ) throws PageException {
        return caster().toStruct( obj );
    }

    public static Struct toStruct( Object obj, Struct defaultValue ) {
        return caster().toStruct( obj, defaultValue );
    }

    public static SQLItem toSQLItem( Object value, int type ) {
        return db().toSQLItem( value, type );
    }

    public static SQL toSQL( String sql, SQLItem[] items ) {
        return db().toSQL( sql, items );
    }

    public static Object toSqlType( SQLItem item ) throws PageException {
        return db().toSqlType( item );
    }

    public static Object[] toNativeArray( Object obj ) throws PageException {
        return caster().toNativeArray( obj );
    }

    public static Key toKey( String str ) {
        return caster().toKey( str );
    }

    public static String toTypeName( Object obj ) {
        return caster().toTypeName( obj );
    }

    public static Node toXML( Object obj ) throws PageException {
        return XMLUtil.toNode( obj );
    }

    /**
     * Parse XML file contents into an XML object ready for manipulation
     * 
     * @param res Resource (File) to read
     * @param cs  Charset to use when parsing document
     * 
     * @return XML Document
     * 
     * @throws PageException
     */
    public static Document toDocument( Resource res, Charset cs ) throws PageException {
        return XMLUtil.parse( XMLUtil.toInputSource( res, cs ), null, false );
    }

    public static boolean isArray( Object obj ) {
        return decision().isArray( obj );
    }

    public static boolean isStruct( Object obj ) {
        return decision().isStruct( obj );
    }

    /**
     * See if a given value is coercable to a string.
     * <p>
     * Blatantly copied from Lucee core because it's not in the Lucee loader, so we don't have access to run it without
     * reflection.
     *
     * @link https://github.com/lucee/Lucee/blob/6.0/core/src/main/java/lucee/runtime/op/Decision.java#L964
     *
     * @param o
     *          Value to compare
     *
     * @return Boolean, true if value is a String or castable to a String.
     */
    public static boolean isString( Object o ) {
        if ( o instanceof String )
            return true;
        else if ( o instanceof Boolean )
            return true;
        else if ( o instanceof Number )
            return true;
        else if ( o instanceof Date )
            return true;
        else if ( o instanceof Castable ) {
            return !( ( Castable ) o ).castToString( "this is a unique string" ).equals("this is a unique string");

        } else if ( o instanceof Clob )
            return true;
        else if ( o instanceof Node )
            return true;
        else if ( o instanceof Map || o instanceof List || o instanceof Function )
            return false;
        else if ( o == null )
            return true;
        else if ( o instanceof ObjectWrap )
            return isString( ( ( ObjectWrap ) o ).getEmbededObject( "" ) );
        return true;
    }

    public static boolean isSimpleValue( Object obj ) {
        return decision().isSimpleValue( obj );
    }

    public static boolean isCastableToBoolean( Object obj ) {
        return decision().isCastableToBoolean( obj );
    }

    public static boolean isCastableToArray( Object o ) {
        return decision().isCastableToArray( o );
    }

    public static boolean isCastableToStruct( Object o ) {
        return decision().isCastableToStruct( o );
    }

    public static boolean isBinary( Object obj ) {
        return decision().isBinary( obj );
    }

    public static boolean isBoolean( Object obj ) {
        return decision().isBoolean( obj );
    }

    public static boolean isAnyType( String type ) {
        return decision().isAnyType( type );
    }

    public static Array createArray() {
        return creator().createArray();
    }

    public static DateTime createDateTime( long time ) {
        return creator().createDateTime( time );
    }

    public static Property createProperty( String name, String type ) {
        return creator().createProperty( name, type );
    }

    public static Struct createStruct() {
        return creator().createStruct();
    }

    public static Collection.Key createKey( String key ) {
        return creator().createKey( key );
    }

    public static Query createQuery( Collection.Key[] columns, int rows, String name ) throws PageException {
        return creator().createQuery( columns, rows, name );
    }

    public static Query createQuery( Collection.Key[] columns, String[] types, int rows, String name ) throws PageException {
        return creator().createQuery( columns, types, rows, name );
    }

    public static Query createQuery( Array names, Array types, int rows, String name ) throws PageException {
        Collection.Key[] knames = new Collection.Key[ names.size() ];
        String[] ktypes = new String[ types.size() ];
        for ( int i = names.size() - 1; i >= 0; i-- ) {
            knames[ i ] = caster().toKey( names.getE( i + 1 ) );
            ktypes[ i ] = caster().toString( types.getE( i + 1 ) );
        }
        return creator().createQuery( knames, ktypes, rows, name );
    }

    public static RefBoolean createRefBoolean() {
        return new RefBooleanImpl();
    }

    public static Key[] keys( Collection coll ) {
        if ( coll == null )
            return new Key[ 0 ];
        Iterator<Key> it = coll.keyIterator();
        List<Key> rtn = new ArrayList<>();
        if ( it != null )
            while ( it.hasNext() ) {
                rtn.add( it.next() );
            }
        return rtn.toArray( new Key[ rtn.size() ] );
    }

    private static Creation creator() {
        if ( creator == null )
            creator = CFMLEngineFactory.getInstance().getCreationUtil();
        return creator;
    }

    private static Decision decision() {
        if ( decision == null )
            decision = CFMLEngineFactory.getInstance().getDecisionUtil();
        return decision;
    }

    static Cast caster() {
        if ( caster == null )
            caster = CFMLEngineFactory.getInstance().getCastUtil();
        return caster;
    }

    private static Operation op() {
        if ( op == null )
            op = CFMLEngineFactory.getInstance().getOperatonUtil();
        return op;
    }

    private static lucee.runtime.util.ListUtil list() {
        if ( list == null )
            list = CFMLEngineFactory.getInstance().getListUtil();
        return list;
    }

    private static ORMUtil orm() {
        if ( orm == null )
            orm = CFMLEngineFactory.getInstance().getORMUtil();
        return orm;
    }

    private static DBUtil db() {
        if ( db == null )
            db = CFMLEngineFactory.getInstance().getDBUtil();
        return db;
    }

    /**
     * Integer Type that can be modified
     */
    public static final class RefBooleanImpl implements RefBoolean {// @TODO: add interface Castable

        private boolean value;

        public RefBooleanImpl() {
        }

        /**
         * @param value
         */
        public RefBooleanImpl( boolean value ) {
            this.value = value;
        }

        /**
         * @param value
         */
        @Override
        public void setValue( boolean value ) {
            this.value = value;
        }

        /**
         * @return returns value as Boolean Object
         */
        @Override
        public Boolean toBoolean() {
            return value ? Boolean.TRUE : Boolean.FALSE;
        }

        /**
         * @return returns value as boolean value
         */
        @Override
        public boolean toBooleanValue() {
            return value;
        }

        @Override
        public String toString() {
            return value ? "true" : "false";
        }
    }

    /**
     * Get the datasource defined for the provided name, or the default if name is null.
     *
     * @param pc
     *             Lucee's PageContext object.
     * @param name
     *             Datasource name, or <code>null</code> to retrieve the default
     *
     * @return A Datasource object
     *
     * @throws PageException
     */
    public static DataSource getDataSource( PageContext pc, String name ) throws PageException {
        if ( Util.isEmpty( name, true ) )
            return orm().getDefaultDataSource( pc );
        return pc.getDataSource( name );
    }

    public static DatasourceConnection getDatasourceConnection( PageContext pc, DataSource ds, String user, String pass,
            boolean transactionSensitive ) throws PageException {
        if ( transactionSensitive ) {
            return pc.getDataSourceManager().getConnection( pc, ds, user, pass );

        }

        DBUtil dbutil = db();
        try {
            if ( mGetDatasourceConnection == null || dbutil.getClass() != mGetDatasourceConnection.getDeclaringClass() ) {
                mGetDatasourceConnection = dbutil.getClass().getMethod( "getDatasourceConnection", PageContext.class,
                        DataSource.class, String.class, String.class, boolean.class );
            }
            return ( DatasourceConnection ) mGetDatasourceConnection.invoke( dbutil, pc, ds, user, pass, false );
        } catch ( Exception e ) {
            throw ExceptionUtil.toPageException( e );
        }
    }

    public static void releaseDatasourceConnection( PageContext pc, DatasourceConnection dc, boolean transactionSensitive )
            throws PageException {
        if ( transactionSensitive ) {
            pc.getDataSourceManager().releaseConnection( pc, dc );
            return;
        }

        DBUtil dbutil = db();
        try {
            if ( mReleaseDatasourceConnection == null || dbutil.getClass() != mReleaseDatasourceConnection.getDeclaringClass() ) {
                mReleaseDatasourceConnection = dbutil.getClass().getMethod( "releaseDatasourceConnection", PageContext.class,
                        DatasourceConnection.class, boolean.class );
            }
            mReleaseDatasourceConnection.invoke( dbutil, pc, dc, false );
        } catch ( Exception e ) {
            throw ExceptionUtil.toPageException( e );
        }
    }

    public static Mapping createMapping( Config config, String virtual, String physical ) {
        return creator().createMapping( config, virtual, physical, null, INSPECT_UNDEFINED, true, false, false, false, true, true,
                null, -1, -1 );
    }

    public static String last( String list, String delimiter ) {
        return list().last( list, delimiter, true );
    }

    public static int listFindNoCaseIgnoreEmpty( String list, String value, char delimiter ) {
        return list().findNoCaseIgnoreEmpty( list, value, delimiter );
    }

    public static String[] trimItems( String[] arr ) {
        for ( int i = 0; i < arr.length; i++ ) {
            arr[ i ] = arr[ i ].trim();
        }
        return arr;
    }

    public static void setFirst( Node parent, Node node ) {
        XMLUtil.setFirst( parent, node );
    }

    public static Property[] getProperties( Component c, boolean onlyPeristent, boolean includeBaseProperties,
            boolean preferBaseProperties, boolean inheritedMappedSuperClassOnly ) {
        return c.getProperties( onlyPeristent, includeBaseProperties, preferBaseProperties, inheritedMappedSuperClassOnly );
    }

    public static void write( Resource res, String string, Charset cs, boolean append ) throws IOException {
        if ( cs == null )
            cs = getCharset();

        try ( Writer writer = getWriter( res, cs, append ); ) {
            writer.write( string );
        }
    }

    public static Writer getWriter( Resource res, Charset charset, boolean append ) throws IOException {
        OutputStream os = null;
        try {
            os = res.getOutputStream( append );
        } catch ( IOException ioe ) {
            closeEL( os );
            throw ioe;
        }
        return getWriter( os, charset );
    }

    public static Writer getWriter( OutputStream os, Charset cs ) {
        // TODO: This is a very bad pattern - setting and using class state on a util class from a static method.
        if ( cs == null ) {
            getCharset();
        }
        return new BufferedWriter( new OutputStreamWriter( os, getCharset() ) );
    }

    public static BufferedReader toBufferedReader( Resource res, Charset charset ) throws IOException {
        return toBufferedReader( getReader( res, ( Charset ) null ) );
    }

    public static boolean equalsComplexEL( Object left, Object right ) {
        return op().equalsComplexEL( left, right, false, true );
    }

    public static PageContext pc() {
        return CFMLEngineFactory.getInstance().getThreadPageContext();
    }

    public static Config config() {
        return pc().getConfig();
    }

    public static void closeEL( OutputStream os ) {
        if ( os != null ) {
            try {
                os.close();
            } catch ( Throwable t ) {
                if ( t instanceof ThreadDeath )
                    throw ( ThreadDeath ) t;
            }
        }
    }

    public static void closeEL( InputStream is ) {
        try {
            if ( is != null )
                is.close();
        } catch ( Throwable t ) {
            if ( t instanceof ThreadDeath )
                throw ( ThreadDeath ) t;
        }
    }

    public static boolean isRelated( Property property ) {
        return orm().isRelated( property );
    }

    public static Object convertToSimpleMap( String paramsStr ) {
        return orm().convertToSimpleMap( paramsStr );
    }

    public static String getDataSourceName( PageContext pc, Component cfc ) throws PageException {
        return orm().getDataSourceName( pc, cfc );
    }

    public static DataSource getDataSource( PageContext pc, Component cfc ) throws PageException {
        return orm().getDataSource( pc, cfc );
    }

    public static boolean equals( Component l, Component r ) {
        return orm().equals( l, r );
    }

    public static DataSource getDefaultDataSource( PageContext pc ) throws PageException {
        return orm().getDefaultDataSource( pc );
    }

    public static Object getPropertyValue( Component cfc, String name, Object defaultValue ) {
        return orm().getPropertyValue( cfc, name, defaultValue );
    }

    public static String toBase64( Object o ) throws PageException {
        return caster().toBase64( o );
    }

    public static Locale toLocale( String strLocale ) throws PageException {
        return caster().toLocale( strLocale );
    }

    /**
     * Convert the given value to a java.util.TimeZone.
     * 
     * @param value Value (likely a string value) to be parsed as a timezone.
     * @param defaultValue Default value (default TimeZone) to use if value is not convertable.
     * @throws PageException
     */
    public static TimeZone toTimeZone( Object value, TimeZone defaultValue ) throws PageException {
        return caster().toTimeZone( value, defaultValue );
    }

    public static Character toCharacter( Object value ) throws PageException {
        return caster().toCharacter( value );
    }

    public static DateTime toDate( Object value, TimeZone timeZone ) throws PageException {
        return caster().toDate( value, timeZone );
    }

    public static Calendar toCalendar( DateTime date, TimeZone timeZone, Locale locale ) {
        return caster().toCalendar( date.getTime(), timeZone, locale );
    }

    /**
     * Tests if this string starts with the specified prefix.
     * <p>
     * Blatantly copied from the Lucee core, since we don't have access to this method without reflection.
     *
     * @link https://github.com/lucee/Lucee/blob/6.0/core/src/main/java/lucee/commons/lang/StringUtil.java#L870
     *
     * @param str
     *               string to check first char
     * @param prefix
     *               the prefix.
     *
     * @return is first of given type
     */
    public static boolean startsWith( String str, char prefix ) {
        return str != null && str.length() > 0 && str.charAt( 0 ) == prefix;
    }

    /**
     * Tests if this string ends with the specified suffix.
     * <p>
     * Blatantly copied from the Lucee core, since we don't have access to this method without reflection.
     *
     * @link https://github.com/lucee/Lucee/blob/6.0/core/src/main/java/lucee/commons/lang/StringUtil.java#L870
     *
     * @param str
     *               string to check first char
     * @param suffix
     *               the suffix.
     *
     * @return is last of given type
     */
    public static boolean endsWith( String str, char suffix ) {
        return str != null && str.length() > 0 && str.charAt( str.length() - 1 ) == suffix;
    }
}
