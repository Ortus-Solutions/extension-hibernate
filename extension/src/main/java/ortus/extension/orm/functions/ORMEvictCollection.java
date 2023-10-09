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

import ortus.extension.orm.util.ORMUtil;

import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.util.Cast;
import lucee.runtime.ext.function.BIF;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;

/**
 * CFML built-in function to evict a collection from the first-level cache (the persistence context).
 */
public class ORMEvictCollection extends BIF {
    private static final int MIN_ARGUMENTS = 2;
    private static final int MAX_ARGUMENTS = 3;

    public static String call( PageContext pc, String entityName, String collectionName ) throws PageException {
        return call( pc, entityName, collectionName, null );
    }

    public static String call( PageContext pc, String entityName, String collectionName, String primaryKey )
            throws PageException {
        ORMSession session = ORMUtil.getSession( pc );
        if ( Util.isEmpty( primaryKey ) )
            session.evictCollection( pc, entityName, collectionName );
        else
            session.evictCollection( pc, entityName, collectionName, primaryKey );
        return null;
    }

    @Override
    public Object invoke( PageContext pc, Object[] args ) throws PageException {
        CFMLEngine engine = CFMLEngineFactory.getInstance();
        Cast cast = engine.getCastUtil();

        if ( args.length == 2 )
            return call( pc, cast.toString( args[ 0 ] ), cast.toString( args[ 1 ] ) );
        if ( args.length == 3 )
            return call( pc, cast.toString( args[ 0 ] ), cast.toString( args[ 1 ] ), cast.toString( args[ 2 ] ) );

        throw engine.getExceptionUtil().createFunctionException( pc, "ORMEvictCollection", MIN_ARGUMENTS, MAX_ARGUMENTS, args.length );
    }
}