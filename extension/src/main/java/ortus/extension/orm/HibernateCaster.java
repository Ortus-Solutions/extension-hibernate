package ortus.extension.orm;

import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import lucee.commons.lang.types.RefBoolean;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import ortus.extension.orm.mapping.CFConstants;
import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ExceptionUtil;
import ortus.extension.orm.util.HibernateUtil;

public class HibernateCaster {

	private HibernateCaster() {
		throw new IllegalStateException( "Utility class; please don't instantiate!" );
	}

	private static final int NULL = -178696;

	/**
	 * Convert a Java object (such as a Lucee Collection or java List) to a CFML-compatible value.
	 *
	 * @param src Java object.
	 *
	 * @return CFML-compatible List object or Collection object or the original unmodified value.
	 */
	@SuppressWarnings( "rawtypes" )
	public static Object toCFML( Object src ) {
		if ( src == null )
			return null;
		if ( src instanceof Collection )
			return src;

		if ( src instanceof List ) {
			return toCFML( ( List ) src );
		}
		return src;
	}

	/**
	 * Convert a Java list to a CFML list.
	 *
	 * @param src Java List of objects.
	 *
	 * @return CFML-compatible List object.
	 */
	@SuppressWarnings( "rawtypes" )
	public static Array toCFML( List src ) {
		int		size	= src.size();

		Array	trg		= CommonUtil.createArray();
		for ( int i = 0; i < size; i++ ) {
			trg.setEL( i + 1, toCFML( src.get( i ) ) );
		}
		return trg;
	}

	/**
	 * Get the persistent entity name for the given Component.
	 *
	 * Entity name is either derived from the `entityname` attribute, or the component name itself.
	 *
	 * @param cfc A persistent Component.
	 *
	 * @return
	 */
	public static String getEntityName( Component cfc ) {

		String name = null;
		try {
			name = CommonUtil.toString( cfc.getMetaStructItem( CommonUtil.ENTITY_NAME ), null );
		} catch ( Exception t ) {
			try {
				Struct md = cfc.getMetaData( CommonUtil.pc() );
				name = CommonUtil.toString( md.get( CommonUtil.ENTITY_NAME ), null );

			} catch ( PageException e ) {
				// @TODO: @nextMajorRelease either drop this catch and let errors out, or handle it properly with a log and fall back.
			}
		}

		if ( !Util.isEmpty( name ) ) {
			return name;
		}
		return getName( cfc );

	}

	/**
	 * Get the component name from the file path.
	 *
	 * @param cfc A Lucee component.
	 *
	 * @return Last item in the dot-notation component path
	 */
	private static String getName( Component cfc ) {
		// @TODO: cfc.getName() should return the real case, this should not be needed
		String	name	= CommonUtil.last( cfc.getPageSource().getDisplayPath(), "\\/" );
		int		index	= name.lastIndexOf( '.' );
		return name.substring( 0, index );
	}

	/**
	 * Try to retrieve a Hibernate type for this column by checking both the {@link ColumnInfo} or the string sql type,
	 * whichever is populated and returns a result first.
	 *
	 * @param info         a {@link ortus.extension.orm.ColumnInfo} object containing a known Java SQL type.
	 * @param type         The string name of a SQL type.
	 * @param defaultValue Default to return if SQL type not detected.
	 *
	 * @return
	 */
	public static String toHibernateType( ColumnInfo info, String type, String defaultValue ) {

		// no type defined
		if ( Util.isEmpty( type, true ) ) {
			return HibernateCaster.toHibernateType( info, defaultValue );
		}

		// type defined
		String tmp = HibernateCaster.toHibernateType( type, null );
		if ( tmp != null )
			return tmp;

		if ( info != null ) {
			tmp = HibernateCaster.toHibernateType( info, defaultValue );
			return tmp;
		}
		return defaultValue;

	}

