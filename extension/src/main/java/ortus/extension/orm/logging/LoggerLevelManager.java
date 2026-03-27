package ortus.extension.orm.logging;

import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.log.Log;

/**
 * Configures ORM logging based on ormSettings.
 *
 * ormSettings control which Hibernate logging categories are enabled (the muzzle).
 * The Lucee orm log level controls what actually gets written (the server threshold).
 * Both filters must pass for a message to appear in orm.log.
 *
 * Per-category severity overrides use standard ordering (TRACE < DEBUG < INFO < WARN < ERROR)
 * via {@link Severity}, not Lucee's non-standard constants.
 */
public class LoggerLevelManager {

	/**
	 * Hibernate logging categories.
	 */
	private static final String								CAT_HIBERNATE		= "org.hibernate";
	private static final String								CAT_SQL				= "org.hibernate.SQL";
	private static final String								CAT_PARAMS			= "org.hibernate.type.descriptor.sql";
	private static final String								CAT_CACHE			= "org.hibernate.cache";
	private static final String								CAT_EHCACHE			= "net.sf.ehcache";
	private static final String								CAT_EXTENSION		= "ortus.extension.orm";

	/**
	 * Per-category severity thresholds using standard ordering.
	 * A message is enabled if its severity >= the category's threshold.
	 */
	private static final ConcurrentHashMap<String, Integer>	thresholds			= new ConcurrentHashMap<>();

	/**
	 * Default threshold before configure() is called.
	 */
	private static volatile int								defaultThreshold	= Severity.ERROR;

	/**
	 * Check if a message at the given standard severity is enabled for the named category.
	 * Used by both JBoss and SLF4J bridges as the performance gate.
	 */
	public static boolean isEnabled( String name, int standardSeverity ) {
		Integer threshold = getThreshold( name );
		return standardSeverity >= threshold;
	}

	/**
	 * Get the threshold for a logger, checking parent categories.
	 * E.g. for "org.hibernate.type.BasicTypeRegistry":
	 * checks "org.hibernate.type.BasicTypeRegistry"
	 * then "org.hibernate.type"
	 * then "org.hibernate"
	 * then falls back to defaultThreshold.
	 */
	private static int getThreshold( String name ) {
		// Exact match
		Integer t = thresholds.get( name );
		if ( t != null )
			return t;
		// Walk up the hierarchy
		int dot = name.lastIndexOf( '.' );
		while ( dot > 0 ) {
			String parent = name.substring( 0, dot );
			t = thresholds.get( parent );
			if ( t != null )
				return t;
			dot = parent.lastIndexOf( '.' );
		}
		return defaultThreshold;
	}

	/**
	 * Configure all logging based on ormSettings.
	 *
	 * @param luceeLog  The Lucee Log instance (orm log) to route all output to.
	 * @param logSQL    Enable SQL statement logging (includes DDL during schema export).
	 * @param logParams Enable parameter binding logging.
	 * @param logCache  Enable cache activity logging.
	 * @param logLevel  Overall log level. One of: trace, debug, info, warn, error. Defaults to error.
	 */
	public static void configure( Log luceeLog, boolean logSQL, boolean logParams, boolean logCache,
	    String logLevel ) {

		int level = Severity.parse( logLevel );
		defaultThreshold = level;

		// Set category thresholds using standard severity
		thresholds.put( CAT_EXTENSION, level );
		thresholds.put( CAT_HIBERNATE, level );
		thresholds.put( CAT_SQL, logSQL ? Severity.DEBUG : level );
		thresholds.put( CAT_PARAMS, logParams ? Severity.TRACE : level );
		thresholds.put( CAT_CACHE, logCache ? Severity.DEBUG : level );
		thresholds.put( CAT_EHCACHE, logCache ? Severity.DEBUG : level );

		// Push Lucee Log instance to both bridges
		LuceeJBossLoggerProvider.setLuceeLog( luceeLog );

		LuceeBackedLoggerFactory factory = LuceeSLF4JServiceProvider.getStaticLoggerFactory();
		if ( factory != null )
			factory.setLuceeLog( luceeLog );
	}
}
