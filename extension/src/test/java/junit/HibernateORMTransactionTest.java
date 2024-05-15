package junit;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import ortus.extension.orm.HibernateORMTransaction;

import org.junit.jupiter.api.Disabled;
// Testing and mocking
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

@Disabled( "No longer able to mock SessionDelegatorBaseImpl in java 21" )
public class HibernateORMTransactionTest {

	@Mock
	private Session MockSession;

	public HibernateORMTransactionTest() {
		MockSession = Mockito.mock( SessionDelegatorBaseImpl.class );
	}

	@Test
	public void canInitialize() {
		Boolean autoManage = false;
		new HibernateORMTransaction( MockSession, autoManage );
	}
}