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
import ortus.extension.orm.util.ORMUtil;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public class ORMGetSessionFactory extends BIF {
    private static final int MIN_ARGUMENTS = 0;
    private static final int MAX_ARGUMENTS = 1;

    private static final long serialVersionUID = -8739815940242857106L;

    public static Object call( PageContext pc ) throws PageException {
        return call( pc, null );
    }

    public static Object call( PageContext pc, String datasource ) throws PageException {
        String dsn = ORMUtil.getDataSource( pc, datasource ).getName();
        return ORMUtil.getSession( pc ).getRawSessionFactory( dsn );
    }

    @Override
    public Object invoke( PageContext pc, Object[] args ) throws PageException {
        if ( args.length == 0 )
            return call( pc );
        return call( pc, CommonUtil.toString( args[ 0 ] ) );
        // @TODO: Enable this throw in the next major version
        // throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException( pc, "ORMGetSessionFactory", MIN_ARGUMENTS, MAX_ARGUMENTS, args.length );
    }
}