	private static int toSQLType( String type, int defaultValue ) {
		type	= type.trim().toLowerCase();
		type	= toHibernateType( type, type );
		if ( "long".equals( type ) )
			return Types.BIGINT;
		if ( "binary".equals( type ) )
			return Types.BINARY;
		if ( "boolean".equals( type ) )
			return Types.BIT;
		if ( "blob".equals( type ) )
			return Types.BLOB;
		if ( "character".equals( type ) )
			return Types.CHAR;
		if ( "clob".equals( type ) )
			return Types.CLOB;
		if ( "date".equals( type ) )
			return Types.DATE;
		if ( "big_decimal".equals( type ) )
			return Types.DECIMAL;
		if ( "big_integer".equals( type ) )
			return Types.NUMERIC;
		if ( "double".equals( type ) )
			return Types.DOUBLE;
		if ( "float".equals( type ) )
			return Types.FLOAT;
		if ( "integer".equals( type ) )
			return Types.INTEGER;
		if ( "string".equals( type ) )
			return Types.VARCHAR;
		if ( "short".equals( type ) )
			return Types.SMALLINT;
		if ( "time".equals( type ) )
			return Types.TIME;
		if ( "timestamp".equals( type ) )
			return Types.TIMESTAMP;
		if ( "byte".equals( type ) )
			return Types.TINYINT;

		return defaultValue;
	}

	/**
	 * Translate the given `java.sql.Types` SQL type from an {@link ColumnInfo} to its Hibernate counterpart.
	 *
	 * Might be utilized after pulling ORM mapping data from an existing database layout.
	 *
	 * @param info         a {@link ortus.extension.orm.ColumnInfo} object containing a known Java SQL type.
	 * @param defaultValue Default to return if SQL type not detected.
	 */
	private static String toHibernateType( ColumnInfo info, String defaultValue ) {
		if ( info == null )
			return defaultValue;

		String rtn = toHibernateType( info.getType(), null );
		if ( rtn != null )
			return rtn;
		return toHibernateType( info.getTypeName(), defaultValue );
	}

	/**
	 * Translate the given `java.sql.Types` SQL type for a column or field to its Hibernate counterpart.
	 *
	 * @param type         One of the SQL types. {@link java.sql.Types}
	 * @param defaultValue Default to return if SQL type not detected.
	 */
	private static String toHibernateType( int type, String defaultValue ) {
		switch ( type ) {
			case Types.ARRAY :
				return "";
			case Types.BIGINT :
				return "long";
			case Types.BINARY :
				return "binary";
			case Types.BIT :
				return "boolean";
			case Types.BLOB :
				return "blob";
			case Types.BOOLEAN :
				return "boolean";
			case Types.CHAR :
				return "string";
			case Types.CLOB :
				return "clob";
			case Types.DATE :
				return "date";
			case Types.DECIMAL :
				return "big_decimal";
			case Types.DOUBLE :
				return "double";
			case Types.FLOAT :
				return "float";
			case Types.INTEGER :
				return "integer";
			case Types.LONGVARBINARY :
				return "binary";
			case Types.LONGVARCHAR :
				return "string";
			case Types.NUMERIC :
				return "big_decimal";
			case Types.SMALLINT :
				return "short";
			case Types.TIME :
				return "time";
			case Types.TIMESTAMP :
				return "timestamp";
			case Types.TINYINT :
				return "byte";
			case Types.VARBINARY :
				return "binary";
			case Types.NVARCHAR :
				return "string";
			case Types.VARCHAR :
				return "string";
			default :
				return defaultValue;
		}
	}

