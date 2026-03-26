package ortus.extension.orm.logging;

import java.text.MessageFormat;

import org.jboss.logging.Logger;

import lucee.commons.io.log.Log;

/**
 * JBoss Logging Logger that delegates directly to Lucee's native Log interface.
 *
 * Uses {@link LoggerLevelManager} for isEnabled() checks (the muzzle) with standard severity ordering,
 * and passes Lucee's level constants to log.log() so Log4j2 maps them correctly.
 */
public class LuceeJBossLogger extends Logger {

	private static final long	serialVersionUID	= 1L;

	private final String		name;

	protected LuceeJBossLogger( String name ) {
		super( name );
		this.name = name;
	}

	private String getSource() {
		int dot = name.lastIndexOf( '.' );
		return dot >= 0 ? name.substring( dot + 1 ) : name;
	}

	private Log getLuceeLog() {
		return LuceeJBossLoggerProvider.getLuceeLog();
	}

	/**
	 * Map JBoss Level to standard severity for isEnabled() checks.
	 */
	private static int toStandardSeverity( Level level ) {
		if ( level != null )
			switch ( level ) {
				case TRACE :
					return Severity.TRACE;
				case DEBUG :
					return Severity.DEBUG;
				case INFO :
					return Severity.INFO;
				case WARN :
					return Severity.WARN;
				case ERROR :
					return Severity.ERROR;
				case FATAL :
					return Severity.FATAL;
			}
		return Severity.ERROR;
	}

	/**
	 * Map JBoss Level to Lucee constant for log.log() calls.
	 */
	private static int toLuceeLevel( Level level ) {
		if ( level != null )
			switch ( level ) {
				case TRACE :
					return Log.LEVEL_TRACE;
				case DEBUG :
					return Log.LEVEL_DEBUG;
				case INFO :
					return Log.LEVEL_INFO;
				case WARN :
					return Log.LEVEL_WARN;
				case ERROR :
					return Log.LEVEL_ERROR;
				case FATAL :
					return Log.LEVEL_FATAL;
			}
		return Log.LEVEL_ERROR;
	}

	@Override
	public boolean isEnabled( Level level ) {
		if ( getLuceeLog() == null )
			return false;
		return LoggerLevelManager.isEnabled( name, toStandardSeverity( level ) );
	}

	@Override
	protected void doLog( Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown ) {
		if ( !isEnabled( level ) )
			return;
		String	text	= parameters == null || parameters.length == 0
		    ? String.valueOf( message )
		    : MessageFormat.format( String.valueOf( message ), parameters );
		Log		log		= getLuceeLog();
		if ( log == null )
			return;
		int luceeLevel = toLuceeLevel( level );
		if ( thrown != null )
			log.log( luceeLevel, getSource(), text, thrown );
		else
			log.log( luceeLevel, getSource(), text );
	}

	@Override
	protected void doLogf( Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown ) {
		if ( !isEnabled( level ) )
			return;
		String	text	= parameters == null ? String.format( format ) : String.format( format, parameters );
		Log		log		= getLuceeLog();
		if ( log == null )
			return;
		int luceeLevel = toLuceeLevel( level );
		if ( thrown != null )
			log.log( luceeLevel, getSource(), text, thrown );
		else
			log.log( luceeLevel, getSource(), text );
	}
}
