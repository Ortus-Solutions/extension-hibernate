package ortus.extension.orm;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * Configure Hibernate logging with a bit of Logback hackery.
 * <p>
 * Note that we SHOULD not be using the LogBack API, since it renders SLF4J nearly useless... however, since the SLF4J interfaces do not define a
 * method for setting a log level on a logger from outside, we are forced to talk to LogBack classes directly. This is the only means by which we can
 * dynamically, at runtime, at will, modify the log level(s).
 * <p>
 * Many thanks to
 * <li>https://stackoverflow.com/questions/65057439/is-it-possible-to-setup-logback-configuration-programmatically
 * <li>https://logback.qos.ch/apidocs/ch/qos/logback/classic/spi/Configurator.html
 * <li>https://logback.qos.ch/manual/configuration.html
 */
public class LoggingConfigurator {

	/**
	 * The logger factory (slf4j) or logger context (logback) that we'll be configurating.
	 */
	private LoggerContext	loggerContext;

	/**
	 * Enable logging of all SQL queries and parameters.
	 */
	private Boolean			enableSQLLogging;

	/**
	 * The default level that Hibernate classes should log at.
	 */
	private Level			defaultLogLevel;

	public LoggingConfigurator( Level defaultLogLevel, Boolean enableSQLLogging ) {
		ch.qos.logback.classic.Logger hibernateLogger = ( ch.qos.logback.classic.Logger ) LoggerFactory
		    .getLogger( "org.hibernate" );

		this.loggerContext		= hibernateLogger.getLoggerContext();
		this.enableSQLLogging	= enableSQLLogging;
		this.defaultLogLevel	= defaultLogLevel;
	}

	public void configure() {

		/**
		 * @TODO: Come up with a sensible way to enable additional logging from a CFML app... not just for development.
		 */
		Logger extensionLogger = loggerContext.getLogger( "ortus.extension.orm" );
		extensionLogger.setLevel( defaultLogLevel );

		Logger hibernateLogger = loggerContext.getLogger( "org.hibernate" );
		hibernateLogger.setLevel( defaultLogLevel );

		// @TODO: In Hibernate 6, this will be `org.hibernate.orm.jdbc.bind`
		Logger sqlLogger = loggerContext.getLogger( "org.hibernate.type.descriptor.sql" );
		sqlLogger.setLevel( getSQLLogLevel() );

		Logger cacheLogger = loggerContext.getLogger( "org.hibernate.cache" );
		cacheLogger.setLevel( defaultLogLevel );

		// for some reason, the net.sf.ehcache logs do not go through org.hibernate/org.hibernate.cache.
		Logger ehcacheLogger = loggerContext.getLogger( "net.sf.ehcache" );
		ehcacheLogger.setLevel( defaultLogLevel );

	}

	public Level getSQLLogLevel() {
		return Boolean.TRUE.equals( this.enableSQLLogging ) ? Level.DEBUG : Level.ERROR;
	}

	public Level getDefaultLogLevel() {
		return defaultLogLevel;
	}
}