	// calendar_date: A type mapping for a Calendar object that represents a date
	// calendar: A type mapping for a Calendar object that represents a datetime.
	private static String toHibernateType( String type, String defaultValue ) {
		type	= type.trim().toLowerCase();
		type	= type.replace( "java.lang.", "" );
		type	= type.replace( "java.util.", "" );
		type	= type.replace( "java.sql.", "" );

		// return same value
		if ( "long".equals( type ) )
			return type;
		if ( "binary".equals( type ) )
			return type;
		if ( "boolean".equals( type ) )
			return type;
		if ( "blob".equals( type ) )
			return "binary";
		if ( "character".equals( type ) )
			return type;
		if ( "clob".equals( type ) )
			return "text";
		if ( "date".equals( type ) )
			return type;
		if ( "big_decimal".equals( type ) )
			return type;
		if ( "double".equals( type ) )
			return type;
		if ( "float".equals( type ) )
			return type;
		if ( "integer".equals( type ) )
			return type;
		if ( "string".equals( type ) )
			return type;
		if ( "big_integer".equals( type ) )
			return type;
		if ( "short".equals( type ) )
			return type;
		if ( "time".equals( type ) )
			return type;
		if ( "timestamp".equals( type ) )
			return type;
		if ( "byte".equals( type ) )
			return type;
		if ( "string".equals( type ) )
			return type;
		if ( "text".equals( type ) )
			return type;
		if ( "calendar".equals( type ) )
			return type;
		if ( "calendar_date".equals( type ) )
			return type;
		if ( "locale".equals( type ) )
			return type;
		if ( "timezone".equals( type ) )
			return type;
		if ( "currency".equals( type ) )
			return type;

		if ( "imm_date".equals( type ) )
			return type;
		if ( "imm_time".equals( type ) )
			return type;
		if ( "imm_timestamp".equals( type ) )
			return type;
		if ( "imm_calendar".equals( type ) )
			return type;
		if ( "imm_calendar_date".equals( type ) )
			return type;
		if ( "imm_serializable".equals( type ) )
			return type;
		if ( "imm_binary".equals( type ) )
			return type;

		// return different value
		if ( "bigint".equals( type ) )
			return "long";
		if ( "bit".equals( type ) )
			return "boolean";

		if ( "int".equals( type ) )
			return "integer";
		if ( "char".equals( type ) )
			return "character";

		if ( "bool".equals( type ) )
			return "boolean";
		if ( "yes-no".equals( type ) )
			return "yes_no";
		if ( "yesno".equals( type ) )
			return "yes_no";
		if ( "yes_no".equals( type ) )
			return "yes_no";
		if ( "true-false".equals( type ) )
			return "true_false";
		if ( "truefalse".equals( type ) )
			return "true_false";
		if ( "true_false".equals( type ) )
			return "true_false";
		if ( "varchar".equals( type ) )
			return "string";
		if ( "big-decimal".equals( type ) )
			return "big_decimal";
		if ( "bigdecimal".equals( type ) )
			return "big_decimal";
		if ( "java.math.bigdecimal".equals( type ) )
			return "big_decimal";
		if ( "big-integer".equals( type ) )
			return "big_integer";
		if ( "biginteger".equals( type ) )
			return "big_integer";
		if ( "bigint".equals( type ) )
			return "big_integer";
		if ( "java.math.biginteger".equals( type ) )
			return "big_integer";
		if ( "byte[]".equals( type ) )
			return "binary";
		if ( "serializable".equals( type ) )
			return "serializable";

		if ( "datetime".equals( type ) )
			return "timestamp";
		if ( "numeric".equals( type ) )
			return "double";
		if ( "number".equals( type ) )
			return "double";
		if ( "numeric".equals( type ) )
			return "double";
		if ( "char".equals( type ) )
			return "character";
		if ( "nchar".equals( type ) )
			return "character";
		if ( "decimal".equals( type ) )
			return "double";
		if ( "eurodate".equals( type ) )
			return "timestamp";
		if ( "usdate".equals( type ) )
			return "timestamp";
		if ( "int".equals( type ) )
			return "integer";
		if ( "varchar".equals( type ) )
			return "string";
		if ( "nvarchar".equals( type ) )
			return "string";

		return defaultValue;
	}

