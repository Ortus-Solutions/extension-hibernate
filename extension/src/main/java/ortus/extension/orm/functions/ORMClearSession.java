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
import lucee.runtime.util.Cast;
import lucee.runtime.ext.function.BIF;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;

/**
 * CFML built-in function to clear the ORM session.
 */
public class ORMClearSession extends BIF {
    private static final int MIN_ARGUMENTS = 0;
    private static final int MAX_ARGUMENTS = 1;

    public static String call( PageContext pc ) throws PageException {
        return call( pc, null );
    }

    public static String call( PageContext pc, String datasource ) throws PageException {
        if ( Util.isEmpty( datasource, true ) )
            ORMUtil.getSession( pc ).clear( pc );
        else
            ORMUtil.getSession( pc ).clear( pc, datasource.trim() );
        return null;
    }

    @Override
    public Object invoke( PageContext pc, Object[] args ) throws PageException {
        CFMLEngine engine = CFMLEngineFactory.getInstance();
        Cast cast = engine.getCastUtil();

        if ( args.length == 0 )
            return call( pc );
        if ( args.length == 1 )
            return call( pc, cast.toString( args[ 0 ] ) );

        throw engine.getExceptionUtil().createFunctionException( pc, "ORMClearSession", MIN_ARGUMENTS, MAX_ARGUMENTS, args.length );
    }
}