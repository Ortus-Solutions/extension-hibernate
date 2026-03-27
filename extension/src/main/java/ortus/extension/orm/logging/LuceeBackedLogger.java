package ortus.extension.orm.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import lucee.commons.io.log.Log;

/**
 * SLF4J Logger that delegates to Lucee's native Log interface.
 *
 * Uses {@link LoggerLevelManager} for isEnabled() checks (the muzzle) with standard severity ordering,
 * and passes Lucee's level constants to log.log() so Log4j2 maps them correctly.
 */
public class LuceeBackedLogger implements Logger {

	private final String	name;
	private Log				luceeLog;

	public LuceeBackedLogger( String name ) {
		this.name = name;
	}

	public void setLuceeLog( Log log ) {
		this.luceeLog = log;
	}

	private String getSource() {
		int dot = name.lastIndexOf( '.' );
		return dot >= 0 ? name.substring( dot + 1 ) : name;
	}

	private boolean isEnabled( int standardSeverity ) {
		return luceeLog != null && LoggerLevelManager.isEnabled( name, standardSeverity );
	}

	private void doLog( int luceeLevel, int standardSeverity, String msg ) {
		if ( !isEnabled( standardSeverity ) )
			return;
		luceeLog.log( luceeLevel, getSource(), msg );
	}

	private void doLog( int luceeLevel, int standardSeverity, String msg, Throwable t ) {
		if ( !isEnabled( standardSeverity ) )
			return;
		luceeLog.log( luceeLevel, getSource(), msg, t );
	}

	private String format( String fmt, Object arg ) {
		return MessageFormatter.format( fmt, arg ).getMessage();
	}

	private String format( String fmt, Object arg1, Object arg2 ) {
		return MessageFormatter.format( fmt, arg1, arg2 ).getMessage();
	}

	private String format( String fmt, Object... args ) {
		return MessageFormatter.arrayFormat( fmt, args ).getMessage();
	}

	// ---------------------------------------------------------------
	// Name
	// ---------------------------------------------------------------

	@Override
	public String getName() {
		return name;
	}

	// ---------------------------------------------------------------
	// TRACE
	// ---------------------------------------------------------------

	@Override
	public boolean isTraceEnabled() {
		return isEnabled( Severity.TRACE );
	}

	@Override
	public void trace( String msg ) {
		doLog( Log.LEVEL_TRACE, Severity.TRACE, msg );
	}

	@Override
	public void trace( String fmt, Object arg ) {
		if ( isTraceEnabled() )
			doLog( Log.LEVEL_TRACE, Severity.TRACE, format( fmt, arg ) );
	}

	@Override
	public void trace( String fmt, Object arg1, Object arg2 ) {
		if ( isTraceEnabled() )
			doLog( Log.LEVEL_TRACE, Severity.TRACE, format( fmt, arg1, arg2 ) );
	}

	@Override
	public void trace( String fmt, Object... args ) {
		if ( isTraceEnabled() )
			doLog( Log.LEVEL_TRACE, Severity.TRACE, format( fmt, args ) );
	}

	@Override
	public void trace( String msg, Throwable t ) {
		doLog( Log.LEVEL_TRACE, Severity.TRACE, msg, t );
	}

	@Override
	public boolean isTraceEnabled( Marker marker ) {
		return isTraceEnabled();
	}

	@Override
	public void trace( Marker marker, String msg ) {
		trace( msg );
	}

	@Override
	public void trace( Marker marker, String fmt, Object arg ) {
		trace( fmt, arg );
	}

	@Override
	public void trace( Marker marker, String fmt, Object arg1, Object arg2 ) {
		trace( fmt, arg1, arg2 );
	}

	@Override
	public void trace( Marker marker, String fmt, Object... args ) {
		trace( fmt, args );
	}

	@Override
	public void trace( Marker marker, String msg, Throwable t ) {
		trace( msg, t );
	}

	// ---------------------------------------------------------------
	// DEBUG
	// ---------------------------------------------------------------

	@Override
	public boolean isDebugEnabled() {
		return isEnabled( Severity.DEBUG );
	}

	@Override
	public void debug( String msg ) {
		doLog( Log.LEVEL_DEBUG, Severity.DEBUG, msg );
	}

	@Override
	public void debug( String fmt, Object arg ) {
		if ( isDebugEnabled() )
			doLog( Log.LEVEL_DEBUG, Severity.DEBUG, format( fmt, arg ) );
	}

	@Override
	public void debug( String fmt, Object arg1, Object arg2 ) {
		if ( isDebugEnabled() )
			doLog( Log.LEVEL_DEBUG, Severity.DEBUG, format( fmt, arg1, arg2 ) );
	}

	@Override
	public void debug( String fmt, Object... args ) {
		if ( isDebugEnabled() )
			doLog( Log.LEVEL_DEBUG, Severity.DEBUG, format( fmt, args ) );
	}

	@Override
	public void debug( String msg, Throwable t ) {
		doLog( Log.LEVEL_DEBUG, Severity.DEBUG, msg, t );
	}

	@Override
	public boolean isDebugEnabled( Marker marker ) {
		return isDebugEnabled();
	}

	@Override
	public void debug( Marker marker, String msg ) {
		debug( msg );
	}

	@Override
	public void debug( Marker marker, String fmt, Object arg ) {
		debug( fmt, arg );
	}

	@Override
	public void debug( Marker marker, String fmt, Object arg1, Object arg2 ) {
		debug( fmt, arg1, arg2 );
	}