	public static Object toHibernateValue( PageContext pc, Object value, String type ) throws PageException {
		type = toHibernateType( type, null );
		// return same value
		if ( "long".equals( type ) )
			return CommonUtil.toLong( value );
		if ( "binary".equals( type ) || "imm_binary".equals( type ) )
			return CommonUtil.toBinary( value );
		if ( "boolean".equals( type ) || "yes_no".equals( type ) || "true_false".equals( type ) )
			return CommonUtil.toBoolean( value );
		if ( "character".equals( type ) )
			return CommonUtil.toCharacter( value );
		if ( "date".equals( type ) || "imm_date".equals( type ) )
			return CommonUtil.toDate( value, pc.getTimeZone() );
		if ( "big_decimal".equals( type ) )
			return CommonUtil.toBigDecimal( value );
		if ( "double".equals( type ) )
			return CommonUtil.toDouble( value );
		if ( "float".equals( type ) )
			return CommonUtil.toFloat( value );
		if ( "integer".equals( type ) )
			return CommonUtil.toInteger( value );
		if ( "string".equals( type ) )
			return CommonUtil.toString( value );
		if ( "big_integer".equals( type ) )
			return new BigInteger( CommonUtil.toString( value ) );
		if ( "short".equals( type ) )
			return CommonUtil.toShort( value );
		if ( "time".equals( type ) || "imm_time".equals( type ) )
			return new Time( CommonUtil.toDate( value, pc.getTimeZone() ).getTime() );
		if ( "timestamp".equals( type ) || "imm_timestamp".equals( type ) )
			return new Timestamp( CommonUtil.toDate( value, pc.getTimeZone() ).getTime() );
		if ( "byte".equals( type ) )
			return CommonUtil.toBinary( value );
		if ( "text".equals( type ) )
			return CommonUtil.toString( value );
		if ( "calendar".equals( type ) || "calendar_date".equals( type ) || "imm_calendar".equals( type )
		    || "imm_calendar_date".equals( type ) )
			return CommonUtil.toCalendar( CommonUtil.toDate( value, pc.getTimeZone() ), pc.getTimeZone(), pc.getLocale() );
		if ( "locale".equals( type ) )
			return CommonUtil.toLocale( CommonUtil.toString( value ) );
		if ( "timezone".equals( type ) )
			return CommonUtil.toTimeZone( value, null );
		if ( "currency".equals( type ) )
			return value;

		if ( "imm_serializable".equals( type ) )
			return value;
		if ( "serializable".equals( type ) )
			return "serializable";

		return value;
	}

	/**
	 * Given the Property object (representing a property on a Lucee .cfc Component file), return the value of the
	 * property in the correct type as specified by the property's `ormtype` or `type` annotations.
	 *
	 * @param entity   The Lucee component to pull the value from. (Since Lucee's Property.getValue() method returns the
	 *                 default, NOT the actual value, we need to retrieve the value from the ComponentScope object.)
	 * @param property A persistent Property from a persistent component.
	 *
	 * @return The property value with the correct Hibernate typing. {@link toHibernateValue(PageContext pc, Object
	 *         value, String type)}
	 *
	 * @throws PageException
	 */
	public static Object toHibernateValue( Component entity, Property property ) throws PageException {
		PageContext	pc		= CFMLEngineFactory.getInstance().getThreadPageContext();
		Object		value	= entity.getComponentScope().get( CommonUtil.createKey( property.getName() ), property.getDefault() );

		// Short-circuit if the value is null, no need to do any further processing.
		if ( value == null ) {
			return value;
		}

		Struct	meta		= ( Struct ) property.getMetaData();
		String	ormType		= CommonUtil.toString( meta.get( CommonUtil.ORMTYPE, "" ) );
		String	fieldType	= !ormType.trim().isEmpty() ? ormType : property.getType();

		// If the value is an empty string, and the field type is not one of the types that should be converted to null
		if ( value instanceof String &&
		    ( ( String ) value ).trim().isEmpty() &&
		    !CFConstants.UnsavedValue.EMPTY_TO_NULL.contains( fieldType ) ) {
			return null;
		}

		return toHibernateValue( pc, value, fieldType );
	}

