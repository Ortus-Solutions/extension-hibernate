package ortus.extension.orm;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;

import ortus.extension.orm.runtime.type.KeyImpl;
import ortus.extension.orm.util.CommonUtil;
import org.osgi.framework.Bundle;

import lucee.loader.util.Util;
import lucee.runtime.db.DataSource;
import lucee.runtime.type.Struct;

/**
 * Hibernate Dialect manager
 */
public class Dialect {

    private static Struct dialects = CommonUtil.createStruct();

    static {

        try {
            BundleClassLoader bcl = ( BundleClassLoader ) org.hibernate.dialect.SybaseDialect.class.getClassLoader();
            Bundle b = bcl.getBundle();

            // List all XML files in the OSGI-INF directory and below
            Enumeration<URL> e = b.findEntries( "org/hibernate/dialect", "*.class", true );
            String path;
            while ( e.hasMoreElements() ) {
                try {
                    path = e.nextElement().getPath();
                    if ( path.startsWith( "/" ) )
                        path = path.substring( 1 );
                    else if ( path.startsWith( "\\" ) )
                        path = path.substring( 1 );
                    if ( path.endsWith( ".class" ) )
                        path = path.substring( 0, path.length() - 6 );
                    path = path.replace( '/', '.' );
                    path = path.replace( '\\', '.' );
                    String name;
                    Class<?> clazz = bcl.loadClass( path );
                    if ( org.hibernate.dialect.Dialect.class.isAssignableFrom( clazz )
                            && !Modifier.isAbstract( clazz.getModifiers() ) ) {
                        dialects.setEL( new KeyImpl( path ), path );
                        dialects.setEL( new KeyImpl( CommonUtil.last( path, "." ) ), path );
                        name = CommonUtil.last( path, "." );
                        dialects.setEL( new KeyImpl( name ), path );
                        if ( name.endsWith( "Dialect" ) ) {
                            name = name.substring( 0, name.length() - 7 );
                            dialects.setEL( new KeyImpl( name ), path );
                        }

                    }
                } catch ( Exception exx ) {
                    exx.printStackTrace();
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        dialects.setEL( new KeyImpl( "CUBRID" ), "org.hibernate.dialect.CUBRIDDialect" );
        dialects.setEL( new KeyImpl( "Cache71" ), "org.hibernate.dialect.Cache71Dialect" );
        dialects.setEL( new KeyImpl( "CockroachDB192" ), "org.hibernate.dialect.CockroachDB192Dialect" );
        dialects.setEL( new KeyImpl( "CockroachDB201" ), "org.hibernate.dialect.CockroachDB201Dialect" );
        dialects.setEL( new KeyImpl( "DB2390" ), "org.hibernate.dialect.DB2390Dialect" );
        dialects.setEL( new KeyImpl( "DB2390V8" ), "org.hibernate.dialect.DB2390V8Dialect" );
        dialects.setEL( new KeyImpl( "DB2400" ), "org.hibernate.dialect.DB2400Dialect" );
        dialects.setEL( new KeyImpl( "DB2400V7R3" ), "org.hibernate.dialect.DB2400V7R3Dialect" );
        dialects.setEL( new KeyImpl( "DB297" ), "org.hibernate.dialect.DB297Dialect" );
        dialects.setEL( new KeyImpl( "DB2" ), "org.hibernate.dialect.DB2Dialect" );
        dialects.setEL( new KeyImpl( "DataDirectOracle9" ), "org.hibernate.dialect.DataDirectOracle9Dialect" );
        dialects.setEL( new KeyImpl( "Derby" ), "org.hibernate.dialect.DerbyDialect" );
        dialects.setEL( new KeyImpl( "DerbyTenFive" ), "org.hibernate.dialect.DerbyTenFiveDialect" );
        dialects.setEL( new KeyImpl( "DerbyTenSeven" ), "org.hibernate.dialect.DerbyTenSevenDialect" );
        dialects.setEL( new KeyImpl( "DerbyTenSix" ), "org.hibernate.dialect.DerbyTenSixDialect" );
        dialects.setEL( new KeyImpl( "Firebird" ), "org.hibernate.dialect.FirebirdDialect" );
        dialects.setEL( new KeyImpl( "FrontBase" ), "org.hibernate.dialect.FrontBaseDialect" );
        dialects.setEL( new KeyImpl( "H2" ), "org.hibernate.dialect.H2Dialect" );
        dialects.setEL( new KeyImpl( "HANACloudColumnStore" ), "org.hibernate.dialect.HANACloudColumnStoreDialect" );
        dialects.setEL( new KeyImpl( "HANAColumnStore" ), "org.hibernate.dialect.HANAColumnStoreDialect" );
        dialects.setEL( new KeyImpl( "HANARowStore" ), "org.hibernate.dialect.HANARowStoreDialect" );
        dialects.setEL( new KeyImpl( "HSQL" ), "org.hibernate.dialect.HSQLDialect" );
        dialects.setEL( new KeyImpl( "Informix10" ), "org.hibernate.dialect.Informix10Dialect" );
        dialects.setEL( new KeyImpl( "Informix" ), "org.hibernate.dialect.InformixDialect" );
        dialects.setEL( new KeyImpl( "Ingres10" ), "org.hibernate.dialect.Ingres10Dialect" );
        dialects.setEL( new KeyImpl( "Ingres9" ), "org.hibernate.dialect.Ingres9Dialect" );
        dialects.setEL( new KeyImpl( "Ingres" ), "org.hibernate.dialect.IngresDialect" );
        dialects.setEL( new KeyImpl( "Interbase" ), "org.hibernate.dialect.InterbaseDialect" );
        dialects.setEL( new KeyImpl( "JDataStore" ), "org.hibernate.dialect.JDataStoreDialect" );
        dialects.setEL( new KeyImpl( "MariaDB102" ), "org.hibernate.dialect.MariaDB102Dialect" );
        dialects.setEL( new KeyImpl( "MariaDB103" ), "org.hibernate.dialect.MariaDB103Dialect" );
        dialects.setEL( new KeyImpl( "MariaDB10" ), "org.hibernate.dialect.MariaDB10Dialect" );
        dialects.setEL( new KeyImpl( "MariaDB53" ), "org.hibernate.dialect.MariaDB53Dialect" );
        dialects.setEL( new KeyImpl( "MariaDB" ), "org.hibernate.dialect.MariaDBDialect" );
        dialects.setEL( new KeyImpl( "Mckoi" ), "org.hibernate.dialect.MckoiDialect" );
        dialects.setEL( new KeyImpl( "MimerSQL" ), "org.hibernate.dialect.MimerSQLDialect" );
        dialects.setEL( new KeyImpl( "MySQL55" ), "org.hibernate.dialect.MySQL55Dialect" );
        dialects.setEL( new KeyImpl( "MySQL57" ), "org.hibernate.dialect.MySQL57Dialect" );
        dialects.setEL( new KeyImpl( "MySQL57InnoDB" ), "org.hibernate.dialect.MySQL57InnoDBDialect" );
        dialects.setEL( new KeyImpl( "MySQL5" ), "org.hibernate.dialect.MySQL5Dialect" );
        dialects.setEL( new KeyImpl( "MySQL5InnoDB" ), "org.hibernate.dialect.MySQL5InnoDBDialect" );
        dialects.setEL( new KeyImpl( "MySQL8" ), "org.hibernate.dialect.MySQL8Dialect" );
        dialects.setEL( new KeyImpl( "MySQL" ), "org.hibernate.dialect.MySQL8Dialect" );
        dialects.setEL( new KeyImpl( "MySQLInnoDB" ), "org.hibernate.dialect.MySQLInnoDBDialect" );
        dialects.setEL( new KeyImpl( "MySQLMyISAM" ), "org.hibernate.dialect.MySQLMyISAMDialect" );
        dialects.setEL( new KeyImpl( "Oracle10g" ), "org.hibernate.dialect.Oracle10gDialect" );
        dialects.setEL( new KeyImpl( "Oracle12c" ), "org.hibernate.dialect.Oracle12cDialect" );
        dialects.setEL( new KeyImpl( "Oracle8i" ), "org.hibernate.dialect.Oracle8iDialect" );
        dialects.setEL( new KeyImpl( "Oracle9" ), "org.hibernate.dialect.Oracle9Dialect" );
        dialects.setEL( new KeyImpl( "Oracle9i" ), "org.hibernate.dialect.Oracle9iDialect" );
        dialects.setEL( new KeyImpl( "Oracle" ), "org.hibernate.dialect.OracleDialect" );
        dialects.setEL( new KeyImpl( "Pointbase" ), "org.hibernate.dialect.PointbaseDialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL10" ), "org.hibernate.dialect.PostgreSQL10Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL81" ), "org.hibernate.dialect.PostgreSQL81Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL82" ), "org.hibernate.dialect.PostgreSQL82Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL91" ), "org.hibernate.dialect.PostgreSQL91Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL92" ), "org.hibernate.dialect.PostgreSQL92Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL93" ), "org.hibernate.dialect.PostgreSQL93Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL94" ), "org.hibernate.dialect.PostgreSQL94Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL95" ), "org.hibernate.dialect.PostgreSQL95Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL9" ), "org.hibernate.dialect.PostgreSQL9Dialect" );
        dialects.setEL( new KeyImpl( "PostgreSQL" ), "org.hibernate.dialect.PostgreSQLDialect" );
        dialects.setEL( new KeyImpl( "PostgresPlus" ), "org.hibernate.dialect.PostgresPlusDialect" );
        dialects.setEL( new KeyImpl( "Progress" ), "org.hibernate.dialect.ProgressDialect" );
        dialects.setEL( new KeyImpl( "RDMSOS2200" ), "org.hibernate.dialect.RDMSOS2200Dialect" );
        dialects.setEL( new KeyImpl( "SAPDB" ), "org.hibernate.dialect.SAPDBDialect" );
        dialects.setEL( new KeyImpl( "SQLServer2005" ), "org.hibernate.dialect.SQLServer2005Dialect" );
        dialects.setEL( new KeyImpl( "SQLServer2008" ), "org.hibernate.dialect.SQLServer2008Dialect" );
        dialects.setEL( new KeyImpl( "SQLServer2012" ), "org.hibernate.dialect.SQLServer2012Dialect" );
        dialects.setEL( new KeyImpl( "SQLServer" ), "org.hibernate.dialect.SQLServerDialect" );
        dialects.setEL( new KeyImpl( "Sybase11" ), "org.hibernate.dialect.Sybase11Dialect" );
        dialects.setEL( new KeyImpl( "SybaseASE157" ), "org.hibernate.dialect.SybaseASE157Dialect" );
        dialects.setEL( new KeyImpl( "SybaseASE15" ), "org.hibernate.dialect.SybaseASE15Dialect" );
        dialects.setEL( new KeyImpl( "SybaseAnywhere" ), "org.hibernate.dialect.SybaseAnywhereDialect" );
        dialects.setEL( new KeyImpl( "Sybase" ), "org.hibernate.dialect.SybaseDialect" );
        dialects.setEL( new KeyImpl( "Teradata14" ), "org.hibernate.dialect.Teradata14Dialect" );
        dialects.setEL( new KeyImpl( "Teradata" ), "org.hibernate.dialect.TeradataDialect" );
        dialects.setEL( new KeyImpl( "TimesTen" ), "org.hibernate.dialect.TimesTenDialect" );

    }

    /**
     * Get the Hibernate dialect for the given Datasource
     *
     * @param ds
     *           - Datasource object to check dialect on
     *
     * @return the string dialect value, like "org.hibernate.dialect.PostgreSQLDialect"
     */
    public static String getDialect( DataSource ds ) {
        String name = ds.getClassDefinition().getClassName();
        if ( "net.sourceforge.jtds.jdbc.Driver".equalsIgnoreCase( name ) ) {
            String dsn = ds.getConnectionStringTranslated();
            if ( dsn.toLowerCase().indexOf( "sybase" ) != -1 )
                return getDialect( "Sybase" );
            return getDialect( "SQLServer" );
        }
        return getDialect( name );
    }

    /**
     * Return a SQL dialect that match the given Name
     *
     * @param name
     *             - Dialect name like "Oracle" or "MySQL57"
     *
     * @return the full dialect string name, like "org.hibernate.dialect.OracleDialect" or
     *         "org.hibernate.dialect.MySQL57Dialect"
     */
    public static String getDialect( String name ) {
        if ( Util.isEmpty( name ) )
            return null;
        return ( String ) dialects.get( new KeyImpl( name ), null );
    }

    /**
     * Get all configurable dialects
     *
     * @return the configured dialects
     */
    public static Struct getDialects() {
        return dialects;
    }
}