	@Override
	public void debug( Marker marker, String fmt, Object... args ) {
		debug( fmt, args );
	}

	@Override
	public void debug( Marker marker, String msg, Throwable t ) {
		debug( msg, t );
	}

	// ---------------------------------------------------------------
	// INFO
	// ---------------------------------------------------------------

	@Override
	public boolean isInfoEnabled() {
		return isEnabled( Severity.INFO );
	}

	@Override
	public void info( String msg ) {
		doLog( Log.LEVEL_INFO, Severity.INFO, msg );
	}

	@Override
	public void info( String fmt, Object arg ) {
		if ( isInfoEnabled() )
			doLog( Log.LEVEL_INFO, Severity.INFO, format( fmt, arg ) );
	}

	@Override
	public void info( String fmt, Object arg1, Object arg2 ) {
		if ( isInfoEnabled() )
			doLog( Log.LEVEL_INFO, Severity.INFO, format( fmt, arg1, arg2 ) );
	}

	@Override
	public void info( String fmt, Object... args ) {
		if ( isInfoEnabled() )
			doLog( Log.LEVEL_INFO, Severity.INFO, format( fmt, args ) );
	}

	@Override
	public void info( String msg, Throwable t ) {
		doLog( Log.LEVEL_INFO, Severity.INFO, msg, t );
	}

	@Override
	public boolean isInfoEnabled( Marker marker ) {
		return isInfoEnabled();
	}

	@Override
	public void info( Marker marker, String msg ) {
		info( msg );
	}

	@Override
	public void info( Marker marker, String fmt, Object arg ) {
		info( fmt, arg );
	}

	@Override
	public void info( Marker marker, String fmt, Object arg1, Object arg2 ) {
		info( fmt, arg1, arg2 );
	}

	@Override
	public void info( Marker marker, String fmt, Object... args ) {
		info( fmt, args );
	}

	@Override
	public void info( Marker marker, String msg, Throwable t ) {
		info( msg, t );
	}

	// ---------------------------------------------------------------
	// WARN
	// ---------------------------------------------------------------

	@Override
	public boolean isWarnEnabled() {
		return isEnabled( Severity.WARN );
	}

	@Override
	public void warn( String msg ) {
		doLog( Log.LEVEL_WARN, Severity.WARN, msg );
	}

	@Override
	public void warn( String fmt, Object arg ) {
		if ( isWarnEnabled() )
			doLog( Log.LEVEL_WARN, Severity.WARN, format( fmt, arg ) );
	}

	@Override
	public void warn( String fmt, Object arg1, Object arg2 ) {
		if ( isWarnEnabled() )
			doLog( Log.LEVEL_WARN, Severity.WARN, format( fmt, arg1, arg2 ) );
	}

	@Override
	public void warn( String fmt, Object... args ) {
		if ( isWarnEnabled() )
			doLog( Log.LEVEL_WARN, Severity.WARN, format( fmt, args ) );
	}

	@Override
	public void warn( String msg, Throwable t ) {
		doLog( Log.LEVEL_WARN, Severity.WARN, msg, t );
	}

	@Override
	public boolean isWarnEnabled( Marker marker ) {
		return isWarnEnabled();
	}

	@Override
	public void warn( Marker marker, String msg ) {
		warn( msg );
	}

	@Override
	public void warn( Marker marker, String fmt, Object arg ) {
		warn( fmt, arg );
	}

	@Override
	public void warn( Marker marker, String fmt, Object arg1, Object arg2 ) {
		warn( fmt, arg1, arg2 );
	}

	@Override
	public void warn( Marker marker, String fmt, Object... args ) {
		warn( fmt, args );
	}

	@Override
	public void warn( Marker marker, String msg, Throwable t ) {
		warn( msg, t );
	}

	// ---------------------------------------------------------------
	// ERROR
	// ---------------------------------------------------------------

	@Override
	public boolean isErrorEnabled() {
		return isEnabled( Severity.ERROR );
	}

	@Override
	public void error( String msg ) {
		doLog( Log.LEVEL_ERROR, Severity.ERROR, msg );
	}

	@Override
	public void error( String fmt, Object arg ) {
		if ( isErrorEnabled() )
			doLog( Log.LEVEL_ERROR, Severity.ERROR, format( fmt, arg ) );
	}

	@Override
	public void error( String fmt, Object arg1, Object arg2 ) {
		if ( isErrorEnabled() )
			doLog( Log.LEVEL_ERROR, Severity.ERROR, format( fmt, arg1, arg2 ) );
	}

	@Override
	public void error( String fmt, Object... args ) {
		if ( isErrorEnabled() )
			doLog( Log.LEVEL_ERROR, Severity.ERROR, format( fmt, args ) );
	}

	@Override
	public void error( String msg, Throwable t ) {
		doLog( Log.LEVEL_ERROR, Severity.ERROR, msg, t );
	}

	@Override
	public boolean isErrorEnabled( Marker marker ) {
		return isErrorEnabled();
	}

	@Override
	public void error( Marker marker, String msg ) {
		error( msg );
	}

	@Override
	public void error( Marker marker, String fmt, Object arg ) {
		error( fmt, arg );
	}

	@Override
	public void error( Marker marker, String fmt, Object arg1, Object arg2 ) {
		error( fmt, arg1, arg2 );
	}

	@Override
	public void error( Marker marker, String fmt, Object... args ) {
		error( fmt, args );
	}

	@Override
	public void error( Marker marker, String msg, Throwable t ) {
		error( msg, t );
	}
}