	/**
	 * translate CFMl specific types to Hibernate/SQL specific types
	 *
	 * @param type
	 * @param value
	 * @param isArray
	 *
	 * @throws PageException
	 */
	public static Object toSQL( Type type, Object value, RefBoolean isArray ) throws PageException {
		int t = toSQLType( type.getName(), Types.OTHER );
		return toSQL( t, value, isArray );
	}

	/**
	 * translate CFMl specific type to SQL specific types
	 *
	 * @param sqlType
	 * @param value
	 * @param isArray
	 *
	 * @throws PageException
	 */
	private static Object toSQL( int sqlType, Object value, RefBoolean isArray ) throws PageException {
		if ( sqlType == Types.OTHER && value instanceof PersistentCollection ) {
			return value;
		}

		if ( isArray != null )
			isArray.setValue( false );

		Boolean	_isArray	= null;
		boolean	hasType		= sqlType != Types.OTHER;

		// first we try to convert without any checking
		if ( hasType ) {
			try {
				return CommonUtil.toSqlType( CommonUtil.toSQLItem( value, sqlType ) );
			} catch ( PageException pe ) {
				_isArray = CommonUtil.isArray( value );
				if ( !_isArray.booleanValue() )
					throw pe;
			}
		}

		// already a hibernate type

		// can only be null if type is other
		if ( _isArray == null && !CommonUtil.isArray( value ) ) {
			return value;
		}

		// at this point it is for sure that the value is an array
		if ( isArray != null )
			isArray.setValue( true );
		Array				src	= CommonUtil.toArray( value );
		Iterator<Object>	it	= src.valueIterator();
		ArrayList<Object>	trg	= new ArrayList<>();
		Object				v;
		while ( it.hasNext() ) {
			v = it.next();
			if ( v == null )
				continue;
			if ( hasType ) {
				trg.add( CommonUtil.toSqlType( CommonUtil.toSQLItem( v, sqlType ) ) );
			} else
				trg.add( v );
		}
		return trg;
	}

	public static lucee.runtime.type.Query toQuery( PageContext pc, HibernateORMSession session, Object obj, String name )
	    throws PageException {
		Query qry = null;
		// a single entity
		if ( !CommonUtil.isArray( obj ) ) {
			qry = toQuery( pc, session, HibernateCaster.toComponent( obj ), name, null, 1, 1 );
		}

		// a array of entities
		else {
			Array	arr	= CommonUtil.toArray( obj );
			int		len	= arr.size();
			if ( len > 0 ) {
				Iterator<Object>	it	= arr.valueIterator();
				int					row	= 1;
				while ( it.hasNext() ) {
					qry = toQuery( pc, session, HibernateCaster.toComponent( it.next() ), name, qry, len, row++ );
				}
			} else
				qry = CommonUtil.createQuery( new Collection.Key[ 0 ], 0, "orm" );
		}

		if ( qry == null ) {
			if ( !Util.isEmpty( name ) ) {
				String message = String.format( "there is no entity inheritance that match the name [%s]", name );
				throw ExceptionUtil.createException( session, null, message, null );
			} else {
				throw ExceptionUtil.createException( session, null, "cannot create query", null );
			}
		}
		return qry;
	}

	private static Query toQuery( PageContext pc, HibernateORMSession session, Component cfc, String entityName, Query qry,
	    int rowcount, int row ) throws PageException {
		// inheritance mapping
		if ( !Util.isEmpty( entityName ) ) {
			return inheritance( pc, session, cfc, qry, entityName );
		}
		return populateQuery( pc, session, cfc, qry );
	}

