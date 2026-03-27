package ortus.extension.orm.logging;

import java.util.Collections;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.LoggerProvider;

import lucee.commons.io.log.Log;

/**
 * JBoss Logging LoggerProvider that routes Hibernate's internal logging to Lucee's native Log.
 *
 * Discovered via META-INF/services/org.jboss.logging.LoggerProvider (ServiceLoader).
 * This bypasses SLF4J entirely for Hibernate's own logging, going straight from
 * JBoss Logging -> Lucee's orm.log.
 *
 * The Lucee Log instance is set by {@link LoggerLevelManager#configure} when the ORM engine initializes.
 */
public class LuceeJBossLoggerProvider implements LoggerProvider {

	private static volatile Log luceeLog;

	public static void setLuceeLog( Log log ) {
		luceeLog = log;
	}

	public static Log getLuceeLog() {
		return luceeLog;
	}

	@Override
	public Logger getLogger( String name ) {
		return new LuceeJBossLogger( name );
	}

	// MDC/NDC not supported — Lucee's Log interface has no diagnostic context concept.
	// These no-ops are required by the LoggerProvider interface.

	@Override
	public void clearMdc() {
	}

	@Override
	public Object putMdc( String key, Object value ) {
		return null;
	}

	@Override
	public Object getMdc( String key ) {
		return null;
	}

	@Override
	public void removeMdc( String key ) {
	}

	@Override
	public Map<String, Object> getMdcMap() {
		return Collections.emptyMap();
	}

	@Override
	public void clearNdc() {
	}

	@Override
	public String getNdc() {
		return null;
	}

	@Override
	public int getNdcDepth() {
		return 0;
	}

	@Override
	public String popNdc() {
		return null;
	}

	@Override
	public String peekNdc() {
		return null;
	}

	@Override
	public void pushNdc( String message ) {
	}

	@Override
	public void setNdcMaxDepth( int maxDepth ) {
	}
}
