package ortus.extension.orm.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import ortus.extension.orm.util.CommonUtil;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;

public class ConnectionProviderImpl implements ConnectionProvider {

	private static final long	serialVersionUID	= -4513189055112912809L;

	private CFMLEngine			engine;
	private final DataSource	ds;
	private final String		user;
	private final String		pass;

	public ConnectionProviderImpl( DataSource ds, String user, String pass ) {
		engine		= CFMLEngineFactory.getInstance();
		this.ds		= ds;
		this.user	= user;
		this.pass	= pass;
	}

	@Override
	public Connection getConnection() throws SQLException {
		PageContext pc = engine.getThreadPageContext();

		try {
			return CommonUtil.getDatasourceConnection( pc, ds, user, pass, true );
			// FUTURE we do not use because this is not managed the 4th argument is required return
			// dbu.getDatasourceConnection(pc, ds, user, pass,true);
		} catch ( PageException pe ) {
			throw engine.getExceptionUtil().createPageRuntimeException( pe );
		}
	}

	@Override
	public void closeConnection( Connection conn ) throws SQLException {
		PageContext pc = engine.getThreadPageContext();
		if ( conn instanceof DatasourceConnection ) {
			try {
				CommonUtil.releaseDatasourceConnection( pc, ( DatasourceConnection ) conn, true );
				// FUTURE see comment above dbu.releaseDatasourceConnection(engine.getThreadConfig(),
				// (DatasourceConnection) conn, false);
			} catch ( PageException pe ) {
				throw engine.getExceptionUtil().createPageRuntimeException( pe );
			}
		}
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	@SuppressWarnings( "rawtypes" )
	public boolean isUnwrappableAs( Class unwrapType ) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap( Class<T> unwrapType ) {
		// TODO Auto-generated method stub
		return null;
	}

}
