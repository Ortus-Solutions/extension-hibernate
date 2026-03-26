package ortus.extension.orm.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * SLF4J 2.x service provider that routes all SLF4J logging through Lucee's native Log interface.
 *
 * Discovered via META-INF/services/org.slf4j.spi.SLF4JServiceProvider.
 * This replaces Logback as the SLF4J backend, so Hibernate's internal logging
 * (SQL, params, schema DDL, cache) flows into Lucee's orm.log.
 */
public class LuceeSLF4JServiceProvider implements SLF4JServiceProvider {

	/**
	 * Must match the SLF4J API version we compile against.
	 */
	public static final String				REQUESTED_API_VERSION	= "2.0.7";

	private static LuceeBackedLoggerFactory	loggerFactory;
	private IMarkerFactory					markerFactory;
	private MDCAdapter						mdcAdapter;

	/**
	 * Get the shared logger factory instance.
	 * Static so that {@link LoggerLevelManager} can access it to push the Lucee Log instance.
	 * Returns null if SLF4J hasn't initialized this provider (e.g. OSGi classloader didn't find it).
	 */
	public static LuceeBackedLoggerFactory getStaticLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	@Override
	public MDCAdapter getMDCAdapter() {
		return mdcAdapter;
	}

	@Override
	public String getRequestedApiVersion() {
		return REQUESTED_API_VERSION;
	}

	@Override
	public void initialize() {
		loggerFactory	= new LuceeBackedLoggerFactory();
		markerFactory	= new BasicMarkerFactory();
		// MDC not supported — Lucee's Log interface has no diagnostic context concept
		mdcAdapter		= new NOPMDCAdapter();
	}
}
