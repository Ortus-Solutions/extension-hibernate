package junit.event;

import org.junit.jupiter.api.Disabled;

// extension classes
import ortus.extension.orm.event.EventListenerIntegrator;

// Lucee stuffs

// Testing and mocking
import org.junit.jupiter.api.Test;

@Disabled
public class EventListenerIntegratorTest {

	@Test
	public void canInitialize() {
		new EventListenerIntegrator();
	}
}
