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

import ortus.extension.orm.functions.ORMExecuteQuery;

/**
 * Modern alias to ORMExecuteQuery(). Run an HQL query on the default ORM datasource.
 */
public class ORMQueryExecute extends ORMExecuteQuery {

    /**
     * Override ORMExecuteQuery to ensure that "too many args" error messages have the correct method name.
     */
    protected String functionName = "ORMQueryExecute";
}