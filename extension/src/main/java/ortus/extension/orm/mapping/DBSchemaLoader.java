package ortus.extension.orm.mapping;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.component.Property;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.util.Creation;
import lucee.runtime.type.Struct;
import ortus.extension.orm.ColumnInfo;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ExceptionUtil;

public class DBSchemaLoader {

	private DatabaseMetaData	dbMeta;
	private Creation			creationUtil;

	private String				tableName;
	private String				dbName;

	public DBSchemaLoader( DatasourceConnection dbConnection, String tableName ) throws PageException {
		this.tableName		= tableName;
		this.creationUtil	= CFMLEngineFactory.getInstance().getCreationUtil();

		try {
			this.dbMeta	= dbConnection.getConnection().getMetaData();
			this.dbName	= CFMLEngineFactory.getInstance().getDBUtil().getDatabaseName( dbConnection );
		} catch ( SQLException e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	public DBSchemaLoader setTableName( String tableName ) {
		this.tableName = tableName;
		return this;
	}

	/**
	 * Get table metadata as an array of Property objects so we can interact with them as if they were loaded from a Lucee Component.
	 */
	public Property[] createPropertiesFromTable() {
		Struct properties = CommonUtil.createStruct();
		try {

			Collection.Key	name;

			// get all columns
			ResultSet		res	= dbMeta.getColumns( dbName, null, tableName, null );
			while ( res.next() ) {
				name = CommonUtil.createKey( res.getString( "COLUMN_NAME" ) );
				properties.setEL( name, CommonUtil.createProperty( name.getString(), res.getString( "TYPE_NAME" ) ) );
			}

			// ids
			res = dbMeta.getPrimaryKeys( null, null, tableName );
			Property p;
			while ( res.next() ) {
				name	= CommonUtil.createKey( res.getString( "COLUMN_NAME" ) );
				p		= ( Property ) properties.get( name, null );
				if ( p != null )
					p.getDynamicAttributes().setEL( CommonUtil.FIELDTYPE, "id" );
			}

			// @TODO: foreign-key relation

		} catch ( Exception t ) {
			return new Property[ 0 ];
		}

		Iterator<Object>	it	= properties.valueIterator();
		Property[]			rtn	= new Property[ properties.size() ];
		for ( int i = 0; i < rtn.length; i++ ) {
			rtn[ i ] = ( Property ) it.next();
		}

		return rtn;
	}

	/**
	 * Get table schema (mainly column data) as a struct. Returns null if no column data found.
	 *
	 * @throws PageException
	 */
	public Struct getTableSchema() throws PageException {
		try {
			Struct rows = tryGetTableSchema();
			if ( rows.isEmpty() ) {
				String properTableName = getTableNameInProperCasing();
				if ( properTableName != null ) {
					setTableName( properTableName );
					rows = tryGetTableSchema();
				}
			}

			if ( rows.isEmpty() ) {
				return null;
			}
			return rows;
		} catch ( SQLException e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	/**
	 * Read table column info back as a struct.
	 *
	 * If the table name did not match ( some DBs are case-sensitive) it will return an empty struct.
	 *
	 * Supported properties include:
	 *
	 * * `DATA_TYPE`
	 * * `TYPE_NAME`
	 * * `COLUMN_SIZE`
	 * * `nullable`
	 *
	 * @throws SQLException
	 * @throws PageException
	 */
	private Struct tryGetTableSchema()
	    throws SQLException, PageException {
		Struct rows = creationUtil.createCastableStruct( tableName, Struct.TYPE_LINKED );

		try ( ResultSet columns = dbMeta.getColumns( dbName, null, tableName, null ); ) {
			String	name;
			Object	nullable;
			while ( columns.next() ) {
				name		= columns.getString( "COLUMN_NAME" );
				nullable	= columns.getObject( "IS_NULLABLE" );
				rows.setEL(
				    CommonUtil.createKey( name ),
				    new ColumnInfo(
				        name,
				        columns.getInt( "DATA_TYPE" ),
				        columns.getString( "TYPE_NAME" ),
				        columns.getInt( "COLUMN_SIZE" ),
				        CommonUtil.toBooleanValue( nullable )
				    )
				);
			}
		}

		return rows;
	}

	/**
	 * Iterate over all table names for the provided <code>DatabaseMetaData</code>, looking for a matching (ignoring case) table name.
	 *
	 * Once a match is found, return it. Else return null.
	 */
	private String getTableNameInProperCasing() {
		try ( ResultSet tables = dbMeta.getTables( dbName, null, null, null ); ) {
			String name;
			while ( tables.next() ) {
				name = tables.getString( "TABLE_NAME" );
				if ( name.equalsIgnoreCase( tableName )
				    && tables.getString( "TABLE_TYPE" ).toUpperCase().indexOf( "SYSTEM" ) == -1 )
					return name;
			}
		} catch ( Exception t ) {
			// @TODO: @nextMajorRelease consider dropping this catch block
		}
		return null;

	}

}
