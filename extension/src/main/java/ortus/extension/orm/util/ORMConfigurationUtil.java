package ortus.extension.orm.util;

import java.lang.reflect.Method;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMConfiguration;

// FUTURE update ORMConfiguration interface
public class ORMConfigurationUtil {

	private static Method	getDbCreate;
	private static Method	getCatalog;
	private static Method	getSchema;
	private static Method	getSqlScript;
	private static Method	getDialect;

	private ORMConfigurationUtil() {
		throw new IllegalStateException( "Utility class; please don't instantiate!" );
	}

	public static int getDbCreate( ORMConfiguration conf, String datasourceName ) throws PageException {
		if ( !Util.isEmpty( datasourceName )
		    && ! ( datasourceName = datasourceName.trim().toLowerCase() ).equals( "__default__" ) ) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			// Lucee >= 5.3.2.16
			try {
				if ( getDbCreate == null || getDbCreate.getDeclaringClass() != conf.getClass() ) {
					getDbCreate = conf.getClass().getMethod( "getDbCreate", String.class );
				}
				return eng.getCastUtil().toIntValue( getDbCreate.invoke( conf, datasourceName ) );
			} catch ( NoSuchMethodException e ) {
				// older Lucee version
			} catch ( Exception e ) {
				throw eng.getCastUtil().toPageException( e );
			}
		}
		return conf.getDbCreate();
	}

	public static String getCatalog( ORMConfiguration conf, String datasourceName ) throws PageException {
		if ( !Util.isEmpty( datasourceName )
		    && ! ( datasourceName = datasourceName.trim().toLowerCase() ).equals( "__default__" ) ) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			// Lucee >= 5.3.2.16
			try {
				if ( getCatalog == null || getCatalog.getDeclaringClass() != conf.getClass() ) {
					getCatalog = conf.getClass().getMethod( "getCatalog", String.class );
				}
				return eng.getCastUtil().toString( getCatalog.invoke( conf, datasourceName ) );
			} catch ( NoSuchMethodException e ) {
				// older Lucee version
			} catch ( Exception e ) {
				throw eng.getCastUtil().toPageException( e );
			}
		}
		return conf.getCatalog();
	}

	public static String getSchema( ORMConfiguration conf, String datasourceName ) throws PageException {
		if ( !Util.isEmpty( datasourceName )
		    && ! ( datasourceName = datasourceName.trim().toLowerCase() ).equals( "__default__" ) ) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			// Lucee >= 5.3.2.16
			try {
				if ( getSchema == null || getSchema.getDeclaringClass() != conf.getClass() ) {
					getSchema = conf.getClass().getMethod( "getSchema", String.class );
				}
				return eng.getCastUtil().toString( getSchema.invoke( conf, datasourceName ) );
			} catch ( NoSuchMethodException e ) {
				// older Lucee version
			} catch ( Exception e ) {
				throw eng.getCastUtil().toPageException( e );
			}
		}
		return conf.getSchema();
	}

	public static String getDialect( ORMConfiguration conf, String datasourceName ) throws PageException {
		if ( !Util.isEmpty( datasourceName )
		    && ! ( datasourceName = datasourceName.trim().toLowerCase() ).equals( "__default__" ) ) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			// Lucee >= 5.3.2.16
			try {
				if ( getDialect == null || getDialect.getDeclaringClass() != conf.getClass() ) {
					getDialect = conf.getClass().getMethod( "getDialect", String.class );
				}
				return eng.getCastUtil().toString( getDialect.invoke( conf, datasourceName ) );
			} catch ( NoSuchMethodException e ) {
				// older Lucee version
			} catch ( Exception e ) {
				throw eng.getCastUtil().toPageException( e );
			}
		}
		return conf.getDialect();
	}

	public static Resource getSqlScript( ORMConfiguration conf, String datasourceName ) throws PageException {
		if ( !Util.isEmpty( datasourceName )
		    && ! ( datasourceName = datasourceName.trim().toLowerCase() ).equals( "__default__" ) ) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			// Lucee >= 5.3.2.16
			try {
				if ( getSqlScript == null || getSqlScript.getDeclaringClass() != conf.getClass() ) {
					getSqlScript = conf.getClass().getMethod( "getSqlScript", String.class );
				}
				return ( Resource ) getSqlScript.invoke( conf, datasourceName );
			} catch ( NoSuchMethodException e ) {
				// older Lucee version
			} catch ( Exception e ) {
				throw eng.getCastUtil().toPageException( e );
			}
		}
		return conf.getSqlScript();
	}

	public static void dump( ORMConfiguration ormConf, String name ) throws PageException {

		System.err.println( "---------------- " + name + " ---------------------" );
		System.err.println( "catalog: " + getCatalog( ormConf, name ) );
		System.err.println( "dialect: " + getDialect( ormConf, name ) );
		System.err.println( "schema: " + getSchema( ormConf, name ) );
		System.err.println( "DbCreate: " + getDbCreate( ormConf, name ) );
		System.err.println( "SqlScript: " + getSqlScript( ormConf, name ) );

		name = "susi";

		System.err.println( "---------------- " + name + " ---------------------" );
		System.err.println( "catalog: " + getCatalog( ormConf, name ) );
		System.err.println( "dialect: " + getDialect( ormConf, name ) );
		System.err.println( "schema: " + getSchema( ormConf, name ) );
		System.err.println( "DbCreate: " + getDbCreate( ormConf, name ) );
		System.err.println( "SqlScript: " + getSqlScript( ormConf, name ) );

	}

}
