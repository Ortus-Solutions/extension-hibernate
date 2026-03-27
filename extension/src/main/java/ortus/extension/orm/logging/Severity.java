package ortus.extension.orm.logging;

import lucee.commons.io.log.Log;

/**
 * Standard log severity ordering for use in the logging bridge.
 *
 * Lucee's Log constants have a non-standard ordering (TRACE=0, INFO=1, DEBUG=2, WARN=3, ERROR=4)
 * where INFO < DEBUG. This class provides a standard ordering (TRACE < DEBUG < INFO < WARN < ERROR)
 * for isEnabled() comparisons in the bridge, while preserving the Lucee constants for log.log() calls
 * so Log4j2 maps them correctly.
 *
 * Standard ordering: TRACE(0) < DEBUG(1) < INFO(2) < WARN(3) < ERROR(4) < FATAL(5)
 */
public final class Severity {

	public static final int	TRACE	= 0;
	public static final int	DEBUG	= 1;
	public static final int	INFO	= 2;
	public static final int	WARN	= 3;
	public static final int	ERROR	= 4;
	public static final int	FATAL	= 5;

	/**
	 * Convert a Lucee Log level constant to standard severity.
	 */
	public static int fromLucee( int luceeLevel ) {
		switch ( luceeLevel ) {
			case Log.LEVEL_TRACE :
				return TRACE;
			case Log.LEVEL_DEBUG :
				return DEBUG;
			case Log.LEVEL_INFO :
				return INFO;
			case Log.LEVEL_WARN :
				return WARN;
			case Log.LEVEL_ERROR :
				return ERROR;
			case Log.LEVEL_FATAL :
				return FATAL;
			default :
				return ERROR;
		}
	}

	/**
	 * Parse a string level name to standard severity.
	 * Defaults to ERROR if null or unrecognized.
	 */
	public static int parse( String level ) {
		if ( level == null )
			return ERROR;
		switch ( level.trim().toLowerCase() ) {
			case "trace" :
				return TRACE;
			case "debug" :
				return DEBUG;
			case "info" :
				return INFO;
			case "warn" :
			case "warning" :
				return WARN;
			case "error" :
				return ERROR;
			case "fatal" :
				return FATAL;
			default :
				return ERROR;
		}
	}

	private Severity() {
		throw new IllegalStateException( "Utility class" );
	}
}
