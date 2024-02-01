package junit.mapping;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ortus.extension.orm.mapping.CFConstants;

public class CFConstantsTest {

	@Test
	public void canValidateCacheUseValue() {
		assertTrue( CFConstants.CacheUse.isValidValue( "read-only" ), "Expected 'read-only' to be valid." );
		assertTrue( CFConstants.CacheUse.isValidValue( "nonstrict-read-write" ), "Expected 'nonstrict-read-write' to be valid." );
		assertTrue( CFConstants.CacheUse.isValidValue( "read-write" ), "Expected 'read-write' to be valid." );
		assertTrue( CFConstants.CacheUse.isValidValue( "transactional" ), "Expected 'transactional' to be valid." );

		assertFalse( CFConstants.CacheUse.isValidValue( "read-ONLY" ), "Expected 'read-ONLY' to be invalid." );
		assertFalse( CFConstants.CacheUse.isValidValue( "readonly" ), "Expected 'readonly' to be invalid." );
		assertFalse( CFConstants.CacheUse.isValidValue( "foobar" ), "Expected 'foobar' to be invalid." );
	}

}