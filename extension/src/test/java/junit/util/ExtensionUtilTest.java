package junit.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ortus.extension.orm.util.ExtensionUtil;

public class ExtensionUtilTest {

	@Test
	public void canGetHibernateVersion() {
		String	actual			= ExtensionUtil.getHibernateVersion();
		String	versionFormat	= "\\d{0,3}.\\d{0,3}.\\d{0,3}..*";
		assertTrue( actual.matches( versionFormat ) );
	}

	@Disabled( "Wont pass outside of a .jar with manifest containing Implementation-Version." )
	@Test
	public void canGetExtensionVersion() {
		assertEquals( "5.4.29.27", ExtensionUtil.getExtensionVersion() );
	}

	@Test
	public void canGetJVMVersion() {
		// assertNoThrow() does not exist, so we just run it and if it passes, it didn't throw.
		ExtensionUtil.getJVMVersion();
	}
}