	private static Query populateQuery( PageContext pc, HibernateORMSession session, Component cfc, Query qry )
	    throws PageException {
		Property[]			properties	= CommonUtil.getProperties( cfc, true, true, false, false );
		String				dsn			= CommonUtil.getDataSourceName( pc, cfc );
		ComponentScope		scope		= cfc.getComponentScope();
		HibernateORMEngine	engine		= ( HibernateORMEngine ) session.getEngine();

		// init
		if ( qry == null ) {
			SessionFactory	factory	= session.getRawSessionFactory( dsn );
			ClassMetadata	md		= factory.getClassMetadata( getEntityName( cfc ) );
			Array			names	= CommonUtil.createArray();
			Array			types	= CommonUtil.createArray();
			String			name;
			int				t;
			Object			obj;
			Struct			sct;
			String			fieldType;
			for ( int i = 0; i < properties.length; i++ ) {
				obj = properties[ i ].getMetaData();
				if ( obj instanceof Struct ) {
					sct			= ( Struct ) obj;
					fieldType	= CommonUtil.toString( sct.get( CommonUtil.FIELDTYPE, null ), null );
					if ( CFConstants.Relationships.isRelationshipType( fieldType ) ) {
						continue;
					}

				}

				name = HibernateUtil.validateColumnName( md, properties[ i ].getName(), null );
				names.append( name );
				if ( name != null ) {

					t = HibernateCaster.toSQLType( HibernateUtil.getPropertyType( md, name ).getName(), NULL );
					if ( t == NULL )
						types.append( "object" );
					else
						types.append( CFMLEngineFactory.getInstance().getDBUtil().toStringType( t ) );
				} else
					types.append( "object" );
			}

			qry = CommonUtil.createQuery( names, types, 0, getEntityName( cfc ) );

		}
		// check
		else if ( engine.getMode() == ORMEngine.MODE_STRICT && !qry.getName().equals( getEntityName( cfc ) ) ) {
			throw ExceptionUtil.createException( session, null, "can only merge entities of the same kind to a query", null );
		}

		// populate
		Key[]	names	= CFMLEngineFactory.getInstance().getDBUtil().getColumnNames( qry );

		int		row		= qry.addRow();
		for ( int i = 0; i < names.length; i++ ) {
			qry.setAtEL( names[ i ], row, scope.get( names[ i ], null ) );
		}
		return qry;
	}

	private static Query inheritance( PageContext pc, HibernateORMSession session, Component cfc, Query qry, String entityName )
	    throws PageException {
		Property[]		properties	= cfc.getProperties( true, false, false, false );
		ComponentScope	scope		= cfc.getComponentScope();
		Object			value;
		Array			arr;
		for ( int i = 0; i < properties.length; i++ ) {
			value = scope.get( CommonUtil.createKey( properties[ i ].getName() ), null );
			if ( value instanceof Component ) {
				qry = inheritance( pc, session, qry, cfc, ( Component ) value, entityName );
			} else if ( CommonUtil.isArray( value ) ) {
				arr = CommonUtil.toArray( value );
				Iterator<Object> it = arr.valueIterator();
				while ( it.hasNext() ) {
					value = it.next();
					if ( value instanceof Component ) {
						qry = inheritance( pc, session, qry, cfc, ( Component ) value, entityName );
					}
				}
			}
		}
		return qry;
	}

	private static Query inheritance( PageContext pc, HibernateORMSession session, Query qry, Component parent, Component child,
	    String entityName ) throws PageException {
		if ( getEntityName( child ).equalsIgnoreCase( entityName ) )
			return populateQuery( pc, session, child, qry );
		return inheritance( pc, session, child, qry, entityName );// @TODO: geh ACF auch so tief?
	}

	public static Component toComponent( Object obj ) throws PageException {
		return CommonUtil.toComponent( obj );
	}
}
