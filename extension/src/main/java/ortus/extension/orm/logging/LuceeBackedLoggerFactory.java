package ortus.extension.orm.logging;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import lucee.commons.io.log.Log;

/**
 * SLF4J ILoggerFactory that produces {@link LuceeBackedLogger} instances.
 *
 * All loggers delegate to a single Lucee {@link Log} instance (the "orm" log).
 * The Lucee Log is set lazily once the ORM engine initializes via {@link #setLuceeLog(Log)}.
 * Before that point, loggers exist but produce no output (safe no-op).
 */
public class LuceeBackedLoggerFactory implements ILoggerFactory {

	private final ConcurrentHashMap<String, LuceeBackedLogger>	loggers	= new ConcurrentHashMap<>();
	private volatile Log										luceeLog;

	@Override
	public Logger getLogger( String name ) {
		return loggers.computeIfAbsent( name, n -> {
			LuceeBackedLogger logger = new LuceeBackedLogger( n );
			if ( luceeLog != null )
				logger.setLuceeLog( luceeLog );
			return logger;
		} );
	}

	/**
	 * Set the Lucee Log instance on all existing and future loggers.
	 * Called from {@link LoggerLevelManager#configure}.
	 */
	public void setLuceeLog( Log log ) {
		this.luceeLog = log;
		for ( LuceeBackedLogger logger : loggers.values() ) {
			logger.setLuceeLog( log );
		}
	}
}
