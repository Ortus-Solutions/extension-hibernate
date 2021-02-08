package org.lucee.extension.orm.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class ConnectionProviderProxy implements ConnectionProvider {

	public static ConnectionProvider provider;

	// public void close() throws HibernateException {
	// provider.close();
	// }

	@Override
	public void closeConnection(Connection arg0) throws SQLException {
		provider.closeConnection(arg0);
	}

	// public void configure(Properties arg0) throws HibernateException {
	// provider.configure(arg0);
	// }

	@Override
	public Connection getConnection() throws SQLException {
		return provider.getConnection();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return provider.supportsAggressiveRelease();
	}

	@Override
	public boolean isUnwrappableAs(Class arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
