package ortus.extension.orm.util;

/**
 * Generic utils that DO NOT need a running Lucee engine instance to function.
 */
public class ExtensionUtil {

	private ExtensionUtil() {
		throw new IllegalStateException( "Utility class; please don't instantiate!" );
	}

	/**
	 * Get the extension version as compiled into the Manifest.MF's `Specification-Version` entry.
	 *
	 * Will not work in compiled class files (think junit tests) because these are run outside of the packaged .jar
	 * file.
	 */
	public static String getExtensionVersion() {
		return ExtensionUtil.class.getPackage().getSpecificationVersion();
	}

	/**
	 * Acquire and return the Hibernate version.
	 */
	public static String getHibernateVersion() {
		return org.hibernate.Version.getVersionString();
	}

	/**
	 * Detect the JVM version. Useful for determining which system property to use for denoting the JAXB context
	 * factory.
	 *
	 * All credits to Aaron Digulla: https://stackoverflow.com/a/2591122
	 *
	 * @return the major java version, like `8`, `9`, `11`, `17`, etc.
	 */
	public static int getJVMVersion() {
		String version = System.getProperty( "java.version" );
		if ( version.startsWith( "1." ) ) {
			version = version.substring( 2, 3 );
		} else {
			int dot = version.indexOf( "." );
			if ( dot != -1 ) {
				version = version.substring( 0, dot );
			}
		}
		return Integer.parseInt( version );
	}
}