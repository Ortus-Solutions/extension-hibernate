/**
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package ortus.extension.orm.functions;

import ortus.extension.orm.util.CommonUtil;
import ortus.extension.orm.util.ExceptionUtil;
import ortus.extension.orm.util.ORMUtil;

import java.util.List;
import java.util.Map;

import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

import lucee.runtime.util.Cast;
import lucee.runtime.ext.function.BIF;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;

/**
 * CFML built-in function to execute an HQL query through Hibernate ORM.
 */
public class ORMExecuteQuery extends BIF {

    private static final int MIN_ARGUMENTS = 1;
    private static final int MAX_ARGUMENTS = 4;

    /**
     * Used in `ORMQueryExecute()` to ensure that "too many args" error messages have the correct method name.
     */
    protected String functionName = "ORMExecuteQuery";

    public static Object call( PageContext pc, String hql ) throws PageException {
        return _call( pc, hql, null, false, null );
    }

    public static Object call( PageContext pc, String hql, Object paramsOrUnique ) throws PageException {
        if ( CommonUtil.isCastableToBoolean( paramsOrUnique ) ) {
            return _call( pc, hql, null, CommonUtil.toBooleanValue( paramsOrUnique ), null );
        }
        return _call( pc, hql, paramsOrUnique, false, null );
    }

    public static Object call( PageContext pc, String hql, Object paramsOrUnique, Object uniqueOrQueryOptions )
            throws PageException {
        if ( CommonUtil.isCastableToBoolean( paramsOrUnique ) ) {
            return _call( pc, hql, null, CommonUtil.toBooleanValue( paramsOrUnique ),
                    CommonUtil.toStruct( uniqueOrQueryOptions ) );
        }
        if ( CommonUtil.isCastableToBoolean( uniqueOrQueryOptions ) ) {
            return _call( pc, hql, paramsOrUnique, CommonUtil.toBooleanValue( uniqueOrQueryOptions ), null );
        }
        return _call( pc, hql, paramsOrUnique, false, CommonUtil.toStruct( uniqueOrQueryOptions ) );
    }

    public static Object call( PageContext pc, String hql, Object params, boolean isUnique, Struct queryOptions )
            throws PageException {
        return _call( pc, hql, params, isUnique, queryOptions );
    }

    private static Object _call( PageContext pc, String hql, Object params, boolean unique, Struct queryOptions )
            throws PageException {
        ORMSession session = ORMUtil.getSession( pc );
        String dsn = null;
        if ( queryOptions != null )
            dsn = CommonUtil.toString( queryOptions.get( CommonUtil.createKey( "datasource" ), null ), null );
        if ( Util.isEmpty( dsn, true ) )
            dsn = ORMUtil.getDefaultDataSource( pc ).getName();

        if ( params == null )
            return toCFML( session.executeQuery( pc, dsn, hql, CommonUtil.createArray(), unique, queryOptions ) );
        else if ( CommonUtil.isStruct( params ) )
            return toCFML( session.executeQuery( pc, dsn, hql, CommonUtil.toStruct( params ), unique, queryOptions ) );
        else if ( CommonUtil.isArray( params ) )
            return toCFML( session.executeQuery( pc, dsn, hql, CommonUtil.toArray( params ), unique, queryOptions ) );
        else if ( CommonUtil.isCastableToStruct( params ) )
            return toCFML( session.executeQuery( pc, dsn, hql, CommonUtil.toStruct( params ), unique, queryOptions ) );
        else if ( CommonUtil.isCastableToArray( params ) )
            return toCFML( session.executeQuery( pc, dsn, hql, CommonUtil.toArray( params ), unique, queryOptions ) );
        else
            throw ExceptionUtil.createException( "ORMExecuteQuery : cannot convert the params to an array or a struct" );

    }

    private static Object toCFML( Object obj ) throws PageException {
        if ( obj instanceof List<?> && ! ( obj instanceof Array ) )
            return CommonUtil.toArray( obj );
        if ( obj instanceof Map<?, ?> && ! ( obj instanceof Struct ) )
            return CommonUtil.toStruct( obj );
        return obj;
    }

    @Override
    public Object invoke( PageContext pc, Object[] args ) throws PageException {
        CFMLEngine engine = CFMLEngineFactory.getInstance();
        Cast cast = engine.getCastUtil();

        if ( args.length == 1 )
            return call( pc, cast.toString( args[ 0 ] ) );
        if ( args.length == 2 )
            return call( pc, cast.toString( args[ 0 ] ), args[ 1 ] );
        if ( args.length == 3 )
            return call( pc, cast.toString( args[ 0 ] ), args[ 1 ], args[ 2 ] );
        if ( args.length == 4 ) {
            Struct queryOptions = null;
            if ( args[ 3 ] != null ) {
                queryOptions = cast.toStruct( args[ 3 ] );
            }
            return call( pc, cast.toString( args[ 0 ] ), args[ 1 ], cast.toBoolean( args[ 2 ], false ), queryOptions );
        }

        throw engine.getExceptionUtil().createFunctionException( pc, this.functionName, MIN_ARGUMENTS, MAX_ARGUMENTS, args.length );
    }
}