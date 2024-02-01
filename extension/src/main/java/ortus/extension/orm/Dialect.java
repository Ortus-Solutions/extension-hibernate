package ortus.extension.orm;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
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
			BundleClassLoader	bcl	= ( BundleClassLoader ) org.hibernate.dialect.SybaseDialect.class.getClassLoader();
			Bundle				b	= bcl.getBundle();

			// List all XML files in the OSGI-INF directory and below
			Enumeration<URL>	e	= b.findEntries( "org/hibernate/dialect", "*.class", true );
			String				path;
			while ( e.hasMoreElements() ) {
				try {
					path = e.nextElement().getPath();
					if ( path.startsWith( "/" ) )
						path = path.substring( 1 );
					else if ( path.startsWith( "\\" ) )
						path = path.substring( 1 );
					if ( path.endsWith( ".class" ) )
						path = path.substring( 0, path.length() - 6 );
					path	= path.replace( '/', '.' );
					path	= path.replace( '\\', '.' );
					String		name;
					Class<?>	clazz	= bcl.loadClass( path );
					if ( org.hibernate.dialect.Dialect.class.isAssignableFrom( clazz )
					    && !Modifier.isAbstract( clazz.getModifiers() ) ) {
						dialects.setEL( CommonUtil.createKey( path ), path );
						dialects.setEL( CommonUtil.createKey( CommonUtil.last( path, "." ) ), path );
						name = CommonUtil.last( path, "." );
						dialects.setEL( CommonUtil.createKey( name ), path );
						if ( name.endsWith( "Dialect" ) ) {
							name = name.substring( 0, name.length() - 7 );
							dialects.setEL( CommonUtil.createKey( name ), path );
						}

					}
				} catch ( Exception exx ) {
					exx.printStackTrace();
				}
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}

		dialects.setEL( CommonUtil.createKey( "CUBRID" ), "org.hibernate.dialect.CUBRIDDialect" );
		dialects.setEL( CommonUtil.createKey( "Cache71" ), "org.hibernate.dialect.Cache71Dialect" );
		dialects.setEL( CommonUtil.createKey( "CockroachDB192" ), "org.hibernate.dialect.CockroachDB192Dialect" );
		dialects.setEL( CommonUtil.createKey( "CockroachDB201" ), "org.hibernate.dialect.CockroachDB201Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB2390" ), "org.hibernate.dialect.DB2390Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB2390V8" ), "org.hibernate.dialect.DB2390V8Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB2400" ), "org.hibernate.dialect.DB2400Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB2400V7R3" ), "org.hibernate.dialect.DB2400V7R3Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB297" ), "org.hibernate.dialect.DB297Dialect" );
		dialects.setEL( CommonUtil.createKey( "DB2" ), "org.hibernate.dialect.DB2Dialect" );
		dialects.setEL( CommonUtil.createKey( "DataDirectOracle9" ), "org.hibernate.dialect.DataDirectOracle9Dialect" );
		dialects.setEL( CommonUtil.createKey( "Derby" ), "org.hibernate.dialect.DerbyDialect" );
		dialects.setEL( CommonUtil.createKey( "DerbyTenFive" ), "org.hibernate.dialect.DerbyTenFiveDialect" );
		dialects.setEL( CommonUtil.createKey( "DerbyTenSeven" ), "org.hibernate.dialect.DerbyTenSevenDialect" );
		dialects.setEL( CommonUtil.createKey( "DerbyTenSix" ), "org.hibernate.dialect.DerbyTenSixDialect" );
		dialects.setEL( CommonUtil.createKey( "Firebird" ), "org.hibernate.dialect.FirebirdDialect" );
		dialects.setEL( CommonUtil.createKey( "FrontBase" ), "org.hibernate.dialect.FrontBaseDialect" );
		dialects.setEL( CommonUtil.createKey( "H2" ), "org.hibernate.dialect.H2Dialect" );
		dialects.setEL( CommonUtil.createKey( "HANACloudColumnStore" ), "org.hibernate.dialect.HANACloudColumnStoreDialect" );
		dialects.setEL( CommonUtil.createKey( "HANAColumnStore" ), "org.hibernate.dialect.HANAColumnStoreDialect" );
		dialects.setEL( CommonUtil.createKey( "HANARowStore" ), "org.hibernate.dialect.HANARowStoreDialect" );
		dialects.setEL( CommonUtil.createKey( "HSQL" ), "org.hibernate.dialect.HSQLDialect" );
		dialects.setEL( CommonUtil.createKey( "Informix10" ), "org.hibernate.dialect.Informix10Dialect" );
		dialects.setEL( CommonUtil.createKey( "Informix" ), "org.hibernate.dialect.InformixDialect" );
		dialects.setEL( CommonUtil.createKey( "Ingres10" ), "org.hibernate.dialect.Ingres10Dialect" );
		dialects.setEL( CommonUtil.createKey( "Ingres9" ), "org.hibernate.dialect.Ingres9Dialect" );
		dialects.setEL( CommonUtil.createKey( "Ingres" ), "org.hibernate.dialect.IngresDialect" );
		dialects.setEL( CommonUtil.createKey( "Interbase" ), "org.hibernate.dialect.InterbaseDialect" );
		dialects.setEL( CommonUtil.createKey( "JDataStore" ), "org.hibernate.dialect.JDataStoreDialect" );
		dialects.setEL( CommonUtil.createKey( "MariaDB102" ), "org.hibernate.dialect.MariaDB102Dialect" );
		dialects.setEL( CommonUtil.createKey( "MariaDB103" ), "org.hibernate.dialect.MariaDB103Dialect" );
		dialects.setEL( CommonUtil.createKey( "MariaDB10" ), "org.hibernate.dialect.MariaDB10Dialect" );
		dialects.setEL( CommonUtil.createKey( "MariaDB53" ), "org.hibernate.dialect.MariaDB53Dialect" );
		dialects.setEL( CommonUtil.createKey( "MariaDB" ), "org.hibernate.dialect.MariaDBDialect" );
		dialects.setEL( CommonUtil.createKey( "Mckoi" ), "org.hibernate.dialect.MckoiDialect" );
		dialects.setEL( CommonUtil.createKey( "MimerSQL" ), "org.hibernate.dialect.MimerSQLDialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL55" ), "org.hibernate.dialect.MySQL55Dialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL57" ), "org.hibernate.dialect.MySQL57Dialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL57InnoDB" ), "org.hibernate.dialect.MySQL57InnoDBDialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL5" ), "org.hibernate.dialect.MySQL5Dialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL5InnoDB" ), "org.hibernate.dialect.MySQL5InnoDBDialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL8" ), "org.hibernate.dialect.MySQL8Dialect" );
		dialects.setEL( CommonUtil.createKey( "MySQL" ), "org.hibernate.dialect.MySQL8Dialect" );
		dialects.setEL( CommonUtil.createKey( "MySQLInnoDB" ), "org.hibernate.dialect.MySQLInnoDBDialect" );
		dialects.setEL( CommonUtil.createKey( "MySQLMyISAM" ), "org.hibernate.dialect.MySQLMyISAMDialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle10g" ), "org.hibernate.dialect.Oracle10gDialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle12c" ), "org.hibernate.dialect.Oracle12cDialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle8i" ), "org.hibernate.dialect.Oracle8iDialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle9" ), "org.hibernate.dialect.Oracle9Dialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle9i" ), "org.hibernate.dialect.Oracle9iDialect" );
		dialects.setEL( CommonUtil.createKey( "Oracle" ), "org.hibernate.dialect.OracleDialect" );
		dialects.setEL( CommonUtil.createKey( "Pointbase" ), "org.hibernate.dialect.PointbaseDialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL10" ), "org.hibernate.dialect.PostgreSQL10Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL81" ), "org.hibernate.dialect.PostgreSQL81Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL82" ), "org.hibernate.dialect.PostgreSQL82Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL91" ), "org.hibernate.dialect.PostgreSQL91Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL92" ), "org.hibernate.dialect.PostgreSQL92Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL93" ), "org.hibernate.dialect.PostgreSQL93Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL94" ), "org.hibernate.dialect.PostgreSQL94Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL95" ), "org.hibernate.dialect.PostgreSQL95Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL9" ), "org.hibernate.dialect.PostgreSQL9Dialect" );
		dialects.setEL( CommonUtil.createKey( "PostgreSQL" ), "org.hibernate.dialect.PostgreSQLDialect" );
		dialects.setEL( CommonUtil.createKey( "PostgresPlus" ), "org.hibernate.dialect.PostgresPlusDialect" );
		dialects.setEL( CommonUtil.createKey( "Progress" ), "org.hibernate.dialect.ProgressDialect" );
		dialects.setEL( CommonUtil.createKey( "RDMSOS2200" ), "org.hibernate.dialect.RDMSOS2200Dialect" );
		dialects.setEL( CommonUtil.createKey( "SAPDB" ), "org.hibernate.dialect.SAPDBDialect" );
		dialects.setEL( CommonUtil.createKey( "SQLServer2005" ), "org.hibernate.dialect.SQLServer2005Dialect" );
		dialects.setEL( CommonUtil.createKey( "SQLServer2008" ), "org.hibernate.dialect.SQLServer2008Dialect" );
		dialects.setEL( CommonUtil.createKey( "SQLServer2012" ), "org.hibernate.dialect.SQLServer2012Dialect" );
		dialects.setEL( CommonUtil.createKey( "SQLServer" ), "org.hibernate.dialect.SQLServerDialect" );
		dialects.setEL( CommonUtil.createKey( "Sybase11" ), "org.hibernate.dialect.Sybase11Dialect" );
		dialects.setEL( CommonUtil.createKey( "SybaseASE157" ), "org.hibernate.dialect.SybaseASE157Dialect" );
		dialects.setEL( CommonUtil.createKey( "SybaseASE15" ), "org.hibernate.dialect.SybaseASE15Dialect" );
		dialects.setEL( CommonUtil.createKey( "SybaseAnywhere" ), "org.hibernate.dialect.SybaseAnywhereDialect" );
		dialects.setEL( CommonUtil.createKey( "Sybase" ), "org.hibernate.dialect.SybaseDialect" );
		dialects.setEL( CommonUtil.createKey( "Teradata14" ), "org.hibernate.dialect.Teradata14Dialect" );
		dialects.setEL( CommonUtil.createKey( "Teradata" ), "org.hibernate.dialect.TeradataDialect" );
		dialects.setEL( CommonUtil.createKey( "TimesTen" ), "org.hibernate.dialect.TimesTenDialect" );

	}

	private Dialect() {
		throw new IllegalStateException( "Utility class; please don't instantiate!" );
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
		return ( String ) dialects.get( CommonUtil.createKey( name ), null );
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
