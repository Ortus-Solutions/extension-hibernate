package ortus.extension.orm.jdbc;

import org.hibernate.cfg.Configuration;

import lucee.runtime.db.DataSource;

public class DataSourceConfig {

	public final DataSource		ds;
	public final Configuration	config;

	public DataSourceConfig( DataSource ds, Configuration config ) {
		this.ds		= ds;
		this.config	= config;
	}
}
