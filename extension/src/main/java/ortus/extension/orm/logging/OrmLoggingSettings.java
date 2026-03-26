package ortus.extension.orm.logging;

import java.lang.reflect.Method;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.type.Struct;

import ortus.extension.orm.util.CommonUtil;
import lucee.runtime.type.Collection.Key;

/**
 * Reads ORM logging settings from ormSettings in Application.cfc.
 *
 * Since Lucee's ORMConfiguration interface only exposes {@code logSQL()}, we read
 * the additional settings ({@code logParams}, {@code logCache}, {@code logLevel})
 * directly from the Application.cfc component's {@code this.ormSettings} struct.
 */
public class OrmLoggingSettings {

	private static final Key	KEY_ORM_SETTINGS	= CommonUtil.createKey( "ormSettings" );
	private static final Key	KEY_LOG_SQL			= CommonUtil.createKey( "logSQL" );
	private static final Key	KEY_LOG_PARAMS		= CommonUtil.createKey( "logParams" );
	private static final Key	KEY_LOG_CACHE		= CommonUtil.createKey( "logCache" );
	private static final Key	KEY_LOG_LEVEL		= CommonUtil.createKey( "logLevel" );

	public final boolean		logSQL;
	public final boolean		logParams;
	public final boolean		logCache;
	public final String			logLevel;

	private OrmLoggingSettings( boolean logSQL, boolean logParams, boolean logCache, String logLevel ) {
		this.logSQL		= logSQL;
		this.logParams	= logParams;
		this.logCache	= logCache;
		this.logLevel	= logLevel;
	}

	/**
	 * Read ORM logging settings from the current application context.
	 *
	 * Reads logSQL from the ORMConfiguration interface, and the extension-specific
	 * settings (logParams, logCache, logLevel) from the raw ormSettings struct
	 * on the Application.cfc component.
	 */
	public static OrmLoggingSettings load( PageContext pc, ORMConfiguration ormConf ) {
		boolean	logSQL		= ormConf.logSQL();
		boolean	logParams	= false;
		boolean	logCache	= false;
		String	logLevel	= null;

		Struct	ormSettings	= getOrmSettingsStruct( pc );
		if ( ormSettings != null ) {
			logSQL		= CommonUtil.toBooleanValue( ormSettings.get( KEY_LOG_SQL, logSQL ), logSQL );
			logParams	= CommonUtil.toBooleanValue( ormSettings.get( KEY_LOG_PARAMS, false ), false );
			logCache	= CommonUtil.toBooleanValue( ormSettings.get( KEY_LOG_CACHE, false ), false );
			logLevel	= CommonUtil.toString( ormSettings.get( KEY_LOG_LEVEL, null ), null );
		}

		return new OrmLoggingSettings( logSQL, logParams, logCache, logLevel );
	}

	/**
	 * Try to read the raw ormSettings struct from the Application.cfc component.
	 *
	 * Uses reflection to access ModernApplicationContext.getComponent(), then reads
	 * this.ormSettings from the component's scope. Returns null if anything fails
	 * (e.g. ClassicApplicationContext, older Lucee version, no ormSettings set).
	 */
	private static Struct getOrmSettingsStruct( PageContext pc ) {
		try {
			ApplicationContext	ac				= pc.getApplicationContext();
			Method				getComponent	= ac.getClass().getMethod( "getComponent" );
			Component			appCFC			= ( Component ) getComponent.invoke( ac );
			if ( appCFC == null )
				return null;
			Object settings = appCFC.get( KEY_ORM_SETTINGS, null );
			if ( settings instanceof Struct )
				return ( Struct ) settings;
		} catch ( Exception e ) {
			// ClassicApplicationContext or older Lucee version - fall back to defaults
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format( "OrmLoggingSettings[logSQL=%s, logParams=%s, logCache=%s, logLevel=%s]",
		    logSQL, logParams, logCache, logLevel );
	}
}
