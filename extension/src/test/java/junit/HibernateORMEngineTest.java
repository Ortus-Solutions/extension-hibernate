package junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

// extension classes
import ortus.extension.orm.HibernateORMEngine;

// Lucee stuffs
// Testing and mocking
import org.junit.jupiter.api.Test;

class HibernateORMEngineTest {

	HibernateORMEngine engine = new HibernateORMEngine();

	@Test
	void canInitialize() {
		assertEquals( 0, engine.getMode() );
	}

}