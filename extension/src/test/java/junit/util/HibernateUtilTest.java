package junit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ortus.extension.orm.util.HibernateUtil;

public class HibernateUtilTest {

	@Test
	public void canIdentifyReservedKeywords() {

		assertEquals( true, HibernateUtil.isKeyword( "year" ) );
		assertEquals( true, HibernateUtil.isKeyword( "and" ) );
		assertEquals( true, HibernateUtil.isKeyword( "select" ) );
		assertEquals( false, HibernateUtil.isKeyword( "name" ) );
		assertEquals( false, HibernateUtil.isKeyword( "age" ) );
		assertEquals( false, HibernateUtil.isKeyword( "description" ) );
	}
}